package sintactico.nodos;

import sintactico.Nodo;
import sintactico.VisitanteNodo;

/** Expresión ternaria: condicion ? siVerdadero : siFalso */
public class NodoTernaria extends Nodo {
    public final Nodo condicion;
    public final Nodo siVerdadero;
    public final Nodo siFalso;

    public NodoTernaria(int linea, Nodo condicion, Nodo siVerdadero, Nodo siFalso) {
        super(linea);
        this.condicion    = condicion;
        this.siVerdadero  = siVerdadero;
        this.siFalso      = siFalso;
    }

    @Override public <T> T aceptar(VisitanteNodo<T> v) { return v.visitarTernaria(this); }
}
