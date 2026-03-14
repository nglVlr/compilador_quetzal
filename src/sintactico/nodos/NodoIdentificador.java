package sintactico.nodos;
import sintactico.Nodo; import sintactico.VisitanteNodo;
/** Referencia a una variable: nombre */
public class NodoIdentificador extends Nodo {
    public final String nombre;
    public NodoIdentificador(int linea, String nombre) { super(linea); this.nombre = nombre; }
    @Override public <T> T aceptar(VisitanteNodo<T> v) { return v.visitarIdentificador(this); }
}
