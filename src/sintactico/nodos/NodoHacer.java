package sintactico.nodos;

import sintactico.Nodo;
import sintactico.VisitanteNodo;

/**
 * Bucle hacer { ... } mientras (cond)
 * Equivalente a do { } while en Java.
 */
public class NodoHacer extends Nodo {
    public final NodoBloque cuerpo;
    public final Nodo       condicion;

    public NodoHacer(int linea, NodoBloque cuerpo, Nodo condicion) {
        super(linea);
        this.cuerpo    = cuerpo;
        this.condicion = condicion;
    }

    @Override public <T> T aceptar(VisitanteNodo<T> v) { return v.visitarHacer(this); }
}
