package sintactico;

/**
 * Excepción lanzada cuando el Analizador Sintáctico encuentra
 * un token inesperado o una construcción inválida.
 */
public class ErrorSintactico extends RuntimeException {
    private final int linea;
    private final int columna;

    public ErrorSintactico(String mensaje, int linea, int columna) {
        super(String.format("[Error Sintáctico] Línea %d, col %d: %s", linea, columna, mensaje));
        this.linea   = linea;
        this.columna = columna;
    }

    public int getLinea()   { return linea;   }
    public int getColumna() { return columna; }
}
