package sintactico.nodos;
import sintactico.Nodo; import sintactico.VisitanteNodo;
/**
 * Texto interpolado: t"Hola {nombre}, tienes {edad} años"
 * El valor guarda el contenido crudo (con {expresiones} como texto).
 * El generador de código lo procesará para armar un String.format o concatenación.
 */
public class NodoLiteralTextoInterp extends Nodo {
    public final String plantilla; // contenido crudo, ej: "Hola {nombre}, tienes {edad} años"
    public NodoLiteralTextoInterp(int linea, String plantilla) { super(linea); this.plantilla = plantilla; }
    @Override public <T> T aceptar(VisitanteNodo<T> v) { return v.visitarLiteralTextoInterp(this); }
}
