package sintactico.nodos;

import sintactico.Nodo;
import sintactico.VisitanteNodo;

// ── MIENTRAS ──────────────────────────────────────────────────────────────────

/**
 * Bucle mientras (cond) { ... }
 */
public class NodoMientras extends Nodo {
    public final Nodo       condicion;
    public final NodoBloque cuerpo;

    public NodoMientras(int linea, Nodo condicion, NodoBloque cuerpo) {
        super(linea);
        this.condicion = condicion;
        this.cuerpo    = cuerpo;
    }

    @Override public <T> T aceptar(VisitanteNodo<T> v) { return v.visitarMientras(this); }
}
