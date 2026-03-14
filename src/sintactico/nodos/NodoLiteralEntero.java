package sintactico.nodos;

import sintactico.Nodo;
import sintactico.VisitanteNodo;

/** Literal entero: 42 */
public class NodoLiteralEntero extends Nodo {
    public final int valor;
    public NodoLiteralEntero(int linea, int valor) { super(linea); this.valor = valor; }
    @Override public <T> T aceptar(VisitanteNodo<T> v) { return v.visitarLiteralEntero(this); }
}
