package sintactico.nodos;

import sintactico.Nodo;
import sintactico.VisitanteNodo;
import java.util.List;

/** nuevo NombreObjeto(arg1, arg2) */
public class NodoNuevoObjeto extends Nodo {
    public final String     tipoObjeto;
    public final List<Nodo> argumentos;

    public NodoNuevoObjeto(int linea, String tipoObjeto, List<Nodo> argumentos) {
        super(linea);
        this.tipoObjeto = tipoObjeto;
        this.argumentos = argumentos;
    }

    @Override public <T> T aceptar(VisitanteNodo<T> v) { return v.visitarNuevoObjeto(this); }
}
