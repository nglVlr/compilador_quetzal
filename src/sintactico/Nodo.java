package sintactico;

/**
 * Nodo base del Árbol de Sintaxis Abstracta (AST).
 *
 * Cada construcción del lenguaje Quetzal (declaración, expresión,
 * sentencia, etc.) es un subclase de Nodo.
 *
 * El campo linea permite dar mensajes de error con ubicación exacta.
 */
public abstract class Nodo {
    public final int linea;

    protected Nodo(int linea) {
        this.linea = linea;
    }

    /**
     * Método de visitante para el patrón Visitor.
     * Lo usaremos en el Semántico y el Generador de código.
     */
    public abstract <T> T aceptar(VisitanteNodo<T> visitante);
}
