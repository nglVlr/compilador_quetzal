package sintactico.nodos;

import sintactico.Nodo;
import sintactico.VisitanteNodo;

/** continuar - salta a la siguiente iteración del bucle */
public class NodoContinuar extends Nodo {
    public NodoContinuar(int linea) { super(linea); }
    @Override public <T> T aceptar(VisitanteNodo<T> v) { return v.visitarContinuar(this); }
}
