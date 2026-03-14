package sintactico.nodos;

import sintactico.Nodo;
import sintactico.VisitanteNodo;

/**
 * Acceso por índice: coleccion[indice]
 * Ejemplos:
 *   numeros[0]
 *   persona.direcciones[1]
 */
public class NodoAccesoIndice extends Nodo {
    public final Nodo coleccion;
    public final Nodo indice;

    public NodoAccesoIndice(int linea, Nodo coleccion, Nodo indice) {
        super(linea);
        this.coleccion = coleccion;
        this.indice    = indice;
    }

    @Override public <T> T aceptar(VisitanteNodo<T> v) { return v.visitarAccesoIndice(this); }
}
