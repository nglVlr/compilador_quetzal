package sintactico.nodos;

import sintactico.Nodo;
import sintactico.VisitanteNodo;

/**
 * Declaración de un objeto JSON nativo.
 * Ejemplo:
 *   jsn var persona = { nombre: "Ana", edad: 30 }
 */
public class NodoDeclaracionJsn extends Nodo {
    public final boolean mutable;
    public final String  nombre;
    public final Nodo    valor;  // NodoLiteralJsn

    public NodoDeclaracionJsn(int linea, boolean mutable, String nombre, Nodo valor) {
        super(linea);
        this.mutable = mutable;
        this.nombre  = nombre;
        this.valor   = valor;
    }

    @Override public <T> T aceptar(VisitanteNodo<T> v) { return v.visitarDeclaracionJsn(this); }
}
