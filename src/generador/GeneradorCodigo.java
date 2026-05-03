package generador;

import sintactico.Nodo;
import sintactico.VisitanteNodo;
import sintactico.nodos.*;

import java.util.List;
import java.util.Map;
/**
 * Fase 4: Generador de Código Java.
 */
public class GeneradorCodigo implements VisitanteNodo<String> {

    // Nivel de indentación actual
    private int nivelIndentacion = 0;

    // Indica si el programa necesita Scanner para consola.pedir()
    private boolean necesitaScanner = false;

    //Helpers de indentación

    private String indentar() {
        String resultado = "";
        for (int i = 0; i < nivelIndentacion; i++) {
            resultado += "    ";
        }
        return resultado;
    }

    private void subirNivel() { nivelIndentacion++; }
    private void bajarNivel() { nivelIndentacion--; }

    //Traducción de tipos Quetzal - Java

    private String traducirTipo(String tipoQuetzal) {
        switch (tipoQuetzal) {
            case "entero": return "int";
            case "numero": return "double";
            case "texto":  return "String";
            case "log":    return "boolean";
            case "vacio":  return "void";
            case "lista":  return "ArrayList";
            case "jsn":    return "HashMap";
            default:       return tipoQuetzal;
        }
    }

    private String traducirTipoLista(String tipoElemento) {
        if (tipoElemento == null) return "ArrayList<Object>";
        return "ArrayList<" + traducirTipoElemento(tipoElemento) + ">";
    }

    private String traducirTipoElemento(String tipo) {
        switch (tipo) {
            case "entero": return "Integer";
            case "numero": return "Double";
            case "texto":  return "String";
            case "log":    return "Boolean";
            default:
                if (tipo.startsWith("lista<")) {
                    String interno = tipo.substring(6, tipo.length() - 1);
                    return "ArrayList<" + traducirTipoElemento(interno) + ">";
                }
                return "Object";
        }
    }

    // Programa

    @Override
    public String visitarPrograma(NodoPrograma n) {
        // Primera pasada: detectar si necesita Scanner
        for (int i = 0; i < n.sentencias.size(); i++) {
            if (necesitaScanner(n.sentencias.get(i))) {
                necesitaScanner = true;
                break;
            }
        }

        StringBuilder miembrosClase = new StringBuilder(); // funciones y objetos
        StringBuilder cuerpoMain    = new StringBuilder(); // sentencias ejecutables

        // Separar: funciones/objetos van fuera de main; el resto va dentro
        nivelIndentacion = 1; // nivel clase
        for (int i = 0; i < n.sentencias.size(); i++) {
            Nodo s = n.sentencias.get(i);
            if (s instanceof NodoDeclaracionFuncion || s instanceof NodoDeclaracionObjeto) {
                String codigo = s.aceptar(this);
                if (codigo != null && !codigo.isEmpty()) {
                    miembrosClase.append(codigo).append("\n\n");
                }
            }
        }

        nivelIndentacion = 2; // nivel main (clase + método)
        for (int i = 0; i < n.sentencias.size(); i++) {
            Nodo s = n.sentencias.get(i);
            if (!(s instanceof NodoDeclaracionFuncion) && !(s instanceof NodoDeclaracionObjeto)) {
                String codigo = s.aceptar(this);
                if (codigo != null && !codigo.isEmpty()) {
                    cuerpoMain.append(codigo).append("\n");
                }
            }
        }

        // Ensamblar el archivo Java final
        StringBuilder sb = new StringBuilder();
        sb.append("import java.util.Scanner;\n");
        sb.append("import java.util.ArrayList;\n");
        sb.append("import java.util.HashMap;\n");
        sb.append("\n");
        sb.append("public class ProgramaQuetzal {\n");
        sb.append("\n");

        // Métodos estáticos y clases anidadas FUERA de main
        if (miembrosClase.length() > 0) {
            sb.append(miembrosClase);
        }

        // Método main
        sb.append("    public static void main(String[] args) {\n");
        if (necesitaScanner) {
            sb.append("        Scanner scanner = new Scanner(System.in);\n");
        }
        sb.append("\n");
        sb.append(cuerpoMain);
        if (necesitaScanner) {
            sb.append("        scanner.close();\n");
        }
        sb.append("    }\n");
        sb.append("}\n");

        return sb.toString();
    }

    //Declaraciones de variables

    @Override
    public String visitarDeclaracionVariable(NodoDeclaracionVariable n) {

        if (n.tipo.startsWith("lista<") && n.valor != null
                && !(n.valor instanceof NodoLiteralLista)) {
            String tipoInterno = n.tipo.substring(6, n.tipo.length() - 1);
            String tipoJava = traducirTipoLista(tipoInterno);
            return indentar() + tipoJava + " " + n.nombre
                    + " = " + n.valor.aceptar(this) + ";";
        }
        String tipoJava = traducirTipo(n.tipo);
        String prompt   = "";
        String valor    = "";


        if (n.valor instanceof NodoConsola) {
            NodoConsola consola = (NodoConsola) n.valor;
            if (consola.metodo.equals("pedir") && consola.argumentos.size() > 0) {
                prompt = indentar() + "System.out.print("
                        + consola.argumentos.get(0).aceptar(this) + ");\n";
                valor  = " = scanner.nextLine()";
            } else {
                valor = " = " + n.valor.aceptar(this);
            }
        } else {
            valor = n.valor != null ? " = " + n.valor.aceptar(this) : "";
        }

        return prompt + indentar() + tipoJava + " " + n.nombre + valor + ";";
    }

    @Override
    public String visitarDeclaracionLista(NodoDeclaracionLista n) {

        if (n.valor != null && !(n.valor instanceof NodoLiteralLista)) {
            String tipoJava = traducirTipoLista(n.tipoElemento);
            return indentar() + tipoJava + " " + n.nombre
                    + " = " + n.valor.aceptar(this) + ";";
        }

        String tipoJava = traducirTipoLista(n.tipoElemento);
        StringBuilder sb = new StringBuilder();
        sb.append(indentar()).append(tipoJava).append(" ")
                .append(n.nombre).append(" = new ArrayList<>();");

        if (n.valor instanceof NodoLiteralLista) {
            NodoLiteralLista lista = (NodoLiteralLista) n.valor;
            for (int i = 0; i < lista.elementos.size(); i++) {
                Nodo elem = lista.elementos.get(i);

                // Si el elemento es otra lista - crear sublista y agregarla
                if (elem instanceof NodoLiteralLista) {
                    String nombreSub = n.nombre + "_fila" + i;
                    String tipoInterno = n.tipoElemento != null
                            && n.tipoElemento.startsWith("lista<")
                            ? traducirTipoLista(n.tipoElemento.substring(6, n.tipoElemento.length() - 1))
                            : "ArrayList<Object>";

                    sb.append("\n").append(indentar())
                            .append(tipoInterno).append(" ").append(nombreSub)
                            .append(" = new ArrayList<>();");

                    NodoLiteralLista subLista = (NodoLiteralLista) elem;
                    for (int j = 0; j < subLista.elementos.size(); j++) {
                        String elemVal = subLista.elementos.get(j).aceptar(this);
                        sb.append("\n").append(indentar())
                                .append(nombreSub).append(".add(").append(elemVal).append(");");
                    }
                    sb.append("\n").append(indentar())
                            .append(n.nombre).append(".add(").append(nombreSub).append(");");

                } else {
                    // Elemento simple
                    String elemVal = elem.aceptar(this);
                    sb.append("\n").append(indentar())
                            .append(n.nombre).append(".add(").append(elemVal).append(");");
                }
            }
        }
        return sb.toString();
    }

    @Override
    public String visitarDeclaracionJsn(NodoDeclaracionJsn n) {
        String valor = n.valor != null
                ? " = " + n.valor.aceptar(this)
                : " = new HashMap<String, Object>()";
        return indentar() + "HashMap<String, Object> " + n.nombre + valor + ";";
    }

    // Asignaciones

    @Override
    public String visitarAsignacion(NodoAsignacion n) {
        // Si el valor es consola.pedir(), agregar el prompt antes
        if (n.valor instanceof NodoConsola) {
            NodoConsola consola = (NodoConsola) n.valor;
            if (consola.metodo.equals("pedir") && consola.argumentos.size() > 0) {
                String prompt = indentar() + "System.out.print("
                        + consola.argumentos.get(0).aceptar(this) + ");\n";
                return prompt + indentar()
                        + n.objetivo.aceptar(this) + " = scanner.nextLine();";
            }
        }
        return indentar() + n.objetivo.aceptar(this)
                + " = " + n.valor.aceptar(this) + ";";
    }

    @Override
    public String visitarAsignacionCompuesta(NodoAsignacionCompuesta n) {
        return indentar() + n.objetivo.aceptar(this)
                + " " + n.operador + " " + n.valor.aceptar(this) + ";";
    }

    @Override
    public String visitarIncrementoDecremento(NodoIncrementoDecremento n) {
        return indentar() + n.objetivo.aceptar(this) + n.operador + ";";
    }

    //Bloque

    @Override
    public String visitarBloque(NodoBloque n) {
        StringBuilder sb = new StringBuilder();
        sb.append(" {\n");
        subirNivel();
        for (int i = 0; i < n.sentencias.size(); i++) {
            String sentencia = n.sentencias.get(i).aceptar(this);
            if (sentencia != null && !sentencia.isEmpty()) {
                sb.append(sentencia).append("\n");
            }
        }
        bajarNivel();
        sb.append(indentar()).append("}");
        return sb.toString();
    }

    //Control de flujo

    @Override
    public String visitarSi(NodoSi n) {
        StringBuilder sb = new StringBuilder();
        sb.append(indentar()).append("if (")
                .append(n.condicion.aceptar(this)).append(")");
        sb.append(visitarBloqueInline(n.cuerpoIf));

        for (int i = 0; i < n.ramasElseIf.size(); i++) {
            NodoSi.RamaElseIf rama = n.ramasElseIf.get(i);
            sb.append(" else if (").append(rama.condicion.aceptar(this)).append(")");
            sb.append(visitarBloqueInline(rama.cuerpo));
        }

        if (n.cuerpoElse != null) {
            sb.append(" else");
            sb.append(visitarBloqueInline(n.cuerpoElse));
        }

        return sb.toString();
    }

    @Override
    public String visitarMientras(NodoMientras n) {
        StringBuilder sb = new StringBuilder();
        sb.append(indentar()).append("while (")
                .append(n.condicion.aceptar(this)).append(")");
        sb.append(visitarBloqueInline(n.cuerpo));
        return sb.toString();
    }

    @Override
    public String visitarHacer(NodoHacer n) {
        StringBuilder sb = new StringBuilder();
        sb.append(indentar()).append("do");
        sb.append(visitarBloqueInline(n.cuerpo));
        sb.append(" while (").append(n.condicion.aceptar(this)).append(");");
        return sb.toString();
    }

    @Override
    public String visitarPara(NodoPara n) {
        StringBuilder sb = new StringBuilder();
        String init      = n.init      != null ? sinPuntoYComa(n.init.aceptar(this))  : "";
        String condicion = n.condicion != null ? n.condicion.aceptar(this)            : "";
        String paso      = n.paso      != null ? sinPuntoYComa(n.paso.aceptar(this))  : "";

        sb.append(indentar()).append("for (")
                .append(init).append("; ")
                .append(condicion).append("; ")
                .append(paso).append(")");
        sb.append(visitarBloqueInline(n.cuerpo));
        return sb.toString();
    }

    @Override
    public String visitarParaEn(NodoParaEn n) {
        // para elemento en lista- for (int i = 0; i < lista.size(); i++)
        String coleccion = n.coleccion.aceptar(this);
        StringBuilder sb = new StringBuilder();
        sb.append(indentar()).append("for (int i = 0; i < ")
                .append(coleccion).append(".size(); i++)");
        sb.append(" {\n");
        subirNivel();
        sb.append(indentar()).append("Object ").append(n.variable)
                .append(" = ").append(coleccion).append(".get(i);\n");

        for (int i = 0; i < n.cuerpo.sentencias.size(); i++) {
            String sentencia = n.cuerpo.sentencias.get(i).aceptar(this);
            if (sentencia != null && !sentencia.isEmpty()) {
                sb.append(sentencia).append("\n");
            }
        }

        bajarNivel();
        sb.append(indentar()).append("}");
        return sb.toString();
    }

    @Override
    public String visitarRetornar(NodoRetornar n) {
        if (n.valor == null) return indentar() + "return;";
        return indentar() + "return " + n.valor.aceptar(this) + ";";
    }

    @Override
    public String visitarRomper(NodoRomper n) {
        return indentar() + "break;";
    }

    @Override
    public String visitarContinuar(NodoContinuar n) {
        return indentar() + "continue;";
    }

    // Funciones

    @Override
    public String visitarDeclaracionFuncion(NodoDeclaracionFuncion n) {
        StringBuilder sb = new StringBuilder();
        String tipoRetorno = traducirTipo(n.tipoRetorno);

        sb.append(indentar()).append("public static ")
                .append(tipoRetorno).append(" ").append(n.nombre).append("(");

        for (int i = 0; i < n.parametros.size(); i++) {
            NodoDeclaracionFuncion.Parametro p = n.parametros.get(i);
            sb.append(traducirTipo(p.tipo)).append(" ").append(p.nombre);
            if (i < n.parametros.size() - 1) sb.append(", ");
        }

        sb.append(")");
        sb.append(visitarBloqueInline(n.cuerpo));
        return sb.toString();
    }

    @Override
    public String visitarLlamadaFuncion(NodoLlamadaFuncion n) {
        StringBuilder sb = new StringBuilder();
        sb.append(n.nombre).append("(");
        for (int i = 0; i < n.argumentos.size(); i++) {
            sb.append(n.argumentos.get(i).aceptar(this));
            if (i < n.argumentos.size() - 1) sb.append(", ");
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public String visitarLlamadaMetodo(NodoLlamadaMetodo n) {
        String objeto = n.objeto.aceptar(this);
        StringBuilder args = new StringBuilder();
        for (int i = 0; i < n.argumentos.size(); i++) {
            args.append(n.argumentos.get(i).aceptar(this));
            if (i < n.argumentos.size() - 1) args.append(", ");
        }

        // Métodos de conversión Quetzal- Java
        switch (n.metodo) {
            case "numero":  return "Double.parseDouble(" + objeto + ")";
            case "entero":  return "Integer.parseInt(" + objeto + ")";
            case "texto":   return "String.valueOf(" + objeto + ")";
            case "agregar": return objeto + ".add(" + args + ")";
            case "tamanio": return objeto + ".size()";
            case "obtener": return objeto + ".get(" + args + ")";
            default:        return objeto + "." + n.metodo + "(" + args + ")";
        }
    }

    // OOP

    @Override
    public String visitarDeclaracionObjeto(NodoDeclaracionObjeto n) {
        StringBuilder sb = new StringBuilder();
        sb.append(indentar()).append("public static class ").append(n.nombre).append(" {\n");
        subirNivel();

        // Atributos privados
        for (int i = 0; i < n.atributosPrivados.size(); i++) {
            NodoDeclaracionVariable attr = n.atributosPrivados.get(i);
            sb.append(indentar()).append("private ")
                    .append(traducirTipo(attr.tipo)).append(" ").append(attr.nombre).append(";\n");
        }

        // Atributos públicos
        for (int i = 0; i < n.atributosPublicos.size(); i++) {
            NodoDeclaracionVariable attr = n.atributosPublicos.get(i);
            sb.append(indentar()).append("public ")
                    .append(traducirTipo(attr.tipo)).append(" ").append(attr.nombre).append(";\n");
        }

        // Constructor
        if (n.constructor != null) {
            sb.append("\n");
            sb.append(indentar()).append("public ").append(n.nombre).append("(");
            List<NodoDeclaracionFuncion.Parametro> params = n.constructor.parametros;
            for (int i = 0; i < params.size(); i++) {
                sb.append(traducirTipo(params.get(i).tipo))
                        .append(" ").append(params.get(i).nombre);
                if (i < params.size() - 1) sb.append(", ");
            }
            sb.append(")");
            sb.append(visitarBloqueInline(n.constructor.cuerpo));
            sb.append("\n");
        }

        // Métodos públicos
        for (int i = 0; i < n.metodosPublicos.size(); i++) {
            sb.append("\n");
            NodoDeclaracionFuncion metodo = n.metodosPublicos.get(i);
            sb.append(indentar()).append("public ")
                    .append(traducirTipo(metodo.tipoRetorno)).append(" ")
                    .append(metodo.nombre).append("(");
            for (int j = 0; j < metodo.parametros.size(); j++) {
                NodoDeclaracionFuncion.Parametro p = metodo.parametros.get(j);
                sb.append(traducirTipo(p.tipo)).append(" ").append(p.nombre);
                if (j < metodo.parametros.size() - 1) sb.append(", ");
            }
            sb.append(")");
            sb.append(visitarBloqueInline(metodo.cuerpo));
            sb.append("\n");
        }

        bajarNivel();
        sb.append(indentar()).append("}");
        return sb.toString();
    }

    @Override
    public String visitarNuevoObjeto(NodoNuevoObjeto n) {
        StringBuilder sb = new StringBuilder();
        sb.append("new ").append(n.tipoObjeto).append("(");
        for (int i = 0; i < n.argumentos.size(); i++) {
            sb.append(n.argumentos.get(i).aceptar(this));
            if (i < n.argumentos.size() - 1) sb.append(", ");
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public String visitarAccesoMiembro(NodoAccesoMiembro n) {
        String objeto = n.objeto.aceptar(this);
        if (objeto.equals("ambiente")) return "this." + n.campo;
        return objeto + "." + n.campo;
    }

    @Override
    public String visitarAccesoIndice(NodoAccesoIndice n) {
        return n.coleccion.aceptar(this) + ".get(" + n.indice.aceptar(this) + ")";
    }

    // Manejo de errores

    @Override
    public String visitarIntentar(NodoIntentar n) {
        StringBuilder sb = new StringBuilder();
        sb.append(indentar()).append("try");
        sb.append(visitarBloqueInline(n.cuerpoIntentar));

        if (n.cuerpoCapturar != null) {
            sb.append(" catch (Exception ").append(n.variableError).append(")");
            sb.append(visitarBloqueInline(n.cuerpoCapturar));
        }

        if (n.cuerpoFinalmente != null) {
            sb.append(" finally");
            sb.append(visitarBloqueInline(n.cuerpoFinalmente));
        }

        return sb.toString();
    }

    @Override
    public String visitarLanzar(NodoLanzar n) {
        return indentar() + "throw new RuntimeException(String.valueOf("
                + n.expresion.aceptar(this) + "));";
    }

    // Módulos

    @Override
    public String visitarImportar(NodoImportar n) {
        return indentar() + "// importar desde \"" + n.ruta + "\": " + n.simbolos;
    }

    @Override
    public String visitarExportar(NodoExportar n) {
        return indentar() + "// exportar: " + n.simbolos;
    }

    //Expresiones

    @Override
    public String visitarBinaria(NodoBinaria n) {
        String izq = generarConParentesis(n.izquierda);
        String der = generarConParentesis(n.derecha);

        switch (n.operador) {
            case "y":  return izq + " && " + der;
            case "o":  return izq + " || " + der;
            case "no": return "!" + izq;

            case "==":
                // Si alguno de los lados es String, usar .equals()
                if (izq.contains("\"") || der.contains("\"")) {
                    return izq + ".equals(" + der + ")";
                }
                return izq + " == " + der;

            case "!=":
                if (izq.contains("\"") || der.contains("\"")) {
                    return "!" + izq + ".equals(" + der + ")";
                }
                return izq + " != " + der;

            default:
                return izq + " " + n.operador + " " + der;
        }
    }

    @Override
    public String visitarUnaria(NodoUnaria n) {
        String operando = n.operando.aceptar(this);
        switch (n.operador) {
            case "no":      return "!" + operando;
            case "esperar": return operando;
            default:        return n.operador + operando;
        }
    }

    @Override
    public String visitarTernaria(NodoTernaria n) {
        return n.condicion.aceptar(this)
                + " ? " + n.siVerdadero.aceptar(this)
                + " : " + n.siFalso.aceptar(this);
    }

    // Literales

    @Override
    public String visitarLiteralEntero(NodoLiteralEntero n) {
        return String.valueOf(n.valor);
    }

    @Override
    public String visitarLiteralReal(NodoLiteralReal n) {
        return String.valueOf(n.valor);
    }

    @Override
    public String visitarLiteralTexto(NodoLiteralTexto n) {
        return "\"" + n.valor + "\"";
    }

    @Override
    public String visitarLiteralTextoInterp(NodoLiteralTextoInterp n) {
        return traducirTextoInterpolado(n.plantilla);
    }

    @Override
    public String visitarLiteralLog(NodoLiteralLog n) {
        return n.valor ? "true" : "false";
    }

    @Override
    public String visitarLiteralNulo(NodoLiteralNulo n) {
        return "null";
    }

    @Override
    public String visitarLiteralLista(NodoLiteralLista n) {
        // Se maneja en visitarDeclaracionLista con .add()
        return "new ArrayList<>()";
    }

    @Override
    public String visitarLiteralJsn(NodoLiteralJsn n) {
        if (n.pares.isEmpty()) {
            return "new HashMap<String, Object>()";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("new HashMap<String, Object>() {{ ");
        for (Map.Entry<String, Nodo> entry : n.pares.entrySet()) {
            sb.append("put(\"").append(entry.getKey()).append("\", ")
                    .append(entry.getValue().aceptar(this)).append("); ");
        }
        sb.append("}}");
        return sb.toString();
    }

    @Override
    public String visitarIdentificador(NodoIdentificador n) {
        if (n.nombre.equals("ambiente")) return "this";
        return n.nombre;
    }

    // Consola

    @Override
    public String visitarConsola(NodoConsola n) {
        String arg = n.argumentos.size() > 0
                ? n.argumentos.get(0).aceptar(this)
                : "\"\"";

        switch (n.metodo) {
            case "mostrar":
                return indentar() + "System.out.println(" + arg + ");";
            case "mostrar_exito":
                return indentar() + "System.out.println(\"[OK] \" + " + arg + ");";
            case "mostrar_error":
                return indentar() + "System.err.println(\"[ERROR] \" + " + arg + ");";
            case "mostrar_advertencia":
                return indentar() + "System.out.println(\"[ADVERTENCIA] \" + " + arg + ");";
            case "mostrar_informacion":
                return indentar() + "System.out.println(\"[INFO] \" + " + arg + ");";
            case "pedir":
                // El prompt lo manejan visitarAsignacion y visitarDeclaracionVariable
                return "scanner.nextLine()";
            default:
                return indentar() + "System.out.println(" + arg + ");";
        }
    }

    // Utilidades internas

    /**
     * Genera el bloque { } sin indentación al inicio
     * porque ya la pone el método que llama.
     */
    private String visitarBloqueInline(NodoBloque bloque) {
        StringBuilder sb = new StringBuilder();
        sb.append(" {\n");
        subirNivel();
        for (int i = 0; i < bloque.sentencias.size(); i++) {
            String s = bloque.sentencias.get(i).aceptar(this);
            if (s != null && !s.isEmpty()) {
                sb.append(s).append("\n");
            }
        }
        bajarNivel();
        sb.append(indentar()).append("}");
        return sb.toString();
    }

    /**
     * Agrega paréntesis a una expresión binaria anidada
     * para respetar la precedencia de operadores.
     * Ej: (nota1 + nota2 + nota3) / 3
     *  - ((nota1 + nota2) + nota3) / 3
     */
    private String generarConParentesis(Nodo n) {
        String resultado = n.aceptar(this);
        if (n instanceof NodoBinaria) {
            return "(" + resultado + ")";
        }
        return resultado;
    }

    /**
     * Quita el punto y coma del final de una sentencia.
     * Se usa para el init y paso del for.
     * Ej: "int i = 0;" - "int i = 0"
     */
    private String sinPuntoYComa(String sentencia) {
        if (sentencia == null) return "";
        sentencia = sentencia.trim();
        if (sentencia.endsWith(";")) {
            return sentencia.substring(0, sentencia.length() - 1);
        }
        return sentencia;
    }

    /**
     * Traduce texto interpolado de Quetzal a concatenación Java.
     * t"Hola {nombre}, tienes {edad} años"
     * - "Hola " + nombre + ", tienes " + edad + " años"
     */
    private String traducirTextoInterpolado(String plantilla) {
        StringBuilder resultado = new StringBuilder();
        StringBuilder fragmento = new StringBuilder();
        StringBuilder expresion = new StringBuilder();
        boolean dentroExpresion = false;

        for (int i = 0; i < plantilla.length(); i++) {
            char c = plantilla.charAt(i);

            if (c == '{' && !dentroExpresion) {
                if (fragmento.length() > 0) {
                    if (resultado.length() > 0) resultado.append(" + ");
                    resultado.append("\"").append(fragmento).append("\"");
                    fragmento = new StringBuilder();
                }
                dentroExpresion = true;

            } else if (c == '}' && dentroExpresion) {
                if (resultado.length() > 0) resultado.append(" + ");
                resultado.append(expresion.toString().trim());
                expresion = new StringBuilder();
                dentroExpresion = false;

            } else if (dentroExpresion) {
                expresion.append(c);
            } else {
                fragmento.append(c);
            }
        }

        // Texto restante después de la última expresión
        if (fragmento.length() > 0) {
            if (resultado.length() > 0) resultado.append(" + ");
            resultado.append("\"").append(fragmento).append("\"");
        }

        if (resultado.length() == 0) return "\"\"";
        return resultado.toString();
    }

    /**
     * Detecta si una sentencia o sus hijos usan consola.pedir()
     * para saber si hay que declarar el Scanner.
     */
    private boolean necesitaScanner(Nodo nodo) {
        if (nodo instanceof NodoConsola) {
            NodoConsola c = (NodoConsola) nodo;
            if (c.metodo.equals("pedir")) return true;
        }
        if (nodo instanceof NodoDeclaracionVariable) {
            NodoDeclaracionVariable d = (NodoDeclaracionVariable) nodo;
            if (d.valor != null) return necesitaScanner(d.valor);
        }
        if (nodo instanceof NodoAsignacion) {
            NodoAsignacion a = (NodoAsignacion) nodo;
            return necesitaScanner(a.valor);
        }
        if (nodo instanceof NodoBloque) {
            NodoBloque b = (NodoBloque) nodo;
            for (int i = 0; i < b.sentencias.size(); i++) {
                if (necesitaScanner(b.sentencias.get(i))) return true;
            }
        }
        if (nodo instanceof NodoSi) {
            NodoSi s = (NodoSi) nodo;
            if (necesitaScanner(s.cuerpoIf)) return true;
            for (int i = 0; i < s.ramasElseIf.size(); i++) {
                if (necesitaScanner(s.ramasElseIf.get(i).cuerpo)) return true;
            }
            if (s.cuerpoElse != null && necesitaScanner(s.cuerpoElse)) return true;
        }
        if (nodo instanceof NodoMientras) {
            return necesitaScanner(((NodoMientras) nodo).cuerpo);
        }
        return false;
    }
}