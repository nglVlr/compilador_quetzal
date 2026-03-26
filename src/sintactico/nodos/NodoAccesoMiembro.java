package sintactico.nodos;

import sintactico.Nodo;
import sintactico.VisitanteNodo;

/**
 * Acceso a miembro de objeto: objeto.campo
 * Ejemplos:
 *   persona.nombre
 *   ambiente.edad
 *   configuracion.ajustes.tema
 */
public class NodoAccesoMiembro extends Nodo {
    public final Nodo   objeto;
    public final String campo;

    public NodoAccesoMiembro(int linea, Nodo objeto, String campo) {
        super(linea);
        this.objeto = objeto;
        this.campo  = campo;
    }

    @Override public <T> T aceptar(VisitanteNodo<T> v) { return v.visitarAccesoMiembro(this); }
}
