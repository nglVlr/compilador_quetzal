package sintactico.nodos;

import sintactico.Nodo;
import sintactico.VisitanteNodo;

/**
 * Declaración de variable primitiva.
 * Ejemplos:
 *   entero edad = 27
 *   texto var nombre = "Ana"
 *   numero pi = 3.14
 *   log var activo = verdadero
 */
public class NodoDeclaracionVariable extends Nodo {
    public final String tipo;       // "entero", "numero", "texto", "log", "vacio"
    public final boolean mutable;   // true si tiene "var"
    public final String nombre;     // nombre del identificador
    public final Nodo   valor;      // expresión del lado derecho (puede ser null si no hay =)

    public NodoDeclaracionVariable(int linea, String tipo, boolean mutable, String nombre, Nodo valor) {
        super(linea);
        this.tipo    = tipo;
        this.mutable = mutable;
        this.nombre  = nombre;
        this.valor   = valor;
    }

    @Override public <T> T aceptar(VisitanteNodo<T> v) { return v.visitarDeclaracionVariable(this); }
}
