package sintactico.nodos;

import sintactico.Nodo;
import sintactico.VisitanteNodo;
import java.util.List;

/** Raíz del AST: contiene todas las sentencias del programa Quetzal. */
public class NodoPrograma extends Nodo {
    public final List<Nodo> sentencias;

    public NodoPrograma(List<Nodo> sentencias) {
        super(0);
        this.sentencias = sentencias;
    }

    @Override public <T> T aceptar(VisitanteNodo<T> v) { return v.visitarPrograma(this); }
}
