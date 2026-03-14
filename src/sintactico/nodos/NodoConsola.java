package sintactico.nodos;
import sintactico.Nodo; import sintactico.VisitanteNodo;
import java.util.List;
/**
 * Llamada a consola: consola.metodo(args)
 *
 * Métodos posibles:
 *   mostrar, mostrar_exito, mostrar_error, mostrar_advertencia, mostrar_informacion
 *   pedir, pedir_secreto
 */
public class NodoConsola extends Nodo {
    public final String     metodo;     // "mostrar", "mostrar_exito", "pedir", etc.
    public final List<Nodo> argumentos;

    public NodoConsola(int linea, String metodo, List<Nodo> argumentos) {
        super(linea);
        this.metodo     = metodo;
        this.argumentos = argumentos;
    }
    @Override public <T> T aceptar(VisitanteNodo<T> v) { return v.visitarConsola(this); }
}
