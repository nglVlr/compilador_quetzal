package sintactico.nodos;
import sintactico.Nodo; import sintactico.VisitanteNodo;
import java.util.LinkedHashMap;
/**
 * Literal objeto JSON: { clave: valor, ... }
 * Usa LinkedHashMap para preservar el orden de las claves.
 */
public class NodoLiteralJsn extends Nodo {
    public final LinkedHashMap<String, Nodo> pares;
    public NodoLiteralJsn(int linea, LinkedHashMap<String, Nodo> pares) { super(linea); this.pares = pares; }
    @Override public <T> T aceptar(VisitanteNodo<T> v) { return v.visitarLiteralJsn(this); }
}
