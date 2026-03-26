package sintactico.nodos;

import sintactico.Nodo;
import sintactico.VisitanteNodo;
import java.util.List;

/**
 * Llamada a función: nombre(arg1, arg2, ...)
 * Ejemplo: sumar(a, b)
 */
public class NodoLlamadaFuncion extends Nodo {
    public final String     nombre;
    public final List<Nodo> argumentos;

    public NodoLlamadaFuncion(int linea, String nombre, List<Nodo> argumentos) {
        super(linea);
        this.nombre     = nombre;
        this.argumentos = argumentos;
    }

    @Override public <T> T aceptar(VisitanteNodo<T> v) { return v.visitarLlamadaFuncion(this); }
}
