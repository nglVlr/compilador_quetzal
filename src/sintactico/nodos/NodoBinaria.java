package sintactico.nodos;

import sintactico.Nodo;
import sintactico.VisitanteNodo;

/** Expresión binaria: izq OP der */
public class NodoBinaria extends Nodo {
    public final Nodo   izquierda;
    public final String operador;   // "+", "-", "*", "/", "%", "==", "!=", "<", ">", "<=", ">=", "y", "o", "&&", "||"
    public final Nodo   derecha;

    public NodoBinaria(int linea, Nodo izquierda, String operador, Nodo derecha) {
        super(linea);
        this.izquierda = izquierda;
        this.operador  = operador;
        this.derecha   = derecha;
    }

    @Override public <T> T aceptar(VisitanteNodo<T> v) { return v.visitarBinaria(this); }
}
