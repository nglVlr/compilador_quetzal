package sintactico.nodos;

import sintactico.Nodo;
import sintactico.VisitanteNodo;
import java.util.List;

/**
 * Declaración de función.
 *
 * Ejemplos:
 *   entero funcion sumar(entero a, entero b) { retornar a + b }
 *   vacio funcion saludar(texto nombre) { consola.mostrar(nombre) }
 *   async funcion obtenerDatos() { ... }
 */
public class NodoDeclaracionFuncion extends Nodo {
    public final String          tipoRetorno;  // "entero", "texto", "vacio", etc.
    public final boolean         esAsync;
    public final String          nombre;
    public final List<Parametro> parametros;
    public final NodoBloque      cuerpo;

    public NodoDeclaracionFuncion(int linea, String tipoRetorno, boolean esAsync,
                                   String nombre, List<Parametro> parametros, NodoBloque cuerpo) {
        super(linea);
        this.tipoRetorno = tipoRetorno;
        this.esAsync     = esAsync;
        this.nombre      = nombre;
        this.parametros  = parametros;
        this.cuerpo      = cuerpo;
    }

    /** Representa un parámetro: tipo nombre */
    public static class Parametro {
        public final String tipo;
        public final String nombre;
        public Parametro(String tipo, String nombre) {
            this.tipo   = tipo;
            this.nombre = nombre;
        }
        @Override public String toString() { return tipo + " " + nombre; }
    }

    @Override public <T> T aceptar(VisitanteNodo<T> v) { return v.visitarDeclaracionFuncion(this); }
}
