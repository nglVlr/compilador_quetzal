package sintactico.nodos;

import sintactico.Nodo;
import sintactico.VisitanteNodo;
import java.util.List;

/**
 * Bloque de sentencias entre llaves { ... }.
 * Introduce un nuevo ámbito de variables.
 */
public class NodoBloque extends Nodo {
    public final List<Nodo> sentencias;

    public NodoBloque(int linea, List<Nodo> sentencias) {
        super(linea);
        this.sentencias = sentencias;
    }

    @Override public <T> T aceptar(VisitanteNodo<T> v) { return v.visitarBloque(this); }
}
