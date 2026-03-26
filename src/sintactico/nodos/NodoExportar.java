package sintactico.nodos;

import sintactico.Nodo;
import sintactico.VisitanteNodo;
import java.util.List;

/**
 * exportar { simbolo1, simbolo2 }
 */
public class NodoExportar extends Nodo {
    public final List<String> simbolos;

    public NodoExportar(int linea, List<String> simbolos) {
        super(linea);
        this.simbolos = simbolos;
    }

    @Override public <T> T aceptar(VisitanteNodo<T> v) { return v.visitarExportar(this); }
}
