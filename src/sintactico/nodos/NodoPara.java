package sintactico.nodos;

import sintactico.Nodo;
import sintactico.VisitanteNodo;

/**
 * Bucle para clásico con inicialización, condición y paso.
 * Ejemplo: para (entero i = 0; i < 10; i++) { ... }
 *
 * init      → NodoDeclaracionVariable o NodoAsignacion
 * condicion → expresión booleana
 * paso      → NodoIncrementoDecremento o NodoAsignacionCompuesta
 */
public class NodoPara extends Nodo {
    public final Nodo       init;
    public final Nodo       condicion;
    public final Nodo       paso;
    public final NodoBloque cuerpo;

    public NodoPara(int linea, Nodo init, Nodo condicion, Nodo paso, NodoBloque cuerpo) {
        super(linea);
        this.init      = init;
        this.condicion = condicion;
        this.paso      = paso;
        this.cuerpo    = cuerpo;
    }

    @Override public <T> T aceptar(VisitanteNodo<T> v) { return v.visitarPara(this); }
}
