package sintactico.nodos;

import sintactico.Nodo;
import sintactico.VisitanteNodo;

/** lanzar expresion */
public class NodoLanzar extends Nodo {
    public final Nodo expresion;

    public NodoLanzar(int linea, Nodo expresion) {
        super(linea);
        this.expresion = expresion;
    }

    @Override public <T> T aceptar(VisitanteNodo<T> v) { return v.visitarLanzar(this); }
}
