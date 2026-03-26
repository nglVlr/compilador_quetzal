package sintactico;

import sintactico.nodos.*;

/**
 * Interfaz del patrón Visitor para recorrer el AST de Quetzal.
 *
 * Cada fase posterior (Semántico, Generador de código Java)
 * implementará esta interfaz y procesará cada tipo de nodo.
 */
public interface VisitanteNodo<T> {

    // Programa
    T visitarPrograma(NodoPrograma nodo);

    //  Declaraciones de variables
    T visitarDeclaracionVariable(NodoDeclaracionVariable nodo);
    T visitarDeclaracionLista(NodoDeclaracionLista nodo);
    T visitarDeclaracionJsn(NodoDeclaracionJsn nodo);

    // Sentencias
    T visitarAsignacion(NodoAsignacion nodo);
    T visitarAsignacionCompuesta(NodoAsignacionCompuesta nodo);
    T visitarIncrementoDecremento(NodoIncrementoDecremento nodo);
    T visitarBloque(NodoBloque nodo);

    // Control de flujo
    T visitarSi(NodoSi nodo);
    T visitarMientras(NodoMientras nodo);
    T visitarHacer(NodoHacer nodo);
    T visitarPara(NodoPara nodo);
    T visitarParaEn(NodoParaEn nodo);
    T visitarRetornar(NodoRetornar nodo);
    T visitarRomper(NodoRomper nodo);
    T visitarContinuar(NodoContinuar nodo);

    //  Funciones
    T visitarDeclaracionFuncion(NodoDeclaracionFuncion nodo);
    T visitarLlamadaFuncion(NodoLlamadaFuncion nodo);
    T visitarLlamadaMetodo(NodoLlamadaMetodo nodo);

    //OOP
    T visitarDeclaracionObjeto(NodoDeclaracionObjeto nodo);
    T visitarNuevoObjeto(NodoNuevoObjeto nodo);
    T visitarAccesoMiembro(NodoAccesoMiembro nodo);
    T visitarAccesoIndice(NodoAccesoIndice nodo);

    //  Manejo de errores
    T visitarIntentar(NodoIntentar nodo);
    T visitarLanzar(NodoLanzar nodo);

    // Módulos
    T visitarImportar(NodoImportar nodo);
    T visitarExportar(NodoExportar nodo);

    // Expresiones
    T visitarBinaria(NodoBinaria nodo);
    T visitarUnaria(NodoUnaria nodo);
    T visitarTernaria(NodoTernaria nodo);
    T visitarLiteralEntero(NodoLiteralEntero nodo);
    T visitarLiteralReal(NodoLiteralReal nodo);
    T visitarLiteralTexto(NodoLiteralTexto nodo);
    T visitarLiteralTextoInterp(NodoLiteralTextoInterp nodo);
    T visitarLiteralLog(NodoLiteralLog nodo);
    T visitarLiteralNulo(NodoLiteralNulo nodo);
    T visitarLiteralLista(NodoLiteralLista nodo);
    T visitarLiteralJsn(NodoLiteralJsn nodo);
    T visitarIdentificador(NodoIdentificador nodo);
    T visitarConsola(NodoConsola nodo);
}
