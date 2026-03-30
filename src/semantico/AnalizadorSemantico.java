package semantico;

import sintactico.Nodo;
import sintactico.VisitanteNodo;
import sintactico.nodos.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnalizadorSemantico implements VisitanteNodo<String> {
    // ── Tipo especial para errores ────────────────────────────────────────
    private static final String TIPO_DESCONOCIDO = "desconocido";

    // ── Infraestructura ───────────────────────────────────────────────────
    private final TablaSimbolos tabla;
    private final List<ErrorSemantico> errores;

    /**
     * AST anotado: mapea cada nodo a su tipo inferido.
     * Es la segunda salida del semántico, usada por la fase de código intermedio.
     */
    private final Map<Nodo, String> tiposAnotados;

    // ── Estado de contexto durante el recorrido ───────────────────────────

    /** Tipo de retorno de la función que se está analizando actualmente. */
    private String tipoRetornoFuncionActual;

    /** Indica si actualmente se está dentro de al menos un bucle. */
    private int profundidadBucle;

    /** Indica si actualmente se está dentro de un objeto. */
    private String nombreObjetoActual;

    // ── Constructor ───────────────────────────────────────────────────────

    public AnalizadorSemantico() {
        this.tabla                    = new TablaSimbolos();
        this.errores                  = new ArrayList<>();
        this.tiposAnotados            = new HashMap<>();
        this.tipoRetornoFuncionActual  = null;
        this.profundidadBucle         = 0;
        this.nombreObjetoActual       = null;
    }

    // ── API pública ───────────────────────────────────────────────────────

    /**
     * Punto de entrada del análisis semántico.
     * Recibe el nodo raíz del AST producido por el AnalizadorSintactico.
     */
    public void analizar(NodoPrograma programa) {
        programa.aceptar(this);
    }

    public List<ErrorSemantico> getErrores()      { return errores; }
    public boolean hayErrores()                    { return !errores.isEmpty(); }
    public Map<Nodo, String> getTiposAnotados()   { return tiposAnotados; }
    public TablaSimbolos getTabla()               { return tabla; }

    // ── Utilidades internas ───────────────────────────────────────────────

    /**
     * Registra el tipo inferido de un nodo en el mapa de anotaciones
     * y lo retorna para que cada visitar* pueda hacer: return anotar(n, tipo).
     */
    private String anotar(Nodo n, String tipo) {
        tiposAnotados.put(n, tipo);
        return tipo;
    }

    private void registrarError(String mensaje, int linea) {
        errores.add(new ErrorSemantico(mensaje, linea));
    }

    /**
     * Verifica compatibilidad de tipos para asignación.
     * "nulo" es compatible con cualquier tipo según la documentación.
     * "entero" es compatible con "numero" por promoción.
     */
    private boolean sonCompatibles(String tipoEsperado, String tipoReal) {
        if (tipoReal.equals(TIPO_DESCONOCIDO))   return true; // ya se reportó el error antes
        if (tipoReal.equals("nulo"))              return true; // nulo es asignable a cualquier tipo
        if (tipoEsperado.equals(tipoReal))        return true;
        if (tipoEsperado.equals("numero") && tipoReal.equals("entero")) return true;
        return false;
    }

    // ── Programa ──────────────────────────────────────────────────────────

    @Override
    public String visitarPrograma(NodoPrograma n) {
        for (Nodo sentencia : n.sentencias) {
            sentencia.aceptar(this);
        }
        return anotar(n, TIPO_DESCONOCIDO);
    }

    // ── Declaraciones de variables ────────────────────────────────────────

    @Override
    public String visitarDeclaracionVariable(NodoDeclaracionVariable n) {
        // Verificar si ya existe en el ámbito actual
        if (tabla.buscarEnActual(n.nombre) != null) {
            registrarError(
                    "El símbolo '" + n.nombre + "' ya fue declarado en este ámbito.",
                    n.linea
            );
            return TIPO_DESCONOCIDO;
        }

        // Verificar valor inicial si existe
        if (n.valor != null) {
            String tipoValor = n.valor.aceptar(this);
            if (!sonCompatibles(n.tipo, tipoValor)) {
                registrarError(
                        "No se puede asignar un valor de tipo '" + tipoValor
                                + "' a la variable '" + n.nombre + "' de tipo '" + n.tipo + "'.",
                        n.linea
                );
            }
        }

        // Determinar categoría
        TipoSimbolo categoria = esNombreDeObjeto(n.tipo)
                ? TipoSimbolo.OBJETO_INSTANCIA
                : TipoSimbolo.VARIABLE;

        SimboloEntrada entrada = new SimboloEntrada(
                n.nombre, n.tipo, null, n.mutable, categoria, n.linea, null, null
        );
        tabla.declarar(entrada);
        return anotar(n, n.tipo);
    }

    @Override
    public String visitarDeclaracionLista(NodoDeclaracionLista n) {
        if (tabla.buscarEnActual(n.nombre) != null) {
            registrarError(
                    "El símbolo '" + n.nombre + "' ya fue declarado en este ámbito.",
                    n.linea
            );
            return TIPO_DESCONOCIDO;
        }

        if (n.valor != null) {
            String tipoValor = n.valor.aceptar(this);
            if (!tipoValor.equals(TIPO_DESCONOCIDO) && !tipoValor.equals("lista") && !tipoValor.equals("nulo")) {
                registrarError(
                        "Se esperaba una lista para '" + n.nombre + "' pero se encontró '" + tipoValor + "'.",
                        n.linea
                );
            }
        }

        SimboloEntrada entrada = new SimboloEntrada(
                n.nombre, "lista", n.tipoElemento, n.mutable,
                TipoSimbolo.VARIABLE, n.linea, null, null
        );
        tabla.declarar(entrada);
        return anotar(n, "lista");
    }

    @Override
    public String visitarDeclaracionJsn(NodoDeclaracionJsn n) {
        if (tabla.buscarEnActual(n.nombre) != null) {
            registrarError(
                    "El símbolo '" + n.nombre + "' ya fue declarado en este ámbito.",
                    n.linea
            );
            return TIPO_DESCONOCIDO;
        }

        if (n.valor != null) {
            String tipoValor = n.valor.aceptar(this);
            if (!tipoValor.equals(TIPO_DESCONOCIDO) && !tipoValor.equals("jsn") && !tipoValor.equals("nulo")) {
                registrarError(
                        "Se esperaba un jsn para '" + n.nombre + "' pero se encontró '" + tipoValor + "'.",
                        n.linea
                );
            }
        }

        SimboloEntrada entrada = new SimboloEntrada(
                n.nombre, "jsn", null, n.mutable,
                TipoSimbolo.VARIABLE, n.linea, null, null
        );
        tabla.declarar(entrada);
        return anotar(n, "jsn");
    }

    // ── Asignación ────────────────────────────────────────────────────────

    @Override
    public String visitarAsignacion(NodoAsignacion n) {
        String tipoObjetivo = n.objetivo.aceptar(this);
        String tipoValor    = n.valor.aceptar(this);

        // Verificar mutabilidad si el objetivo es un identificador
        if (n.objetivo instanceof NodoIdentificador) {
            String nombre = ((NodoIdentificador) n.objetivo).nombre;
            SimboloEntrada simbolo = tabla.buscar(nombre);
            if (simbolo != null && !simbolo.esMutable()) {
                registrarError(
                        "La variable '" + nombre + "' es inmutable. "
                                + "Usa 'var' en su declaración para permitir reasignación.",
                        n.linea
                );
            }
        }

        // Verificar compatibilidad de tipos
        if (!sonCompatibles(tipoObjetivo, tipoValor)) {
            registrarError(
                    "No se puede asignar un valor de tipo '" + tipoValor
                            + "' a una variable de tipo '" + tipoObjetivo + "'.",
                    n.linea
            );
        }

        return anotar(n, tipoObjetivo);
    }

    @Override
    public String visitarAsignacionCompuesta(NodoAsignacionCompuesta n) {
        String tipoObjetivo = n.objetivo.aceptar(this);
        String tipoValor    = n.valor.aceptar(this);

        // Verificar mutabilidad
        if (n.objetivo instanceof NodoIdentificador) {
            String nombre = ((NodoIdentificador) n.objetivo).nombre;
            SimboloEntrada simbolo = tabla.buscar(nombre);
            if (simbolo != null && !simbolo.esMutable()) {
                registrarError(
                        "La variable '" + nombre + "' es inmutable. "
                                + "Usa 'var' para permitir operaciones de asignación compuesta.",
                        n.linea
                );
            }
        }

        // Verificar que los tipos soporten la operación
        if (!sonCompatibles(tipoObjetivo, tipoValor)) {
            registrarError(
                    "Operación '" + n.operador + "' no compatible entre '"
                            + tipoObjetivo + "' y '" + tipoValor + "'.",
                    n.linea
            );
        }

        return anotar(n, tipoObjetivo);
    }

    @Override
    public String visitarIncrementoDecremento(NodoIncrementoDecremento n) {
        String tipo = n.objetivo.aceptar(this);

        // Solo entero y numero permiten ++ y --
        if (!tipo.equals("entero") && !tipo.equals("numero") && !tipo.equals(TIPO_DESCONOCIDO)) {
            registrarError(
                    "El operador '" + n.operador + "' solo aplica a tipos numéricos, "
                            + "pero se encontró '" + tipo + "'.",
                    n.linea
            );
        }

        // Verificar mutabilidad
        if (n.objetivo instanceof NodoIdentificador) {
            String nombre = ((NodoIdentificador) n.objetivo).nombre;
            SimboloEntrada simbolo = tabla.buscar(nombre);
            if (simbolo != null && !simbolo.esMutable()) {
                registrarError(
                        "La variable '" + nombre + "' es inmutable. "
                                + "Usa 'var' para permitir '" + n.operador + "'.",
                        n.linea
                );
            }
        }

        return anotar(n, tipo);
    }

    // ── Bloque ────────────────────────────────────────────────────────────

    @Override
    public String visitarBloque(NodoBloque n) {
        for (Nodo sentencia : n.sentencias) {
            sentencia.aceptar(this);
        }
        return TIPO_DESCONOCIDO;
    }

    // ── Control de flujo ──────────────────────────────────────────────────

    @Override
    public String visitarSi(NodoSi n) {
        String tipoCond = n.condicion.aceptar(this);
        if (!tipoCond.equals("log") && !tipoCond.equals(TIPO_DESCONOCIDO)) {
            registrarError(
                    "La condición del 'si' debe ser de tipo 'log' pero se encontró '" + tipoCond + "'.",
                    n.linea
            );
        }

        tabla.entrarAmbito("si:linea" + n.linea);
        n.cuerpoIf.aceptar(this);
        tabla.salirAmbito();

        for (NodoSi.RamaElseIf rama : n.ramasElseIf) {
            String tipoRama = rama.condicion.aceptar(this);
            if (!tipoRama.equals("log") && !tipoRama.equals(TIPO_DESCONOCIDO)) {
                registrarError(
                        "La condición del 'sino_si' debe ser de tipo 'log' pero se encontró '" + tipoRama + "'.",
                        n.linea
                );
            }
            tabla.entrarAmbito("sino_si:linea" + n.linea);
            rama.cuerpo.aceptar(this);
            tabla.salirAmbito();
        }

        if (n.cuerpoElse != null) {
            tabla.entrarAmbito("sino:linea" + n.linea);
            n.cuerpoElse.aceptar(this);
            tabla.salirAmbito();
        }

        return TIPO_DESCONOCIDO;
    }

    @Override
    public String visitarMientras(NodoMientras n) {
        String tipoCond = n.condicion.aceptar(this);
        if (!tipoCond.equals("log") && !tipoCond.equals(TIPO_DESCONOCIDO)) {
            registrarError(
                    "La condición del 'mientras' debe ser de tipo 'log' pero se encontró '" + tipoCond + "'.",
                    n.linea
            );
        }

        profundidadBucle++;
        tabla.entrarAmbito("mientras:linea" + n.linea);
        n.cuerpo.aceptar(this);
        tabla.salirAmbito();
        profundidadBucle--;

        return TIPO_DESCONOCIDO;
    }

    @Override
    public String visitarHacer(NodoHacer n) {
        profundidadBucle++;
        tabla.entrarAmbito("hacer:linea" + n.linea);
        n.cuerpo.aceptar(this);
        tabla.salirAmbito();
        profundidadBucle--;

        String tipoCond = n.condicion.aceptar(this);
        if (!tipoCond.equals("log") && !tipoCond.equals(TIPO_DESCONOCIDO)) {
            registrarError(
                    "La condición del 'hacer...mientras' debe ser de tipo 'log' pero se encontró '" + tipoCond + "'.",
                    n.linea
            );
        }

        return TIPO_DESCONOCIDO;
    }

    @Override
    public String visitarPara(NodoPara n) {
        tabla.entrarAmbito("para:linea" + n.linea);

        if (n.init != null)      n.init.aceptar(this);
        if (n.condicion != null) {
            String tipoCond = n.condicion.aceptar(this);
            if (!tipoCond.equals("log") && !tipoCond.equals(TIPO_DESCONOCIDO)) {
                registrarError(
                        "La condición del 'para' debe ser de tipo 'log' pero se encontró '" + tipoCond + "'.",
                        n.linea
                );
            }
        }
        if (n.paso != null) n.paso.aceptar(this);

        profundidadBucle++;
        n.cuerpo.aceptar(this);
        profundidadBucle--;

        tabla.salirAmbito();
        return TIPO_DESCONOCIDO;
    }

    @Override
    public String visitarParaEn(NodoParaEn n) {
        String tipoColeccion = n.coleccion.aceptar(this);
        if (!tipoColeccion.equals("lista") && !tipoColeccion.equals("jsn")
                && !tipoColeccion.equals(TIPO_DESCONOCIDO)) {
            registrarError(
                    "El 'para...en' requiere una lista o jsn, pero se encontró '" + tipoColeccion + "'.",
                    n.linea
            );
        }

        tabla.entrarAmbito("para_en:linea" + n.linea);

        // Registrar la variable de iteración
        SimboloEntrada varIteracion = new SimboloEntrada(
                n.variable, "desconocido", null, false,
                TipoSimbolo.VARIABLE, n.linea, null, null
        );
        tabla.declarar(varIteracion);

        profundidadBucle++;
        n.cuerpo.aceptar(this);
        profundidadBucle--;

        tabla.salirAmbito();
        return TIPO_DESCONOCIDO;
    }

    @Override
    public String visitarRetornar(NodoRetornar n) {
        if (tipoRetornoFuncionActual == null) {
            registrarError(
                    "'retornar' usado fuera de una función.",
                    n.linea
            );
            return TIPO_DESCONOCIDO;
        }

        if (n.valor == null) {
            // retornar sin valor solo es válido en funciones vacio
            if (!tipoRetornoFuncionActual.equals("vacio")) {
                registrarError(
                        "La función debe retornar un valor de tipo '"
                                + tipoRetornoFuncionActual + "' pero se encontró 'retornar' sin valor.",
                        n.linea
                );
            }
            return anotar(n, "vacio");
        }

        String tipoValor = n.valor.aceptar(this);
        if (!sonCompatibles(tipoRetornoFuncionActual, tipoValor)) {
            registrarError(
                    "La función debe retornar '" + tipoRetornoFuncionActual
                            + "' pero se encontró '" + tipoValor + "'.",
                    n.linea
            );
        }

        return anotar(n, tipoValor);
    }

    @Override
    public String visitarRomper(NodoRomper n) {
        if (profundidadBucle == 0) {
            registrarError(
                    "'romper' usado fuera de un bucle.",
                    n.linea
            );
        }
        return TIPO_DESCONOCIDO;
    }

    @Override
    public String visitarContinuar(NodoContinuar n) {
        if (profundidadBucle == 0) {
            registrarError(
                    "'continuar' usado fuera de un bucle.",
                    n.linea
            );
        }
        return TIPO_DESCONOCIDO;
    }

    // ── Funciones ─────────────────────────────────────────────────────────

    @Override
    public String visitarDeclaracionFuncion(NodoDeclaracionFuncion n) {
        // Verificar redeclaración en ámbito actual
        if (tabla.buscarEnActual(n.nombre) != null) {
            registrarError(
                    "La función '" + n.nombre + "' ya fue declarada en este ámbito.",
                    n.linea
            );
            return TIPO_DESCONOCIDO;
        }

        // Construir lista de parámetros como SimboloEntrada
        List<SimboloEntrada> params = new ArrayList<>();
        for (NodoDeclaracionFuncion.Parametro p : n.parametros) {
            params.add(new SimboloEntrada(
                    p.nombre, p.tipo, null, false,
                    TipoSimbolo.PARAMETRO, n.linea, null, null
            ));
        }

        // Registrar la función en el ámbito actual
        SimboloEntrada entradaFuncion = new SimboloEntrada(
                n.nombre, n.tipoRetorno, null, false,
                TipoSimbolo.FUNCION, n.linea, params, n.tipoRetorno
        );
        tabla.declarar(entradaFuncion);

        // Entrar al ámbito de la función
        String tipoRetornoAnterior = tipoRetornoFuncionActual;
        tipoRetornoFuncionActual = n.tipoRetorno;
        tabla.entrarAmbito("funcion:" + n.nombre);

        // Registrar parámetros dentro del ámbito de la función
        for (SimboloEntrada param : params) {
            tabla.declarar(param);
        }

        // Analizar el cuerpo
        n.cuerpo.aceptar(this);

        tabla.salirAmbito();
        tipoRetornoFuncionActual = tipoRetornoAnterior;

        return anotar(n, n.tipoRetorno);
    }

    @Override
    public String visitarLlamadaFuncion(NodoLlamadaFuncion n) {
        SimboloEntrada simbolo = tabla.buscar(n.nombre);

        if (simbolo == null) {
            registrarError(
                    "La función '" + n.nombre + "' no está declarada.",
                    n.linea
            );
            return TIPO_DESCONOCIDO;
        }

        if (!simbolo.esFuncion()) {
            registrarError(
                    "'" + n.nombre + "' no es una función.",
                    n.linea
            );
            return TIPO_DESCONOCIDO;
        }

        // Verificar número de argumentos
        int esperados = simbolo.getParametros() != null ? simbolo.getParametros().size() : 0;
        int recibidos = n.argumentos.size();
        if (esperados != recibidos) {
            registrarError(
                    "La función '" + n.nombre + "' espera " + esperados
                            + " argumento(s) pero se encontraron " + recibidos + ".",
                    n.linea
            );
        }

        // Verificar tipos de argumentos
        int limite = Math.min(esperados, recibidos);
        for (int i = 0; i < limite; i++) {
            String tipoArg   = n.argumentos.get(i).aceptar(this);
            String tipoParam = simbolo.getParametros().get(i).getTipo();
            if (!sonCompatibles(tipoParam, tipoArg)) {
                registrarError(
                        "El argumento " + (i + 1) + " de '" + n.nombre
                                + "' debe ser de tipo '" + tipoParam
                                + "' pero se encontró '" + tipoArg + "'.",
                        n.linea
                );
            }
        }

        return anotar(n, simbolo.getTipoRetorno() != null ? simbolo.getTipoRetorno() : TIPO_DESCONOCIDO);
    }

    @Override
    public String visitarLlamadaMetodo(NodoLlamadaMetodo n) {
        // Analizar el objeto receptor para verificar que existe
        n.objeto.aceptar(this);
        // Analizar los argumentos
        for (Nodo arg : n.argumentos) {
            arg.aceptar(this);
        }
        // El tipo de retorno de un método no se puede determinar sin
        // información completa de la clase — se retorna desconocido
        // para no bloquear el análisis
        return TIPO_DESCONOCIDO;
    }

    // ── Objetos ───────────────────────────────────────────────────────────

    @Override
    public String visitarDeclaracionObjeto(NodoDeclaracionObjeto n) {
        if (tabla.buscarEnActual(n.nombre) != null) {
            registrarError(
                    "El objeto '" + n.nombre + "' ya fue declarado en este ámbito.",
                    n.linea
            );
            return TIPO_DESCONOCIDO;
        }

        // Registrar el tipo del objeto en el ámbito global
        SimboloEntrada entradaObjeto = new SimboloEntrada(
                n.nombre, n.nombre, null, false,
                TipoSimbolo.OBJETO_TIPO, n.linea, null, null
        );
        tabla.declarar(entradaObjeto);

        // Entrar al ámbito del objeto
        String objetoAnterior = nombreObjetoActual;
        nombreObjetoActual = n.nombre;
        tabla.entrarAmbito("objeto:" + n.nombre);

        // Registrar atributos privados
        for (NodoDeclaracionVariable attr : n.atributosPrivados) {
            attr.aceptar(this);
        }

        // Registrar atributos públicos
        for (NodoDeclaracionVariable attr : n.atributosPublicos) {
            attr.aceptar(this);
        }

        // Analizar constructor
        if (n.constructor != null) {
            n.constructor.aceptar(this);
        }

        // Analizar métodos públicos
        for (NodoDeclaracionFuncion metodo : n.metodosPublicos) {
            metodo.aceptar(this);
        }

        // Analizar métodos privados
        for (NodoDeclaracionFuncion metodo : n.metodosPrivados) {
            metodo.aceptar(this);
        }

        tabla.salirAmbito();
        nombreObjetoActual = objetoAnterior;

        return n.nombre;
    }

    @Override
    public String visitarNuevoObjeto(NodoNuevoObjeto n) {
        SimboloEntrada simbolo = tabla.buscar(n.tipoObjeto);
        if (simbolo == null || simbolo.getTipoSimbolo() != TipoSimbolo.OBJETO_TIPO) {
            registrarError(
                    "El tipo de objeto '" + n.tipoObjeto + "' no está declarado.",
                    n.linea
            );
            return TIPO_DESCONOCIDO;
        }

        // Analizar los argumentos del constructor
        for (Nodo arg : n.argumentos) {
            arg.aceptar(this);
        }

        return anotar(n, n.tipoObjeto);
    }

    @Override
    public String visitarAccesoMiembro(NodoAccesoMiembro n) {
        // Verificar que el objeto existe
        n.objeto.aceptar(this);
        // El tipo del miembro no se puede resolver sin tabla de miembros completa
        return TIPO_DESCONOCIDO;
    }

    @Override
    public String visitarAccesoIndice(NodoAccesoIndice n) {
        String tipoColeccion = n.coleccion.aceptar(this);
        String tipoIndice    = n.indice.aceptar(this);

        if (!tipoColeccion.equals("lista") && !tipoColeccion.equals("jsn")
                && !tipoColeccion.equals("texto") && !tipoColeccion.equals(TIPO_DESCONOCIDO)) {
            registrarError(
                    "El acceso por índice solo aplica a lista, jsn o texto, "
                            + "pero se encontró '" + tipoColeccion + "'.",
                    n.linea
            );
        }

        if (!tipoIndice.equals("entero") && !tipoIndice.equals(TIPO_DESCONOCIDO)) {
            registrarError(
                    "El índice de acceso debe ser de tipo 'entero' pero se encontró '"
                            + tipoIndice + "'.",
                    n.linea
            );
        }

        return TIPO_DESCONOCIDO;
    }

    // ── Manejo de errores ─────────────────────────────────────────────────

    @Override
    public String visitarIntentar(NodoIntentar n) {
        tabla.entrarAmbito("intentar:linea" + n.linea);
        n.cuerpoIntentar.aceptar(this);
        tabla.salirAmbito();

        if (n.cuerpoCapturar != null) {
            tabla.entrarAmbito("capturar:linea" + n.linea);
            // Registrar la variable de excepción
            if (n.variableError != null) {
                SimboloEntrada varError = new SimboloEntrada(
                        n.variableError, "excepcion", null, false,
                        TipoSimbolo.VARIABLE, n.linea, null, null
                );
                tabla.declarar(varError);
            }
            n.cuerpoCapturar.aceptar(this);
            tabla.salirAmbito();
        }

        if (n.cuerpoFinalmente != null) {
            tabla.entrarAmbito("finalmente:linea" + n.linea);
            n.cuerpoFinalmente.aceptar(this);
            tabla.salirAmbito();
        }

        return TIPO_DESCONOCIDO;
    }

    @Override
    public String visitarLanzar(NodoLanzar n) {
        n.expresion.aceptar(this);
        return TIPO_DESCONOCIDO;
    }

    // ── Módulos ───────────────────────────────────────────────────────────

    @Override
    public String visitarImportar(NodoImportar n) {
        // Registrar los símbolos importados en el ámbito global
        // con tipo desconocido ya que no se analiza el archivo externo
        for (String simbolo : n.simbolos) {
            if (tabla.buscarEnActual(simbolo) == null) {
                SimboloEntrada entrada = new SimboloEntrada(
                        simbolo, TIPO_DESCONOCIDO, null, false,
                        TipoSimbolo.VARIABLE, n.linea, null, null
                );
                tabla.declarar(entrada);
            }
        }
        return TIPO_DESCONOCIDO;
    }

    @Override
    public String visitarExportar(NodoExportar n) {
        // Verificar que cada símbolo exportado exista
        for (String simbolo : n.simbolos) {
            if (!tabla.existe(simbolo)) {
                registrarError(
                        "El símbolo '" + simbolo + "' no está declarado y no puede exportarse.",
                        n.linea
                );
            }
        }
        return TIPO_DESCONOCIDO;
    }

    // ── Expresiones binarias y unarias ────────────────────────────────────

    @Override
    public String visitarBinaria(NodoBinaria n) {
        String tipoIzq = n.izquierda.aceptar(this);
        String tipoDer = n.derecha.aceptar(this);

        // Si alguno es desconocido, no seguir generando errores en cascada
        if (tipoIzq.equals(TIPO_DESCONOCIDO) || tipoDer.equals(TIPO_DESCONOCIDO)) {
            return TIPO_DESCONOCIDO;
        }

        switch (n.operador) {
            // Operadores aritméticos
            case "+":
                if (tipoIzq.equals("texto") && tipoDer.equals("texto")) return anotar(n, "texto");
                if (esNumerico(tipoIzq) && esNumerico(tipoDer)) return anotar(n, tipoResultanteNumerico(tipoIzq, tipoDer));
                registrarError(
                        "El operador '+' no aplica entre '" + tipoIzq + "' y '" + tipoDer + "'.",
                        n.linea
                );
                return TIPO_DESCONOCIDO;

            case "-": case "*": case "/": case "%":
                if (esNumerico(tipoIzq) && esNumerico(tipoDer)) return anotar(n, tipoResultanteNumerico(tipoIzq, tipoDer));
                registrarError(
                        "El operador '" + n.operador + "' solo aplica a tipos numéricos, "
                                + "pero se encontró '" + tipoIzq + "' y '" + tipoDer + "'.",
                        n.linea
                );
                return TIPO_DESCONOCIDO;

            // Operadores relacionales
            case "==": case "!=":
                return anotar(n, "log");

            case "<": case ">": case "<=": case ">=":
                if (esNumerico(tipoIzq) && esNumerico(tipoDer)) return anotar(n, "log");
                if (tipoIzq.equals("texto") && tipoDer.equals("texto")) return anotar(n, "log");
                registrarError(
                        "El operador '" + n.operador + "' no aplica entre '"
                                + tipoIzq + "' y '" + tipoDer + "'.",
                        n.linea
                );
                return TIPO_DESCONOCIDO;

            // Operadores lógicos
            case "y": case "&&":
            case "o": case "||":
                if (!tipoIzq.equals("log")) {
                    registrarError(
                            "El operador '" + n.operador + "' requiere 'log' en el lado izquierdo, "
                                    + "pero se encontró '" + tipoIzq + "'.",
                            n.linea
                    );
                }
                if (!tipoDer.equals("log")) {
                    registrarError(
                            "El operador '" + n.operador + "' requiere 'log' en el lado derecho, "
                                    + "pero se encontró '" + tipoDer + "'.",
                            n.linea
                    );
                }
                return anotar(n, "log");

            default:
                return anotar(n, TIPO_DESCONOCIDO);
        }
    }

    @Override
    public String visitarUnaria(NodoUnaria n) {
        String tipoOperando = n.operando.aceptar(this);

        switch (n.operador) {
            case "-":
                if (!esNumerico(tipoOperando) && !tipoOperando.equals(TIPO_DESCONOCIDO)) {
                    registrarError(
                            "El operador '-' unario solo aplica a tipos numéricos, "
                                    + "pero se encontró '" + tipoOperando + "'.",
                            n.linea
                    );
                }
                return anotar(n, tipoOperando);

            case "!": case "no":
                if (!tipoOperando.equals("log") && !tipoOperando.equals(TIPO_DESCONOCIDO)) {
                    registrarError(
                            "El operador '" + n.operador + "' solo aplica a 'log', "
                                    + "pero se encontró '" + tipoOperando + "'.",
                            n.linea
                    );
                }
                return anotar(n, "log");

            case "esperar":
                // esperar está reservado pero no implementado en v0.0.12
                return anotar(n, tipoOperando);

            default:
                return anotar(n, TIPO_DESCONOCIDO);
        }
    }

    @Override
    public String visitarTernaria(NodoTernaria n) {
        String tipoCond = n.condicion.aceptar(this);
        if (!tipoCond.equals("log") && !tipoCond.equals(TIPO_DESCONOCIDO)) {
            registrarError(
                    "La condición del operador ternario debe ser 'log' pero se encontró '"
                            + tipoCond + "'.",
                    n.linea
            );
        }

        String tipoVerdadero = n.siVerdadero.aceptar(this);
        String tipoFalso     = n.siFalso.aceptar(this);

        // Retornar el tipo de la rama verdadera si son compatibles
        if (sonCompatibles(tipoVerdadero, tipoFalso)) return anotar(n, tipoVerdadero);
        if (sonCompatibles(tipoFalso, tipoVerdadero)) return anotar(n, tipoFalso);

        return anotar(n, TIPO_DESCONOCIDO);
    }

    // ── Literales ─────────────────────────────────────────────────────────

    @Override public String visitarLiteralEntero(NodoLiteralEntero n)   { return anotar(n, "entero"); }
    @Override public String visitarLiteralReal(NodoLiteralReal n)       { return anotar(n, "numero"); }
    @Override public String visitarLiteralTexto(NodoLiteralTexto n)     { return anotar(n, "texto");  }
    @Override public String visitarLiteralTextoInterp(NodoLiteralTextoInterp n) { return anotar(n, "texto");  }
    @Override public String visitarLiteralLog(NodoLiteralLog n)         { return anotar(n, "log");    }
    @Override public String visitarLiteralNulo(NodoLiteralNulo n)       { return anotar(n, "nulo");   }
    @Override public String visitarLiteralLista(NodoLiteralLista n) {
        for (Nodo elemento : n.elementos) {
            elemento.aceptar(this);
        }
        return anotar(n, "lista");
    }
    @Override public String visitarLiteralJsn(NodoLiteralJsn n) {
        for (Nodo valor : n.pares.values()) {
            valor.aceptar(this);
        }
        return anotar(n, "jsn");
    }

    // ── Identificador y consola ───────────────────────────────────────────

    @Override
    public String visitarIdentificador(NodoIdentificador n) {
        // ambiente es válido solo dentro de un objeto
        if (n.nombre.equals("ambiente")) {
            if (nombreObjetoActual == null) {
                registrarError(
                        "'ambiente' solo puede usarse dentro de un objeto.",
                        n.linea
                );
                return TIPO_DESCONOCIDO;
            }
            return anotar(n, nombreObjetoActual);
        }

        SimboloEntrada simbolo = tabla.buscar(n.nombre);
        if (simbolo == null) {
            registrarError(
                    "El símbolo '" + n.nombre + "' no está declarado.",
                    n.linea
            );
            return TIPO_DESCONOCIDO;
        }

        return anotar(n, simbolo.getTipo());
    }

    @Override
    public String visitarConsola(NodoConsola n) {
        // Verificar los argumentos de la llamada a consola
        for (Nodo arg : n.argumentos) {
            arg.aceptar(this);
        }
        // Métodos de consola que retornan un valor según la documentación de Quetzal:
        // pedir()        → texto  (lee entrada del usuario como texto)
        // pedir_entero() → entero
        // pedir_numero() → numero
        // Todos los demás (mostrar, mostrar_error, etc.) → vacio
        switch (n.metodo) {
            case "pedir":         return anotar(n, "texto");
            case "pedir_entero":  return anotar(n, "entero");
            case "pedir_numero":  return anotar(n, "numero");
            default:              return anotar(n, "vacio");
        }
    }

    // ── Utilidades de tipos ───────────────────────────────────────────────

    /** Indica si un tipo es numérico (entero o numero). */
    private boolean esNumerico(String tipo) {
        return tipo.equals("entero") || tipo.equals("numero");
    }

    /**
     * Determina el tipo resultante de una operación entre dos tipos numéricos.
     * Si alguno es numero el resultado es numero, de lo contrario entero.
     */
    private String tipoResultanteNumerico(String tipoIzq, String tipoDer) {
        if (tipoIzq.equals("numero") || tipoDer.equals("numero")) return "numero";
        return "entero";
    }

    /**
     * Heurística: un tipo es nombre de objeto si empieza con mayúscula.
     * Consistente con la heurística del AnalizadorSintactico existente.
     */
    private boolean esNombreDeObjeto(String tipo) {
        if (tipo == null || tipo.isEmpty()) return false;
        return Character.isUpperCase(tipo.charAt(0));
    }
}
