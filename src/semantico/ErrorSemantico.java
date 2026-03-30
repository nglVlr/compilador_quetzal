package semantico;

public class ErrorSemantico {
    private final String mensaje;
    private final int linea;
    private final int columna;

    public ErrorSemantico(String mensaje, int linea, int columna) {
        this.mensaje = mensaje;
        this.linea = linea;
        this.columna = columna;
    }

    public ErrorSemantico(String mensaje, int linea) {
        this(mensaje, linea, 0);
    }

    public String getMensaje() {
        return mensaje;
    }

    public int getLinea() {
        return linea;
    }

    public int getColumna() {
        return columna;
    }

    @Override
    public String toString() {
        return String.format("[Error Semántico] Línea %d, col %d: %s",
                linea, columna, mensaje);
    }
}
