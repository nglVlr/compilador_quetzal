package sintactico.nodos;

import sintactico.Nodo;
import sintactico.VisitanteNodo;

/**
 * Manejo de errores: intentar { } capturar (e) { } finalmente { }
 */
public class NodoIntentar extends Nodo {
    public final NodoBloque cuerpoIntentar;
    public final String     variableError;   // nombre de la variable en capturar(e)
    public final NodoBloque cuerpoCapturar;  // puede ser null
    public final NodoBloque cuerpoFinalmente; // puede ser null

    public NodoIntentar(int linea, NodoBloque cuerpoIntentar,
                         String variableError, NodoBloque cuerpoCapturar,
                         NodoBloque cuerpoFinalmente) {
        super(linea);
        this.cuerpoIntentar   = cuerpoIntentar;
        this.variableError    = variableError;
        this.cuerpoCapturar   = cuerpoCapturar;
        this.cuerpoFinalmente = cuerpoFinalmente;
    }

    @Override public <T> T aceptar(VisitanteNodo<T> v) { return v.visitarIntentar(this); }
}
