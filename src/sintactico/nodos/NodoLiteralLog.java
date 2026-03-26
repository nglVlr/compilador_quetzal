package sintactico.nodos;
import sintactico.Nodo; import sintactico.VisitanteNodo;
/** Literal booleano: verdadero / falso */
public class NodoLiteralLog extends Nodo {
    public final boolean valor;
    public NodoLiteralLog(int linea, boolean valor) { super(linea); this.valor = valor; }
    @Override public <T> T aceptar(VisitanteNodo<T> v) { return v.visitarLiteralLog(this); }
}
