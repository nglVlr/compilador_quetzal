package sintactico.nodos;

import sintactico.Nodo;
import sintactico.VisitanteNodo;

/**
 * Bucle para elemento en coleccion { ... }
 * Equivalente a for-each en Java.
 *
 * Ejemplo:
 *   para elemento en nombres { consola.mostrar(elemento) }
 */
public class NodoParaEn extends Nodo {
    public final String     variable;    // nombre de la variable iteradora
    public final Nodo       coleccion;   // expresión que da la lista
    public final NodoBloque cuerpo;

    public NodoParaEn(int linea, String variable, Nodo coleccion, NodoBloque cuerpo) {
        super(linea);
        this.variable   = variable;
        this.coleccion  = coleccion;
        this.cuerpo     = cuerpo;
    }

    @Override public <T> T aceptar(VisitanteNodo<T> v) { return v.visitarParaEn(this); }
}
