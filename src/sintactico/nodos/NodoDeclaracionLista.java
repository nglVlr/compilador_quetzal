package sintactico.nodos;

import sintactico.Nodo;
import sintactico.VisitanteNodo;

/**
 * Declaración de lista tipada o no tipada.
 * Ejemplos:
 *   lista<entero> numeros = [1, 2, 3]
 *   lista<texto> var nombres = ["Ana", "Luis"]
 *   lista valores = [1, "dos", verdadero]
 *   lista<texto> vacia = []
 */
public class NodoDeclaracionLista extends Nodo {
    public final String tipoElemento; // "entero", "texto", etc. Null si no tipada
    public final boolean mutable;
    public final String  nombre;
    public final Nodo    valor;       // NodoLiteralLista o null

    public NodoDeclaracionLista(int linea, String tipoElemento, boolean mutable, String nombre, Nodo valor) {
        super(linea);
        this.tipoElemento = tipoElemento;
        this.mutable      = mutable;
        this.nombre       = nombre;
        this.valor        = valor;
    }

    @Override public <T> T aceptar(VisitanteNodo<T> v) { return v.visitarDeclaracionLista(this); }
}
