package sintactico.nodos;
import sintactico.Nodo; import sintactico.VisitanteNodo;
import java.util.List;
/** Literal lista: [1, 2, 3] o ["Ana", "Luis"] */
public class NodoLiteralLista extends Nodo {
    public final List<Nodo> elementos;
    public NodoLiteralLista(int linea, List<Nodo> elementos) { super(linea); this.elementos = elementos; }
    @Override public <T> T aceptar(VisitanteNodo<T> v) { return v.visitarLiteralLista(this); }
}
