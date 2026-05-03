package sintactico;

import lexico.TipoToken;
import lexico.Token;
import sintactico.nodos.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class AnalizadorSintactico {

    private final List<Token> tokens;
    private int               pos;
    private final List<String> errores;

    private static final List<TipoToken> TIPOS = List.of(
        TipoToken.ENTERO, TipoToken.NUMERO, TipoToken.TEXTO,
        TipoToken.LOG, TipoToken.VACIO
    );

    //aqui reciben la lista de tokens filtrados ya sin saltos de linea y comentarios
    public AnalizadorSintactico(List<Token> tokens) {

        this.tokens  = filtrarLineas(tokens);
        this.pos     = 0; //es un número entero que dice en qué posición de la lista está parado ahora mismo.
        //aumenta en 1 cada vez que se llama al metodo Consumir() o avanzar()
        this.errores = new ArrayList<>();
    }

    // API pública

    //pos es el cursor compartido. Todos los métodos usan el mismo pos.
    // Entonces por ejemplo cuando parsearSi() consume tokens, el pos avanza.
    // Cuando termina y devuelve el nodo, el pos ya está apuntando al siguiente token disponible.
    // eso de pos esta implicito en el metodo consumir porque esta el pos++
    public NodoPrograma analizar() {
        List<Nodo> sentencias = new ArrayList<>();
        while (!finDelArchivo()) {
            try {
                Nodo s = parsearSentencia(); //parsea una sentencia
                if (s != null) sentencias.add(s); //la agrega a la lista
            } catch (ErrorSintactico e) {
                errores.add(e.getMessage());
                sincronizar(); // recuperación de errores: avanza hasta la próxima línea segura
            }
        }
        return new NodoPrograma(sentencias);
    }

    public List<String> getErrores()  { return errores;           }
    public boolean      hayErrores()  { return !errores.isEmpty(); }

    //Sentencias

    //cada if solo mira 1 token para tomar la decisión.
    // No necesita leer toda la sentencia para saber qué es, con el primer token ya lo sabe.
    private Nodo parsearSentencia() {
        // Módulos
        if (verifica(TipoToken.IMPORTAR)) return parsearImportar();
        if (verifica(TipoToken.EXPORTAR)) return parsearExportar();

        // Declaración de objeto/clase
        if (verifica(TipoToken.OBJETO))   return parsearDeclaracionObjeto();

        // Declaración de función
        if (esCabeceraDeFuncion())        return parsearDeclaracionFuncion(false);
        if (verifica(TipoToken.ASYNC))    return parsearDeclaracionFuncionAsync();

        // Declaración de variables tipadas (primitivas, lista, jsn)
        if (esTipo() || esDeclaracionConVar()) return parsearDeclaracion();

        // Declaración con tipo objeto: Usuario persona = nuevo Usuario(...)
        if (verifica(TipoToken.IDENTIFICADOR) && esDeclaracionTipoObjeto()) return parsearDeclaracion();

        // Control de flujo
        if (verifica(TipoToken.SI))        return parsearSi();
        if (verifica(TipoToken.MIENTRAS))  return parsearMientras();
        if (verifica(TipoToken.HACER))     return parsearHacer();
        if (verifica(TipoToken.PARA))      return parsearPara();
        if (verifica(TipoToken.RETORNAR))  return parsearRetornar();
        if (verifica(TipoToken.ROMPER))    { int l = lineaActual(); consumir(TipoToken.ROMPER);    return new NodoRomper(l); }
        if (verifica(TipoToken.CONTINUAR)) { int l = lineaActual(); consumir(TipoToken.CONTINUAR); return new NodoContinuar(l); }
        if (verifica(TipoToken.LANZAR))    return parsearLanzar();
        if (verifica(TipoToken.INTENTAR))  return parsearIntentar();

        // Expresión / asignación
        return parsearSentenciaExpresion();
    }

    // IMPORTAR / EXPORTAR

    //importar { sumar, restar } desde "Matematica"
    //los simbolos son  lista de nombres que se quieren importar
    // y la ruta es el nombre de archivo de donde se van a importar, sería una clase
    private NodoImportar parsearImportar() {
        int linea = lineaActual();
        consumir(TipoToken.IMPORTAR);
        consumir(TipoToken.LLAVE_IZQ);
        List<String> simbolos = new ArrayList<>();
        simbolos.add(consumir(TipoToken.IDENTIFICADOR).getValor());
        while (verifica(TipoToken.COMA)) {
            consumir(TipoToken.COMA);
            simbolos.add(consumir(TipoToken.IDENTIFICADOR).getValor());
        }
        consumir(TipoToken.LLAVE_DER);
        consumir(TipoToken.DESDE);
        String ruta = consumir(TipoToken.LITERAL_TEXTO).getValor();
        return new NodoImportar(linea, simbolos, ruta);
    }

    //el exportar son por ejemplo desde matematica solo suma y resta es visible para *la ruta* osea el archivo donde se quiere usar
    private NodoExportar parsearExportar() {
        int linea = lineaActual();
        consumir(TipoToken.EXPORTAR);
        consumir(TipoToken.LLAVE_IZQ);
        List<String> simbolos = new ArrayList<>();
        simbolos.add(consumir(TipoToken.IDENTIFICADOR).getValor());
        while (verifica(TipoToken.COMA)) {
            consumir(TipoToken.COMA);
            simbolos.add(consumir(TipoToken.IDENTIFICADOR).getValor());
        }
        consumir(TipoToken.LLAVE_DER);
        return new NodoExportar(linea, simbolos);
    }

    // DECLARACIONES

    /**
     * Detecta si la posición actual es una declaración:
     *   tipo [var] nombre = ...
     *   lista[<tipo>] [var] nombre = ...
     *   jsn [var] nombre = ...
     */
    private Nodo parsearDeclaracion() {
        int linea = lineaActual();

        // lista<tipo> var nombre = ...  o  lista var nombre = ...  o  lista nombre = ...
        if (verifica(TipoToken.LISTA)) {
            return parsearDeclaracionLista(linea);
        }

        // jsn [var] nombre = ...
        if (verifica(TipoToken.JSN)) {
            return parsearDeclaracionJsn(linea);
        }

        // Tipo de objeto (identificador con mayúscula) var nombre = nuevo ...
        // ej: Usuario var persona = nuevo Usuario(...)
        if (verifica(TipoToken.IDENTIFICADOR) && esDeclaracionTipoObjeto()) {
            return parsearDeclaracionTipoObjeto(linea);
        }

        // tipo primitivo [var] nombre [= expr]
        return parsearDeclaracionVariable(linea);
    }

    private NodoDeclaracionVariable parsearDeclaracionVariable(int linea) {
        String tipo   = consumirTipo();
        //indica si la variable puede cambiar de valor después de ser declarada.
        boolean mutable = false;
        // si contiene la palabra reservada var se hace mutable, si no, es inmutable (constante)
        if (verifica(TipoToken.VAR)) { consumir(TipoToken.VAR); mutable = true; }
        String nombre = consumir(TipoToken.IDENTIFICADOR).getValor();
        Nodo valor = null;
        if (verifica(TipoToken.OP_ASIGNACION)) {
            consumir(TipoToken.OP_ASIGNACION);
            valor = parsearExpresion();
        }
        return new NodoDeclaracionVariable(linea, tipo, mutable, nombre, valor);
    }

    private String consumirTipoCompleto() {
        if (verifica(TipoToken.LISTA)) {
            consumir(TipoToken.LISTA);
            if (verifica(TipoToken.OP_MENOR)) {
                consumir(TipoToken.OP_MENOR);
                String tipoInterno = consumirTipoCompleto(); // recursivo
                consumir(TipoToken.OP_MAYOR);
                return "lista<" + tipoInterno + ">";
            }
            return "lista";
        }
        return consumirTipo(); // para entero, numero, texto, log, vacio
    }

    private NodoDeclaracionLista parsearDeclaracionLista(int linea) {
        consumir(TipoToken.LISTA);
        String tipoElemento = null;
        if (verifica(TipoToken.OP_MENOR)) {
            consumir(TipoToken.OP_MENOR);
            tipoElemento = consumirTipoCompleto();
            consumir(TipoToken.OP_MAYOR);
        }
        boolean mutable = false;
        if (verifica(TipoToken.VAR)) { consumir(TipoToken.VAR); mutable = true; }
        String nombre = consumir(TipoToken.IDENTIFICADOR).getValor();
        Nodo valor = null;
        if (verifica(TipoToken.OP_ASIGNACION)) {
            consumir(TipoToken.OP_ASIGNACION);
            valor = parsearExpresion();
        }
        return new NodoDeclaracionLista(linea, tipoElemento, mutable, nombre, valor);
    }

    private NodoDeclaracionJsn parsearDeclaracionJsn(int linea) {
        consumir(TipoToken.JSN);
        boolean mutable = false;
        if (verifica(TipoToken.VAR)) { consumir(TipoToken.VAR); mutable = true; }
        String nombre = consumir(TipoToken.IDENTIFICADOR).getValor();
        Nodo valor = null;
        if (verifica(TipoToken.OP_ASIGNACION)) {
            consumir(TipoToken.OP_ASIGNACION);
            valor = parsearExpresion();
        }
        return new NodoDeclaracionJsn(linea, mutable, nombre, valor);
    }

    /** Declaración con tipo objeto: Usuario persona = nuevo Usuario(...) */
    private NodoDeclaracionVariable parsearDeclaracionTipoObjeto(int linea) {
        String tipo = consumir(TipoToken.IDENTIFICADOR).getValor();
        boolean mutable = false;
        if (verifica(TipoToken.VAR)) { consumir(TipoToken.VAR); mutable = true; }
        String nombre = consumir(TipoToken.IDENTIFICADOR).getValor();
        Nodo valor = null;
        if (verifica(TipoToken.OP_ASIGNACION)) {
            consumir(TipoToken.OP_ASIGNACION);
            valor = parsearExpresion();
        }
        return new NodoDeclaracionVariable(linea, tipo, mutable, nombre, valor);
    }

    // FUNCIONES

    private boolean esCabeceraDeFuncion() {
        // Patrón: tipo identificador (
        if (!esTipo()) return false;
        int guardado = pos;
        consumirTipo();                                    // consume el tipo
        if (!verifica(TipoToken.IDENTIFICADOR)) { pos = guardado; return false; }
        pos++;                                             // consume el nombre
        boolean esFun = verifica(TipoToken.PAREN_IZQ);    // debe seguir '('
        pos = guardado;
        return esFun;
    }

    private NodoDeclaracionFuncion parsearDeclaracionFuncion(boolean esAsync) {
        int linea = lineaActual();
        String tipoRetorno = "vacio";
        if (esTipo()) tipoRetorno = consumirTipo();
        // No se consume 'funcion' — la sintaxis es: tipo nombre(params) { }
        String nombre = consumir(TipoToken.IDENTIFICADOR).getValor();
        List<NodoDeclaracionFuncion.Parametro> params = parsearParametros();
        NodoBloque cuerpo = parsearBloque();
        return new NodoDeclaracionFuncion(linea, tipoRetorno, esAsync, nombre, params, cuerpo);
    }

    private NodoDeclaracionFuncion parsearDeclaracionFuncionAsync() {
        consumir(TipoToken.ASYNC);
        return parsearDeclaracionFuncion(true);
    }

    private List<NodoDeclaracionFuncion.Parametro> parsearParametros() {
        List<NodoDeclaracionFuncion.Parametro> params = new ArrayList<>();
        consumir(TipoToken.PAREN_IZQ);
        if (!verifica(TipoToken.PAREN_DER)) {
            do {
                if (verifica(TipoToken.COMA)) consumir(TipoToken.COMA);
                String tipo   = consumirTipoOIdentificador();
                String nombre = consumir(TipoToken.IDENTIFICADOR).getValor();
                params.add(new NodoDeclaracionFuncion.Parametro(tipo, nombre));
            } while (verifica(TipoToken.COMA));
        }
        consumir(TipoToken.PAREN_DER);
        return params;
    }

    // OBJETO / CLASE

    private NodoDeclaracionObjeto parsearDeclaracionObjeto() {
        int linea = lineaActual();
        consumir(TipoToken.OBJETO);
        String nombre = consumir(TipoToken.IDENTIFICADOR).getValor();
        consumir(TipoToken.LLAVE_IZQ);

        List<NodoDeclaracionVariable> atribPriv  = new ArrayList<>();
        List<NodoDeclaracionVariable> atribPub   = new ArrayList<>();
        List<NodoDeclaracionFuncion>  metodsPub  = new ArrayList<>();
        List<NodoDeclaracionFuncion>  metodsPriv = new ArrayList<>();
        NodoDeclaracionFuncion constructor = null;

        while (!verifica(TipoToken.LLAVE_DER) && !finDelArchivo()) {

            if (verifica(TipoToken.PRIVADO)) {
                consumir(TipoToken.PRIVADO);
                consumir(TipoToken.DOS_PUNTOS);
                // Lee atributos privados hasta el siguiente bloque o }
                while (!verifica(TipoToken.PUBLICO) && !verifica(TipoToken.PRIVADO)
                        && !verifica(TipoToken.LLAVE_DER) && !finDelArchivo()) {
                    if (esTipo()) {
                        atribPriv.add(parsearDeclaracionVariable(lineaActual()));
                    } else break;
                }

            } else if (verifica(TipoToken.PUBLICO)) {
                consumir(TipoToken.PUBLICO);
                consumir(TipoToken.DOS_PUNTOS);
                // Lee constructor y métodos públicos
                while (!verifica(TipoToken.PRIVADO) && !verifica(TipoToken.PUBLICO)
                        && !verifica(TipoToken.LLAVE_DER) && !finDelArchivo()) {

                    int lm = lineaActual();

                    // Constructor: mismo nombre que el objeto seguido de (
                    if (verifica(TipoToken.IDENTIFICADOR) && actual().getValor().equals(nombre)
                            && verificaSiguiente(TipoToken.PAREN_IZQ)) {
                        consumir(TipoToken.IDENTIFICADOR);
                        List<NodoDeclaracionFuncion.Parametro> params = parsearParametros();
                        NodoBloque cuerpo = parsearBloque();
                        constructor = new NodoDeclaracionFuncion(lm, nombre, false, nombre, params, cuerpo);

                    // Método público
                    } else if (esTipo()) {
                        String tipoRet = consumirTipo();
                        String nomMetodo = consumir(TipoToken.IDENTIFICADOR).getValor();
                        List<NodoDeclaracionFuncion.Parametro> params = parsearParametros();
                        NodoBloque cuerpo = parsearBloque();
                        metodsPub.add(new NodoDeclaracionFuncion(lm, tipoRet, false, nomMetodo, params, cuerpo));

                    } else if (verifica(TipoToken.FUNCION)) {
                        metodsPub.add(parsearDeclaracionFuncion(false));

                    } else break;
                }
            } else {
                // Miembro libre (sin bloque privado/publico)
                break;
            }
        }

        consumir(TipoToken.LLAVE_DER);
        return new NodoDeclaracionObjeto(linea, nombre, atribPriv, atribPub, constructor, metodsPub, metodsPriv);
    }

    // CONTROL DE FLUJO

    private NodoSi parsearSi() {
        int linea = lineaActual();
        consumir(TipoToken.SI);
        consumir(TipoToken.PAREN_IZQ);//la palabra reservada SI va seguida de un parentesis, si no hay, se lanza un error porque es estructura fija
        Nodo condicion = parsearExpresion();
        consumir(TipoToken.PAREN_DER);
        NodoBloque cuerpoIf = parsearBloque();

        List<NodoSi.RamaElseIf> ramasElseIf = new ArrayList<>();
        NodoBloque cuerpoElse = null;

        while (verifica(TipoToken.SINO_SI) ||
                (verifica(TipoToken.SINO) && verificaSiguiente(TipoToken.SI))) {

            // consume sino_si  O  consume sino + si
            if (verifica(TipoToken.SINO_SI)) {
                consumir(TipoToken.SINO_SI);
            } else {
                consumir(TipoToken.SINO);
                consumir(TipoToken.SI);
            }

            consumir(TipoToken.PAREN_IZQ);
            Nodo condSinoSi = parsearExpresion();
            consumir(TipoToken.PAREN_DER);
            NodoBloque cuerpoSinoSi = parsearBloque();
            ramasElseIf.add(new NodoSi.RamaElseIf(condSinoSi, cuerpoSinoSi));
        }

        if (verifica(TipoToken.SINO) && !verificaSiguiente(TipoToken.SI)) {
            consumir(TipoToken.SINO);
            cuerpoElse = parsearBloque();
        }
        return new NodoSi(linea, condicion, cuerpoIf, ramasElseIf, cuerpoElse);
    }

    private NodoMientras parsearMientras() {
        int linea = lineaActual();
        consumir(TipoToken.MIENTRAS);
        consumir(TipoToken.PAREN_IZQ);
        Nodo condicion = parsearExpresion();
        consumir(TipoToken.PAREN_DER);
        NodoBloque cuerpo = parsearBloque();
        return new NodoMientras(linea, condicion, cuerpo);
    }

    private NodoHacer parsearHacer() {
        int linea = lineaActual();
        consumir(TipoToken.HACER);
        NodoBloque cuerpo = parsearBloque();
        consumir(TipoToken.MIENTRAS);
        consumir(TipoToken.PAREN_IZQ);
        Nodo condicion = parsearExpresion();
        consumir(TipoToken.PAREN_DER);
        return new NodoHacer(linea, cuerpo, condicion);
    }

    private Nodo parsearPara() {
        int linea = lineaActual();
        consumir(TipoToken.PARA);

        // para elemento en coleccion { }
        if (verifica(TipoToken.IDENTIFICADOR) && verificaAdelante(TipoToken.EN, 1)) {
            String variable = consumir(TipoToken.IDENTIFICADOR).getValor();
            consumir(TipoToken.EN);
            Nodo coleccion = parsearExpresion();
            NodoBloque cuerpo = parsearBloque();
            return new NodoParaEn(linea, variable, coleccion, cuerpo);
        }

        // para (init; cond; paso) { }
        consumir(TipoToken.PAREN_IZQ);
        Nodo init = null;
        if (esTipo()) {
            init = parsearDeclaracionVariable(lineaActual());
        } else if (!verifica(TipoToken.PUNTO_COMA)) {
            init = parsearSentenciaExpresion();
        }
        consumirOpcional(TipoToken.PUNTO_COMA);

        Nodo condicion = null;
        if (!verifica(TipoToken.PUNTO_COMA)) condicion = parsearExpresion();
        consumirOpcional(TipoToken.PUNTO_COMA);

        Nodo paso = null;
        if (!verifica(TipoToken.PAREN_DER)) paso = parsearSentenciaExpresion();

        consumir(TipoToken.PAREN_DER);
        NodoBloque cuerpo = parsearBloque();
        return new NodoPara(linea, init, condicion, paso, cuerpo);
    }

    private NodoRetornar parsearRetornar() {
        int linea = lineaActual();
        consumir(TipoToken.RETORNAR);
        Nodo valor = null;
        if (!finLinea()) valor = parsearExpresion();
        return new NodoRetornar(linea, valor);
    }

    private NodoLanzar parsearLanzar() {
        int linea = lineaActual();
        consumir(TipoToken.LANZAR);
        Nodo expr = parsearExpresion();
        return new NodoLanzar(linea, expr);
    }

    private NodoIntentar parsearIntentar() {
        int linea = lineaActual();
        consumir(TipoToken.INTENTAR);
        NodoBloque cuerpoIntentar = parsearBloque();

        String variableError = null;
        NodoBloque cuerpoCapturar = null;
        if (verifica(TipoToken.CAPTURAR)) {
            consumir(TipoToken.CAPTURAR);
            consumir(TipoToken.PAREN_IZQ);
            variableError = consumir(TipoToken.IDENTIFICADOR).getValor();
            consumir(TipoToken.PAREN_DER);
            cuerpoCapturar = parsearBloque();
        }

        NodoBloque cuerpoFinalmente = null;
        if (verifica(TipoToken.FINALMENTE)) {
            consumir(TipoToken.FINALMENTE);
            cuerpoFinalmente = parsearBloque();
        }

        return new NodoIntentar(linea, cuerpoIntentar, variableError, cuerpoCapturar, cuerpoFinalmente);
    }

    // EXPRESIONES

    private Nodo parsearSentenciaExpresion() {
        int linea = lineaActual();
        Nodo expr = parsearExpresion();

        // Asignación: nombre = valor
        if (verifica(TipoToken.OP_ASIGNACION)) {
            consumir(TipoToken.OP_ASIGNACION);
            Nodo valor = parsearExpresion();
            return new NodoAsignacion(linea, expr, valor);
        }

        // Asignación compuesta: nombre += valor
        if (esOperadorAsignacionCompuesta()) {
            String op = actual().getValor();
            avanzar();
            Nodo valor = parsearExpresion();
            return new NodoAsignacionCompuesta(linea, expr, op, valor);
        }

        // Incremento / Decremento: nombre++ / nombre--
        if (verifica(TipoToken.OP_INCREMENTO)) {
            consumir(TipoToken.OP_INCREMENTO);
            return new NodoIncrementoDecremento(linea, expr, "++");
        }
        if (verifica(TipoToken.OP_DECREMENTO)) {
            consumir(TipoToken.OP_DECREMENTO);
            return new NodoIncrementoDecremento(linea, expr, "--");
        }

        return expr;
    }

    /** expresion ---- ternaria */
    public Nodo parsearExpresion() {
        return parsearTernaria();
    }

    private Nodo parsearTernaria() {
        int linea = lineaActual();
        Nodo cond = parsearLogicaO();
        if (verifica(TipoToken.INTERROGACION)) {
            consumir(TipoToken.INTERROGACION);
            Nodo siV = parsearExpresion();
            consumir(TipoToken.DOS_PUNTOS);
            Nodo siF = parsearExpresion();
            return new NodoTernaria(linea, cond, siV, siF);
        }
        return cond;
    }

    private Nodo parsearLogicaO() {
        int linea = lineaActual();
        Nodo izq = parsearLogicaY();
        while (verifica(TipoToken.O) || verifica(TipoToken.OP_OR)) {
            String op = actual().getValor();
            avanzar();
            Nodo der = parsearLogicaY();
            izq = new NodoBinaria(linea, izq, op, der);
        }
        return izq;
    }

    private Nodo parsearLogicaY() {
        int linea = lineaActual();
        Nodo izq = parsearIgualdad();
        while (verifica(TipoToken.Y) || verifica(TipoToken.OP_AND)) {
            String op = actual().getValor();
            avanzar();
            Nodo der = parsearIgualdad();
            izq = new NodoBinaria(linea, izq, op, der);
        }
        return izq;
    }

    private Nodo parsearIgualdad() {
        int linea = lineaActual();
        Nodo izq = parsearComparacion();
        while (verifica(TipoToken.OP_IGUAL) || verifica(TipoToken.OP_DIFERENTE)) {
            String op = actual().getValor();
            avanzar();
            Nodo der = parsearComparacion();
            izq = new NodoBinaria(linea, izq, op, der);
        }
        return izq;
    }

    private Nodo parsearComparacion() {
        int linea = lineaActual();
        Nodo izq = parsearSuma();
        while (verifica(TipoToken.OP_MENOR) || verifica(TipoToken.OP_MAYOR)
            || verifica(TipoToken.OP_MENOR_IGUAL) || verifica(TipoToken.OP_MAYOR_IGUAL)) {
            String op = actual().getValor();
            avanzar();
            Nodo der = parsearSuma();
            izq = new NodoBinaria(linea, izq, op, der);
        }
        return izq;
    }

    private Nodo parsearSuma() {
        int linea = lineaActual();
        Nodo izq = parsearMult();
        while (verifica(TipoToken.OP_SUMA) || verifica(TipoToken.OP_RESTA)) {
            String op = actual().getValor();
            avanzar();
            Nodo der = parsearMult();
            izq = new NodoBinaria(linea, izq, op, der);
        }
        return izq;
    }

    private Nodo parsearMult() {
        int linea = lineaActual();
        Nodo izq = parsearUnaria();
        while (verifica(TipoToken.OP_MULT) || verifica(TipoToken.OP_DIV) || verifica(TipoToken.OP_MOD)) {
            String op = actual().getValor();
            avanzar();
            Nodo der = parsearUnaria();
            izq = new NodoBinaria(linea, izq, op, der);
        }
        return izq;
    }

    private Nodo parsearUnaria() {
        int linea = lineaActual();
        if (verifica(TipoToken.OP_RESTA)) { avanzar(); return new NodoUnaria(linea, "-",  parsearUnaria()); }
        if (verifica(TipoToken.OP_NOT))   { avanzar(); return new NodoUnaria(linea, "!",  parsearUnaria()); }
        if (verifica(TipoToken.NO))       { avanzar(); return new NodoUnaria(linea, "no", parsearUnaria()); }
        return parsearPostfijo();
    }

    private Nodo parsearPostfijo() {
        int linea = lineaActual();
        Nodo expr = parsearPrimario();

        while (true) {
            if (verifica(TipoToken.PUNTO)) {
                consumir(TipoToken.PUNTO);
                String campo = consumirIdentificadorOPalabraReservada();

                // Llamada a método: expr.campo(args)
                if (verifica(TipoToken.PAREN_IZQ)) {
                    List<Nodo> args = parsearArgumentos();
                    // Caso especial: consola.mostrar(...)
                    if (expr instanceof NodoIdentificador && ((NodoIdentificador) expr).nombre.equals("consola")) {
                        expr = new NodoConsola(linea, campo, args);
                    } else {
                        expr = new NodoLlamadaMetodo(linea, expr, campo, args);
                    }
                } else {
                    expr = new NodoAccesoMiembro(linea, expr, campo);
                }

            } else if (verifica(TipoToken.CORCHETE_IZQ)) {
                consumir(TipoToken.CORCHETE_IZQ);
                Nodo indice = parsearExpresion();
                consumir(TipoToken.CORCHETE_DER);
                expr = new NodoAccesoIndice(linea, expr, indice);

            } else {
                break;
            }
        }
        return expr;
    }

    private Nodo parsearPrimario() {
        int linea = lineaActual();

        // Literales simples
        if (verifica(TipoToken.LITERAL_ENTERO)) {
            int val = Integer.parseInt(consumir(TipoToken.LITERAL_ENTERO).getValor());
            return new NodoLiteralEntero(linea, val);
        }
        if (verifica(TipoToken.LITERAL_REAL)) {
            double val = Double.parseDouble(consumir(TipoToken.LITERAL_REAL).getValor());
            return new NodoLiteralReal(linea, val);
        }
        if (verifica(TipoToken.LITERAL_TEXTO)) {
            return new NodoLiteralTexto(linea, consumir(TipoToken.LITERAL_TEXTO).getValor());
        }
        if (verifica(TipoToken.LITERAL_TEXTO_INTERP)) {
            return new NodoLiteralTextoInterp(linea, consumir(TipoToken.LITERAL_TEXTO_INTERP).getValor());
        }
        if (verifica(TipoToken.VERDADERO)) { consumir(TipoToken.VERDADERO); return new NodoLiteralLog(linea, true);  }
        if (verifica(TipoToken.FALSO))     { consumir(TipoToken.FALSO);     return new NodoLiteralLog(linea, false); }
        if (verifica(TipoToken.NULO))      { consumir(TipoToken.NULO);      return new NodoLiteralNulo(linea);       }

        // Lista literal: [1, 2, 3]
        if (verifica(TipoToken.CORCHETE_IZQ)) {
            return parsearLiteralLista(linea);
        }

        // JSN literal: { clave: valor, ... }
        if (verifica(TipoToken.LLAVE_IZQ)) {
            return parsearLiteralJsn(linea);
        }

        // nuevo Tipo(args)
        if (verifica(TipoToken.NUEVO)) {
            consumir(TipoToken.NUEVO);
            String tipo = consumir(TipoToken.IDENTIFICADOR).getValor();
            List<Nodo> args = parsearArgumentos();
            return new NodoNuevoObjeto(linea, tipo, args);
        }

        // esperar expresion
        if (verifica(TipoToken.ESPERAR)) {
            consumir(TipoToken.ESPERAR);
            Nodo expr = parsearExpresion();
            return new NodoUnaria(linea, "esperar", expr);
        }

        // Agrupación: (expresion)
        if (verifica(TipoToken.PAREN_IZQ)) {
            consumir(TipoToken.PAREN_IZQ);
            Nodo expr = parsearExpresion();
            consumir(TipoToken.PAREN_DER);
            return expr;
        }

        // Identificador o llamada a función
        if (verifica(TipoToken.IDENTIFICADOR)) {
            String nombre = consumir(TipoToken.IDENTIFICADOR).getValor();
            if (verifica(TipoToken.PAREN_IZQ)) {
                List<Nodo> args = parsearArgumentos();
                return new NodoLlamadaFuncion(linea, nombre, args);
            }
            return new NodoIdentificador(linea, nombre);
        }

        // ambiente (referencia al objeto actual)
        if (verifica(TipoToken.AMBIENTE)) {
            consumir(TipoToken.AMBIENTE);
            return new NodoIdentificador(linea, "ambiente");
        }

        if (verifica(TipoToken.CONSOLA)) {
            consumir(TipoToken.CONSOLA);
            return new NodoIdentificador(linea, "consola");
        }

        throw error("Se esperaba una expresión pero se encontró: '" + actual().getValor() + "'");
    }

    private NodoLiteralLista parsearLiteralLista(int linea) {
        consumir(TipoToken.CORCHETE_IZQ);
        List<Nodo> elementos = new ArrayList<>();
        if (!verifica(TipoToken.CORCHETE_DER)) {
            elementos.add(parsearExpresion());
            while (verifica(TipoToken.COMA)) {
                consumir(TipoToken.COMA);
                if (verifica(TipoToken.CORCHETE_DER)) break; // trailing comma
                elementos.add(parsearExpresion());
            }
        }
        consumir(TipoToken.CORCHETE_DER);
        return new NodoLiteralLista(linea, elementos);
    }

    private NodoLiteralJsn parsearLiteralJsn(int linea) {
        consumir(TipoToken.LLAVE_IZQ);
        LinkedHashMap<String, Nodo> pares = new LinkedHashMap<>();
        while (!verifica(TipoToken.LLAVE_DER) && !finDelArchivo()) {
            String clave;
            if (verifica(TipoToken.IDENTIFICADOR)) {
                clave = consumir(TipoToken.IDENTIFICADOR).getValor();
            } else {
                clave = consumir(TipoToken.LITERAL_TEXTO).getValor();
            }
            consumir(TipoToken.DOS_PUNTOS);
            Nodo valor = parsearExpresion();
            pares.put(clave, valor);
            if (verifica(TipoToken.COMA)) consumir(TipoToken.COMA);
        }
        consumir(TipoToken.LLAVE_DER);
        return new NodoLiteralJsn(linea, pares);
    }

    private NodoBloque parsearBloque() {
        int linea = lineaActual();
        consumir(TipoToken.LLAVE_IZQ);
        List<Nodo> sentencias = new ArrayList<>();
        while (!verifica(TipoToken.LLAVE_DER) && !finDelArchivo()) {
            try {
                Nodo s = parsearSentencia();
                if (s != null) sentencias.add(s);
            } catch (ErrorSintactico e) {
                errores.add(e.getMessage());
                sincronizar();
            }
        }
        consumir(TipoToken.LLAVE_DER);
        return new NodoBloque(linea, sentencias);
    }

    private List<Nodo> parsearArgumentos() {
        List<Nodo> args = new ArrayList<>();
        consumir(TipoToken.PAREN_IZQ);
        if (!verifica(TipoToken.PAREN_DER)) {
            args.add(parsearExpresion());
            while (verifica(TipoToken.COMA)) {
                consumir(TipoToken.COMA);
                if (verifica(TipoToken.PAREN_DER)) break;
                args.add(parsearExpresion());
            }
        }
        consumir(TipoToken.PAREN_DER);
        return args;
    }


    private boolean esTipo() {
        return TIPOS.contains(tokenActual().getTipo());
    }

    private boolean esDeclaracionConVar() {
        // lista [<tipo>] [var] id = ...
        // jsn [var] id = ...
        return verifica(TipoToken.LISTA) || verifica(TipoToken.JSN);
    }

    private boolean esDeclaracionTipoObjeto() {
        String val = actual().getValor();
        return Character.isUpperCase(val.charAt(0)) && verificaSiguiente(TipoToken.IDENTIFICADOR, 1)
            || Character.isUpperCase(val.charAt(0)) && verificaSiguiente(TipoToken.VAR, 1);
    }

    private String consumirTipo() {
        Token t = tokenActual();
        if (TIPOS.contains(t.getTipo())) { avanzar(); return t.getValor(); }
        throw error("Se esperaba un tipo de dato pero se encontró: '" + t.getValor() + "'");
    }

    private String consumirTipoOIdentificador() {
        Token t = tokenActual();
        if (TIPOS.contains(t.getTipo()) || t.getTipo() == TipoToken.IDENTIFICADOR
                || t.getTipo() == TipoToken.LISTA || t.getTipo() == TipoToken.JSN) {
            avanzar();
            return t.getValor();
        }
        throw error("Se esperaba un tipo pero se encontró: '" + t.getValor() + "'");
    }

    private boolean esOperadorAsignacionCompuesta() {
        TipoToken t = tokenActual().getTipo();
        return t == TipoToken.OP_SUMA_ASIG || t == TipoToken.OP_RESTA_ASIG
            || t == TipoToken.OP_MULT_ASIG || t == TipoToken.OP_DIV_ASIG
            || t == TipoToken.OP_MOD_ASIG;
    }

    private boolean verifica(TipoToken tipo) {
        return !finDelArchivo() && tokenActual().getTipo() == tipo;
    }

    private boolean verificaSiguiente(TipoToken tipo) {
        return pos + 1 < tokens.size() && tokens.get(pos + 1).getTipo() == tipo;
    }

    private boolean verificaSiguiente(TipoToken tipo, int offset) {
        return pos + offset < tokens.size() && tokens.get(pos + offset).getTipo() == tipo;
    }

    private boolean verificaAdelante(TipoToken tipo, int offset) {
        return pos + offset < tokens.size() && tokens.get(pos + offset).getTipo() == tipo;
    }

    private Token consumir(TipoToken tipo) {
        if (verifica(tipo)) return tokens.get(pos++); // aqui va atualizando la posicion del token que se esta consumiendo
        Token t = tokenActual();
        throw error("Se esperaba '" + tipo + "' pero se encontró '" + t.getValor()
                + "' (" + t.getTipo() + ")");
    }

    private void consumirOpcional(TipoToken tipo) {
        if (verifica(tipo)) pos++;
    }

    private Token actual() { return tokenActual(); }
    // si pos está dentro del límite-- devuelve ese token
    //   si pos se pasó del límite--- devuelve el último token (EOF)
    //math.min devuelve el número más pequeño entre los dos
    //por ejemplo si algo falla y poss llega a ser mayor por error que el tamaño de la lista de tokens, en vez de dar error por índice fuera de rango
    //devulve el numero mas pequeño entre pos y la size de la lista de tokens asi no da error

    private Token tokenActual() { return tokens.get(Math.min(pos, tokens.size() - 1)); }
    private void  avanzar() { if (!finDelArchivo()) pos++; }
    private boolean finDelArchivo() { return pos >= tokens.size() || tokenActual().getTipo() == TipoToken.EOF; }
    private int lineaActual() { return tokenActual().getLinea(); }

    /** ¿La siguiente línea no tiene más tokens de expresión? (para retornar sin valor) */
    private boolean finLinea() {
        return finDelArchivo()
            || verifica(TipoToken.LLAVE_DER)
            || verifica(TipoToken.SINO)
            || verifica(TipoToken.SINO_SI)
            || verifica(TipoToken.CAPTURAR)
            || verifica(TipoToken.FINALMENTE);
    }

    private ErrorSintactico error(String mensaje) {
        Token t = tokenActual();
        return new ErrorSintactico(mensaje, t.getLinea(), t.getColumna());
    }

    /** Recuperación de errores: avanza hasta el próximo token que pueda iniciar una sentencia */
    private void sincronizar() {
        while (!finDelArchivo()) {
            TipoToken t = tokenActual().getTipo();
            switch (t) {
                //estos son los casos de tokens seguros para reanudar el análisis sintáctico después de un error, tokens que pueden iniciar una nueva sentencia o bloque
                case ENTERO: case NUMERO: case TEXTO: case LOG: case VACIO:
                case LISTA: case JSN:
                case SI: case MIENTRAS: case PARA: case HACER:
                case RETORNAR: case OBJETO:
                case INTENTAR: case LANZAR:
                case IMPORTAR: case EXPORTAR:
                    return;
                default:
                    avanzar(); // aqui ignora los tokens que estan causando errores para no detener el analisis
            }
        }
    }

    /** Filtra tokens NUEVA_LINEA y COMENTARIO para simplificar el parser */
    //nueva linea es /n como no hay ; entonces ese salto de linea ayuda al lexico pero no es fundamental pera el sintactico
    private List<Token> filtrarLineas(List<Token> original) {
        List<Token> filtrados = new ArrayList<>();       // lista vacía nueva

        for (Token t : original) {                       // recorre cada token de la lista y lo asigna a t
            if (t.getTipo() != TipoToken.NUEVA_LINEA     // si NO es nueva línea
                    && t.getTipo() != TipoToken.COMENTARIO_LINEA  // Y NO es comentario //
                    && t.getTipo() != TipoToken.COMENTARIO_BLOQUE) { // Y NO es comentario /* */
                filtrados.add(t);                        // entonces sí lo agrega
            }
            // si es cualquiera de esos tres, lo ignora
        }

        return filtrados; // devuelve la lista limpia
    }


    /**
     * Consume el siguiente token como nombre de campo o método.
     * Acepta IDENTIFICADOR y también palabras reservadas usadas como
     * nombres de método, por ejemplo: entrada.numero(), lista.texto()
     */
    private String consumirIdentificadorOPalabraReservada() {
        Token t = tokenActual();
        switch (t.getTipo()) {
            case IDENTIFICADOR:
            case NUMERO:      // .numero()
            case TEXTO:       // .texto()
            case ENTERO:      // .entero()
            case LOG:         // .log()
            case LISTA:       // .lista()
                avanzar();
                return t.getValor();
            default:
                throw error("Se esperaba nombre de campo o método pero se encontró: '"
                        + t.getValor() + "'");
        }
    }
}
