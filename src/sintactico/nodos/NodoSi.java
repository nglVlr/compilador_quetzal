package sintactico.nodos;

import sintactico.Nodo;
import sintactico.VisitanteNodo;
import java.util.List;

// ── SI / SINO_SI / SINO ───────────────────────────────────────────────────────

/**
 * Condicional si / sino_si / sino.
 *
 * Ejemplo:
 *   si (cond) { ... } sino_si (cond2) { ... } sino { ... }
 *
 * ramasElseIf: lista de pares (condicion, bloque) para los sino_si
 * cuerpoElse : bloque del sino final (puede ser null)
 */
public class NodoSi extends Nodo {
    public final Nodo           condicion;
    public final NodoBloque     cuerpoIf;
    public final List<RamaElseIf> ramasElseIf;
    public final NodoBloque     cuerpoElse;  // puede ser null

    public NodoSi(int linea, Nodo condicion, NodoBloque cuerpoIf,
                  List<RamaElseIf> ramasElseIf, NodoBloque cuerpoElse) {
        super(linea);
        this.condicion    = condicion;
        this.cuerpoIf     = cuerpoIf;
        this.ramasElseIf  = ramasElseIf;
        this.cuerpoElse   = cuerpoElse;
    }

    /** Par condicion+bloque para sino_si */
    public static class RamaElseIf {
        public final Nodo       condicion;
        public final NodoBloque cuerpo;
        public RamaElseIf(Nodo condicion, NodoBloque cuerpo) {
            this.condicion = condicion;
            this.cuerpo    = cuerpo;
        }
    }

    @Override public <T> T aceptar(VisitanteNodo<T> v) { return v.visitarSi(this); }
}
