package sintactico.nodos;

import sintactico.Nodo;
import sintactico.VisitanteNodo;

/**
 * Asignación compuesta: nombre += expr  /  -= *= /= %=
 */
public class NodoAsignacionCompuesta extends Nodo {
    public final Nodo   objetivo;   // NodoIdentificador
    public final String operador;   // "+=", "-=", "*=", "/=", "%="
    public final Nodo   valor;

    public NodoAsignacionCompuesta(int linea, Nodo objetivo, String operador, Nodo valor) {
        super(linea);
        this.objetivo = objetivo;
        this.operador = operador;
        this.valor    = valor;
    }

    @Override public <T> T aceptar(VisitanteNodo<T> v) { return v.visitarAsignacionCompuesta(this); }
}
