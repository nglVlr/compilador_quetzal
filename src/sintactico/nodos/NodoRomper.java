package sintactico.nodos;

import sintactico.Nodo;
import sintactico.VisitanteNodo;

/** romper - sale del bucle actual */
public class NodoRomper extends Nodo {
    public NodoRomper(int linea) { super(linea); }
    @Override public <T> T aceptar(VisitanteNodo<T> v) { return v.visitarRomper(this); }
}
