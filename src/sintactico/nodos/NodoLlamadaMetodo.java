package sintactico.nodos;

import sintactico.Nodo;
import sintactico.VisitanteNodo;
import java.util.List;

/**
 * Llamada a método sobre un objeto o cadena de acceso.
 * Ejemplos:
 *   persona.saludar()
 *   frutas.agregar("pera")
 *   consola.mostrar("hola")
 *   persona.direcciones[0].ciudad
 */
public class NodoLlamadaMetodo extends Nodo {
    public final Nodo       objeto;      // el receptor (NodoIdentificador, NodoAccesoMiembro, etc.)
    public final String     metodo;      // nombre del método
    public final List<Nodo> argumentos;

    public NodoLlamadaMetodo(int linea, Nodo objeto, String metodo, List<Nodo> argumentos) {
        super(linea);
        this.objeto     = objeto;
        this.metodo     = metodo;
        this.argumentos = argumentos;
    }

    @Override public <T> T aceptar(VisitanteNodo<T> v) { return v.visitarLlamadaMetodo(this); }
}
