package sintactico.nodos;
import sintactico.Nodo; import sintactico.VisitanteNodo;
/** Literal real: 3.14 */
public class NodoLiteralReal extends Nodo {
    public final double valor;
    public NodoLiteralReal(int linea, double valor) { super(linea); this.valor = valor; }
    @Override public <T> T aceptar(VisitanteNodo<T> v) { return v.visitarLiteralReal(this); }
}
