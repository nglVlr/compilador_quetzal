package sintactico;

import sintactico.nodos.*;
import java.util.List;

/**
 * Convierte el AST a JSON estructurado real.
 * Cada nodo se convierte en un objeto JSON con sus hijos anidados.
 *
 * Uso:
 *   ASTaJSON visitor = new ASTaJSON();
 *   String json = ast.aceptar(visitor);
 */
public class ASTaJSON implements VisitanteNodo<String> {

    // Helpers base

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private String nodoSimple(String tipo, String... pares) {
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"tipo\":\"").append(tipo).append("\"");
        for (int i = 0; i < pares.length - 1; i += 2) {
            sb.append(",\"").append(pares[i]).append("\":").append(pares[i + 1]);
        }
        sb.append("}");
        return sb.toString();
    }

    private String arrayNodos(List<? extends Nodo> nodos) {
        if (nodos == null || nodos.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < nodos.size(); i++) {
            if (nodos.get(i) != null) sb.append(nodos.get(i).aceptar(this));
            else sb.append("null");
            if (i < nodos.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    private String nodoONull(Nodo n) {
        return n != null ? n.aceptar(this) : "null";
    }

    // Programa

    @Override
    public String visitarPrograma(NodoPrograma n) {
        return "{\"tipo\":\"Programa\",\"sentencias\":" + arrayNodos(n.sentencias) + "}";
    }

    // Declaraciones

    @Override
    public String visitarDeclaracionVariable(NodoDeclaracionVariable n) {
        return "{\"tipo\":\"DeclVar\""
                + ",\"tipoDato\":\"" + esc(n.tipo) + "\""
                + ",\"nombre\":\"" + esc(n.nombre) + "\""
                + ",\"mutable\":" + n.mutable
                + ",\"valor\":" + nodoONull(n.valor)
                + "}";
    }

    @Override
    public String visitarDeclaracionLista(NodoDeclaracionLista n) {
        String tipoElem = n.tipoElemento != null ? "\"" + esc(n.tipoElemento) + "\"" : "null";
        return "{\"tipo\":\"DeclLista\""
                + ",\"tipoElemento\":" + tipoElem
                + ",\"nombre\":\"" + esc(n.nombre) + "\""
                + ",\"mutable\":" + n.mutable
                + ",\"valor\":" + nodoONull(n.valor)
                + "}";
    }

    @Override
    public String visitarDeclaracionJsn(NodoDeclaracionJsn n) {
        return "{\"tipo\":\"DeclJsn\""
                + ",\"nombre\":\"" + esc(n.nombre) + "\""
                + ",\"mutable\":" + n.mutable
                + ",\"valor\":" + nodoONull(n.valor)
                + "}";
    }

    // Asignaciones

    @Override
    public String visitarAsignacion(NodoAsignacion n) {
        return "{\"tipo\":\"Asignacion\""
                + ",\"objetivo\":" + nodoONull(n.objetivo)
                + ",\"valor\":" + nodoONull(n.valor)
                + "}";
    }

    @Override
    public String visitarAsignacionCompuesta(NodoAsignacionCompuesta n) {
        return "{\"tipo\":\"AsignacionCompuesta\""
                + ",\"operador\":\"" + esc(n.operador) + "\""
                + ",\"objetivo\":" + nodoONull(n.objetivo)
                + ",\"valor\":" + nodoONull(n.valor)
                + "}";
    }

    @Override
    public String visitarIncrementoDecremento(NodoIncrementoDecremento n) {
        return "{\"tipo\":\"IncrementoDecremento\""
                + ",\"operador\":\"" + esc(n.operador) + "\""
                + ",\"objetivo\":" + nodoONull(n.objetivo)
                + "}";
    }

    // Bloque

    @Override
    public String visitarBloque(NodoBloque n) {
        return "{\"tipo\":\"Bloque\",\"sentencias\":" + arrayNodos(n.sentencias) + "}";
    }

    //Control de flujo

    @Override
    public String visitarSi(NodoSi n) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"tipo\":\"Si\"");
        sb.append(",\"condicion\":").append(nodoONull(n.condicion));
        sb.append(",\"cuerpoIf\":").append(nodoONull(n.cuerpoIf));

        // Ramas sino_si
        sb.append(",\"ramasSinoSi\":[");
        List<NodoSi.RamaElseIf> ramas = n.ramasElseIf;
        for (int i = 0; i < ramas.size(); i++) {
            NodoSi.RamaElseIf r = ramas.get(i);
            sb.append("{\"condicion\":").append(nodoONull(r.condicion));
            sb.append(",\"cuerpo\":").append(nodoONull(r.cuerpo)).append("}");
            if (i < ramas.size() - 1) sb.append(",");
        }
        sb.append("]");

        sb.append(",\"cuerpoElse\":").append(nodoONull(n.cuerpoElse));
        sb.append("}");
        return sb.toString();
    }

    @Override
    public String visitarMientras(NodoMientras n) {
        return "{\"tipo\":\"Mientras\""
                + ",\"condicion\":" + nodoONull(n.condicion)
                + ",\"cuerpo\":" + nodoONull(n.cuerpo)
                + "}";
    }

    @Override
    public String visitarHacer(NodoHacer n) {
        return "{\"tipo\":\"Hacer\""
                + ",\"cuerpo\":" + nodoONull(n.cuerpo)
                + ",\"condicion\":" + nodoONull(n.condicion)
                + "}";
    }

    @Override
    public String visitarPara(NodoPara n) {
        return "{\"tipo\":\"Para\""
                + ",\"init\":" + nodoONull(n.init)
                + ",\"condicion\":" + nodoONull(n.condicion)
                + ",\"paso\":" + nodoONull(n.paso)
                + ",\"cuerpo\":" + nodoONull(n.cuerpo)
                + "}";
    }

    @Override
    public String visitarParaEn(NodoParaEn n) {
        return "{\"tipo\":\"ParaEn\""
                + ",\"variable\":\"" + esc(n.variable) + "\""
                + ",\"coleccion\":" + nodoONull(n.coleccion)
                + ",\"cuerpo\":" + nodoONull(n.cuerpo)
                + "}";
    }

    @Override
    public String visitarRetornar(NodoRetornar n) {
        return "{\"tipo\":\"Retornar\",\"valor\":" + nodoONull(n.valor) + "}";
    }

    @Override
    public String visitarRomper(NodoRomper n) {
        return "{\"tipo\":\"Romper\"}";
    }

    @Override
    public String visitarContinuar(NodoContinuar n) {
        return "{\"tipo\":\"Continuar\"}";
    }

    // Funciones

    @Override
    public String visitarDeclaracionFuncion(NodoDeclaracionFuncion n) {
        StringBuilder params = new StringBuilder("[");
        for (int i = 0; i < n.parametros.size(); i++) {
            NodoDeclaracionFuncion.Parametro p = n.parametros.get(i);
            params.append("{\"tipo\":\"").append(esc(p.tipo)).append("\"")
                    .append(",\"nombre\":\"").append(esc(p.nombre)).append("\"}");
            if (i < n.parametros.size() - 1) params.append(",");
        }
        params.append("]");

        return "{\"tipo\":\"DeclaracionFuncion\""
                + ",\"nombre\":\"" + esc(n.nombre) + "\""
                + ",\"tipoRetorno\":\"" + esc(n.tipoRetorno) + "\""
                + ",\"esAsync\":" + n.esAsync
                + ",\"parametros\":" + params
                + ",\"cuerpo\":" + nodoONull(n.cuerpo)
                + "}";
    }

    @Override
    public String visitarLlamadaFuncion(NodoLlamadaFuncion n) {
        return "{\"tipo\":\"LlamadaFuncion\""
                + ",\"nombre\":\"" + esc(n.nombre) + "\""
                + ",\"argumentos\":" + arrayNodos(n.argumentos)
                + "}";
    }

    @Override
    public String visitarLlamadaMetodo(NodoLlamadaMetodo n) {
        return "{\"tipo\":\"LlamadaMetodo\""
                + ",\"metodo\":\"" + esc(n.metodo) + "\""
                + ",\"objeto\":" + nodoONull(n.objeto)
                + ",\"argumentos\":" + arrayNodos(n.argumentos)
                + "}";
    }

    // OOP

    @Override
    public String visitarDeclaracionObjeto(NodoDeclaracionObjeto n) {
        return "{\"tipo\":\"DeclaracionObjeto\""
                + ",\"nombre\":\"" + esc(n.nombre) + "\""
                + ",\"atributosPrivados\":" + arrayNodos(n.atributosPrivados)
                + ",\"atributosPublicos\":" + arrayNodos(n.atributosPublicos)
                + ",\"constructor\":" + nodoONull(n.constructor)
                + ",\"metodosPublicos\":" + arrayNodos(n.metodosPublicos)
                + ",\"metodosPrivados\":" + arrayNodos(n.metodosPrivados)
                + "}";
    }

    @Override
    public String visitarNuevoObjeto(NodoNuevoObjeto n) {
        return "{\"tipo\":\"NuevoObjeto\""
                + ",\"tipoObjeto\":\"" + esc(n.tipoObjeto) + "\""
                + ",\"argumentos\":" + arrayNodos(n.argumentos)
                + "}";
    }

    @Override
    public String visitarAccesoMiembro(NodoAccesoMiembro n) {
        return "{\"tipo\":\"AccesoMiembro\""
                + ",\"campo\":\"" + esc(n.campo) + "\""
                + ",\"objeto\":" + nodoONull(n.objeto)
                + "}";
    }

    @Override
    public String visitarAccesoIndice(NodoAccesoIndice n) {
        return "{\"tipo\":\"AccesoIndice\""
                + ",\"coleccion\":" + nodoONull(n.coleccion)
                + ",\"indice\":" + nodoONull(n.indice)
                + "}";
    }

    //Manejo de errores

    @Override
    public String visitarIntentar(NodoIntentar n) {
        String varError = n.variableError != null
                ? "\"" + esc(n.variableError) + "\""
                : "null";
        return "{\"tipo\":\"Intentar\""
                + ",\"cuerpoIntentar\":" + nodoONull(n.cuerpoIntentar)
                + ",\"variableError\":" + varError
                + ",\"cuerpoCapturar\":" + nodoONull(n.cuerpoCapturar)
                + ",\"cuerpoFinalmente\":" + nodoONull(n.cuerpoFinalmente)
                + "}";
    }

    @Override
    public String visitarLanzar(NodoLanzar n) {
        return "{\"tipo\":\"Lanzar\",\"expresion\":" + nodoONull(n.expresion) + "}";
    }

    // Módulos

    @Override
    public String visitarImportar(NodoImportar n) {
        StringBuilder simbolos = new StringBuilder("[");
        for (int i = 0; i < n.simbolos.size(); i++) {
            simbolos.append("\"").append(esc(n.simbolos.get(i))).append("\"");
            if (i < n.simbolos.size() - 1) simbolos.append(",");
        }
        simbolos.append("]");
        return "{\"tipo\":\"Importar\""
                + ",\"simbolos\":" + simbolos
                + ",\"ruta\":\"" + esc(n.ruta) + "\""
                + "}";
    }

    @Override
    public String visitarExportar(NodoExportar n) {
        StringBuilder simbolos = new StringBuilder("[");
        for (int i = 0; i < n.simbolos.size(); i++) {
            simbolos.append("\"").append(esc(n.simbolos.get(i))).append("\"");
            if (i < n.simbolos.size() - 1) simbolos.append(",");
        }
        simbolos.append("]");
        return "{\"tipo\":\"Exportar\",\"simbolos\":" + simbolos + "}";
    }

    //Expresiones

    @Override
    public String visitarBinaria(NodoBinaria n) {
        return "{\"tipo\":\"Binaria\""
                + ",\"operador\":\"" + esc(n.operador) + "\""
                + ",\"izquierda\":" + nodoONull(n.izquierda)
                + ",\"derecha\":" + nodoONull(n.derecha)
                + "}";
    }

    @Override
    public String visitarUnaria(NodoUnaria n) {
        return "{\"tipo\":\"Unaria\""
                + ",\"operador\":\"" + esc(n.operador) + "\""
                + ",\"operando\":" + nodoONull(n.operando)
                + "}";
    }

    @Override
    public String visitarTernaria(NodoTernaria n) {
        return "{\"tipo\":\"Ternaria\""
                + ",\"condicion\":" + nodoONull(n.condicion)
                + ",\"siVerdadero\":" + nodoONull(n.siVerdadero)
                + ",\"siFalso\":" + nodoONull(n.siFalso)
                + "}";
    }

    //Literales

    @Override
    public String visitarLiteralEntero(NodoLiteralEntero n) {
        return "{\"tipo\":\"LiteralEntero\",\"valor\":" + n.valor + "}";
    }

    @Override
    public String visitarLiteralReal(NodoLiteralReal n) {
        return "{\"tipo\":\"LiteralReal\",\"valor\":" + n.valor + "}";
    }

    @Override
    public String visitarLiteralTexto(NodoLiteralTexto n) {
        return "{\"tipo\":\"LiteralTexto\",\"valor\":\"" + esc(n.valor) + "\"}";
    }

    @Override
    public String visitarLiteralTextoInterp(NodoLiteralTextoInterp n) {
        return "{\"tipo\":\"LiteralTextoInterp\",\"plantilla\":\"" + esc(n.plantilla) + "\"}";
    }

    @Override
    public String visitarLiteralLog(NodoLiteralLog n) {
        return "{\"tipo\":\"LiteralLog\",\"valor\":" + n.valor + "}";
    }

    @Override
    public String visitarLiteralNulo(NodoLiteralNulo n) {
        return "{\"tipo\":\"LiteralNulo\",\"valor\":null}";
    }

    @Override
    public String visitarLiteralLista(NodoLiteralLista n) {
        return "{\"tipo\":\"LiteralLista\",\"elementos\":" + arrayNodos(n.elementos) + "}";
    }

    @Override
    public String visitarLiteralJsn(NodoLiteralJsn n) {
        StringBuilder pares = new StringBuilder("[");
        int i = 0;
        for (var entry : n.pares.entrySet()) {
            pares.append("{\"clave\":\"").append(esc(entry.getKey())).append("\"")
                    .append(",\"valor\":").append(nodoONull(entry.getValue())).append("}");
            if (i < n.pares.size() - 1) pares.append(",");
            i++;
        }
        pares.append("]");
        return "{\"tipo\":\"LiteralJsn\",\"pares\":" + pares + "}";
    }

    @Override
    public String visitarIdentificador(NodoIdentificador n) {
        return "{\"tipo\":\"Identificador\",\"nombre\":\"" + esc(n.nombre) + "\"}";
    }

    @Override
    public String visitarConsola(NodoConsola n) {
        return "{\"tipo\":\"Consola\""
                + ",\"metodo\":\"" + esc(n.metodo) + "\""
                + ",\"argumentos\":" + arrayNodos(n.argumentos)
                + "}";
    }
}