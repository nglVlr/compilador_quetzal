package sintactico.nodos;

import sintactico.Nodo;
import sintactico.VisitanteNodo;

/**
 * Incremento / decremento postfijo.
 * Ejemplos:
 *   valor++
 *   valor--
 */
public class NodoIncrementoDecremento extends Nodo {
    public final Nodo   objetivo;   // NodoIdentificador
    public final String operador;   // "++" o "--"

    public NodoIncrementoDecremento(int linea, Nodo objetivo, String operador) {
        super(linea);
        this.objetivo = objetivo;
        this.operador = operador;
    }

    @Override public <T> T aceptar(VisitanteNodo<T> v) { return v.visitarIncrementoDecremento(this); }
}
