package sintactico.nodos;
import sintactico.Nodo; import sintactico.VisitanteNodo;
/** Literal nulo */
public class NodoLiteralNulo extends Nodo {
    public NodoLiteralNulo(int linea) { super(linea); }
    @Override public <T> T aceptar(VisitanteNodo<T> v) { return v.visitarLiteralNulo(this); }
}
