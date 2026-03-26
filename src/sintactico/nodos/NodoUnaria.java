package sintactico.nodos;

import sintactico.Nodo;
import sintactico.VisitanteNodo;

// ── UNARIA ────────────────────────────────────────────────────────────────────

/** Expresión unaria: -expr  !expr  no expr */
public class NodoUnaria extends Nodo {
    public final String operador;  // "-", "!", "no"
    public final Nodo   operando;

    public NodoUnaria(int linea, String operador, Nodo operando) {
        super(linea);
        this.operador = operador;
        this.operando = operando;
    }

    @Override public <T> T aceptar(VisitanteNodo<T> v) { return v.visitarUnaria(this); }
}
