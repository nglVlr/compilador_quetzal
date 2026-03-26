package sintactico.nodos;
import sintactico.Nodo; import sintactico.VisitanteNodo;
/** Literal texto: "hola" */
public class NodoLiteralTexto extends Nodo {
    public final String valor;
    public NodoLiteralTexto(int linea, String valor) { super(linea); this.valor = valor; }
    @Override public <T> T aceptar(VisitanteNodo<T> v) { return v.visitarLiteralTexto(this); }
}
