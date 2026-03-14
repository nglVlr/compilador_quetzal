package sintactico.nodos;

import sintactico.Nodo;
import sintactico.VisitanteNodo;

/**
 * Asignación simple: nombre = expresion
 * También cubre: ambiente.campo = expresion  y  obj.campo = expresion
 */
public class NodoAsignacion extends Nodo {
    public final Nodo   objetivo; // NodoIdentificador o NodoAccesoMiembro
    public final Nodo   valor;

    public NodoAsignacion(int linea, Nodo objetivo, Nodo valor) {
        super(linea);
        this.objetivo = objetivo;
        this.valor    = valor;
    }

    @Override public <T> T aceptar(VisitanteNodo<T> v) { return v.visitarAsignacion(this); }
}
