package sintactico;

import sintactico.nodos.*;

/**
 * Imprime el AST de forma indentada para visualizar el resultado del parser.
 * Implementa VisitanteNodo<String> -- devuelve el árbol como texto.
 */
public class ImpressorAST implements VisitanteNodo<String> {

    private int nivel = 0;

    private String indentar(String texto) {
        return "  ".repeat(nivel) + texto;
    }

    private String hijos(java.util.List<Nodo> nodos) {
        if (nodos == null || nodos.isEmpty()) return "";
        nivel++;
        StringBuilder sb = new StringBuilder();
        for (Nodo n : nodos) {
            if (n != null) sb.append("\n").append(n.aceptar(this));
        }
        nivel--;
        return sb.toString();
    }

    private String hijo(Nodo n) {
        if (n == null) return " nulo";
        nivel++;
        String r = "\n" + n.aceptar(this);
        nivel--;
        return r;
    }

    @Override public String visitarPrograma(NodoPrograma n) {
        return indentar("Programa") + hijos(n.sentencias);
    }

    @Override public String visitarDeclaracionVariable(NodoDeclaracionVariable n) {
        return indentar("DeclVar " + n.tipo + (n.mutable ? " var" : "") + " " + n.nombre)
             + hijo(n.valor);
    }

    @Override public String visitarDeclaracionLista(NodoDeclaracionLista n) {
        String tipoStr = n.tipoElemento != null ? "<" + n.tipoElemento + ">" : "";
        return indentar("DeclLista" + tipoStr + (n.mutable ? " var" : "") + " " + n.nombre)
             + hijo(n.valor);
    }

    @Override public String visitarDeclaracionJsn(NodoDeclaracionJsn n) {
        return indentar("DeclJsn" + (n.mutable ? " var" : "") + " " + n.nombre)
             + hijo(n.valor);
    }

    @Override public String visitarAsignacion(NodoAsignacion n) {
        return indentar("Asignar") + hijo(n.objetivo) + hijo(n.valor);
    }

    @Override public String visitarAsignacionCompuesta(NodoAsignacionCompuesta n) {
        return indentar("AsignarComp " + n.operador) + hijo(n.objetivo) + hijo(n.valor);
    }

    @Override public String visitarIncrementoDecremento(NodoIncrementoDecremento n) {
        return indentar("IncDec " + n.operador) + hijo(n.objetivo);
    }

    @Override public String visitarBloque(NodoBloque n) {
        return indentar("Bloque") + hijos(n.sentencias);
    }

    @Override public String visitarSi(NodoSi n) {
        StringBuilder sb = new StringBuilder(indentar("Si"));
        nivel++; sb.append("\n").append(indentar("Cond:")).append(hijo(n.condicion));
        sb.append("\n").append(indentar("CuerpoSi:")).append(hijo(n.cuerpoIf));
        for (NodoSi.RamaElseIf r : n.ramasElseIf) {
            sb.append("\n").append(indentar("SinoSi:")).append(hijo(r.condicion));
            sb.append(hijo(r.cuerpo));
        }
        if (n.cuerpoElse != null) sb.append("\n").append(indentar("Sino:")).append(hijo(n.cuerpoElse));
        nivel--;
        return sb.toString();
    }

    @Override public String visitarMientras(NodoMientras n) {
        return indentar("Mientras") + hijo(n.condicion) + hijo(n.cuerpo);
    }

    @Override public String visitarHacer(NodoHacer n) {
        return indentar("Hacer") + hijo(n.cuerpo) + hijo(n.condicion);
    }

    @Override public String visitarPara(NodoPara n) {
        return indentar("Para") + hijo(n.init) + hijo(n.condicion) + hijo(n.paso) + hijo(n.cuerpo);
    }

    @Override public String visitarParaEn(NodoParaEn n) {
        return indentar("ParaEn " + n.variable) + hijo(n.coleccion) + hijo(n.cuerpo);
    }

    @Override public String visitarRetornar(NodoRetornar n) {
        return indentar("Retornar") + (n.valor != null ? hijo(n.valor) : "");
    }

    @Override public String visitarRomper(NodoRomper n)     { return indentar("Romper"); }
    @Override public String visitarContinuar(NodoContinuar n) { return indentar("Continuar"); }

    @Override public String visitarDeclaracionFuncion(NodoDeclaracionFuncion n) {
        String params = n.parametros.stream()
            .map(p -> p.tipo + " " + p.nombre)
            .reduce((a,b) -> a + ", " + b).orElse("");
        return indentar("Funcion" + (n.esAsync ? "(async)" : "") + " " + n.tipoRetorno
               + " " + n.nombre + "(" + params + ")") + hijo(n.cuerpo);
    }

    @Override public String visitarLlamadaFuncion(NodoLlamadaFuncion n) {
        return indentar("LlamarFun " + n.nombre) + hijos(n.argumentos);
    }

    @Override public String visitarLlamadaMetodo(NodoLlamadaMetodo n) {
        return indentar("LlamarMetodo ." + n.metodo) + hijo(n.objeto) + hijos(n.argumentos);
    }

    @Override public String visitarDeclaracionObjeto(NodoDeclaracionObjeto n) {
        StringBuilder sb = new StringBuilder(indentar("Objeto " + n.nombre));
        nivel++;
        if (!n.atributosPrivados.isEmpty()) {
            sb.append("\n").append(indentar("privado:"));
            for (NodoDeclaracionVariable a : n.atributosPrivados) sb.append(hijo(a));
        }
        if (n.constructor != null) {
            sb.append("\n").append(indentar("constructor:")).append(hijo(n.constructor));
        }
        if (!n.metodosPublicos.isEmpty()) {
            sb.append("\n").append(indentar("publico:"));
            for (NodoDeclaracionFuncion m : n.metodosPublicos) sb.append(hijo(m));
        }
        nivel--;
        return sb.toString();
    }

    @Override public String visitarNuevoObjeto(NodoNuevoObjeto n) {
        return indentar("Nuevo " + n.tipoObjeto) + hijos(n.argumentos);
    }

    @Override public String visitarAccesoMiembro(NodoAccesoMiembro n) {
        return indentar("AccesoMiembro ." + n.campo) + hijo(n.objeto);
    }

    @Override public String visitarAccesoIndice(NodoAccesoIndice n) {
        return indentar("AccesoIndice") + hijo(n.coleccion) + hijo(n.indice);
    }

    @Override public String visitarIntentar(NodoIntentar n) {
        StringBuilder sb = new StringBuilder(indentar("Intentar"));
        sb.append(hijo(n.cuerpoIntentar));
        if (n.cuerpoCapturar != null)   sb.append("\n").append(indentar("  Capturar(" + n.variableError + "):")).append(hijo(n.cuerpoCapturar));
        if (n.cuerpoFinalmente != null) sb.append("\n").append(indentar("  Finalmente:")).append(hijo(n.cuerpoFinalmente));
        return sb.toString();
    }

    @Override public String visitarLanzar(NodoLanzar n) {
        return indentar("Lanzar") + hijo(n.expresion);
    }

    @Override public String visitarImportar(NodoImportar n) {
        return indentar("Importar " + n.simbolos + " desde \"" + n.ruta + "\"");
    }

    @Override public String visitarExportar(NodoExportar n) {
        return indentar("Exportar " + n.simbolos);
    }

    @Override public String visitarBinaria(NodoBinaria n) {
        return indentar("Binaria " + n.operador) + hijo(n.izquierda) + hijo(n.derecha);
    }

    @Override public String visitarUnaria(NodoUnaria n) {
        return indentar("Unaria " + n.operador) + hijo(n.operando);
    }

    @Override public String visitarTernaria(NodoTernaria n) {
        return indentar("Ternaria ?:") + hijo(n.condicion) + hijo(n.siVerdadero) + hijo(n.siFalso);
    }

    @Override public String visitarLiteralEntero(NodoLiteralEntero n)   { return indentar("EnteroLit " + n.valor); }
    @Override public String visitarLiteralReal(NodoLiteralReal n)       { return indentar("RealLit " + n.valor); }
    @Override public String visitarLiteralTexto(NodoLiteralTexto n)     { return indentar("TextoLit \"" + n.valor + "\""); }
    @Override public String visitarLiteralTextoInterp(NodoLiteralTextoInterp n) { return indentar("TextoInterp t\"" + n.plantilla + "\""); }
    @Override public String visitarLiteralLog(NodoLiteralLog n)         { return indentar("LogLit " + n.valor); }
    @Override public String visitarLiteralNulo(NodoLiteralNulo n)       { return indentar("NuloLit"); }
    @Override public String visitarLiteralLista(NodoLiteralLista n)     { return indentar("ListaLit") + hijos(n.elementos); }
    @Override public String visitarLiteralJsn(NodoLiteralJsn n) {
        StringBuilder sb = new StringBuilder(indentar("JsnLit"));
        nivel++;
        for (var e : n.pares.entrySet()) {
            sb.append("\n").append(indentar(e.getKey() + ":")).append(hijo(e.getValue()));
        }
        nivel--;
        return sb.toString();
    }
    @Override public String visitarIdentificador(NodoIdentificador n)   { return indentar("Id " + n.nombre); }
    @Override public String visitarConsola(NodoConsola n)               { return indentar("Consola." + n.metodo) + hijos(n.argumentos); }
}
