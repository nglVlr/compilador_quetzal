package sintactico.nodos;

import sintactico.Nodo;
import sintactico.VisitanteNodo;
import java.util.List;

/**
 * importar { Simbolo1, Simbolo2 } desde "ruta"
 */
public class NodoImportar extends Nodo {
    public final List<String> simbolos;
    public final String       ruta;

    public NodoImportar(int linea, List<String> simbolos, String ruta) {
        super(linea);
        this.simbolos = simbolos;
        this.ruta     = ruta;
    }

    @Override public <T> T aceptar(VisitanteNodo<T> v) { return v.visitarImportar(this); }
}
