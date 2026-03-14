package sintactico.nodos;

import sintactico.Nodo;
import sintactico.VisitanteNodo;
import java.util.List;

/**
 * Declaración de objeto (clase).
 *
 * Ejemplo:
 *   objeto Usuario {
 *       privado:
 *           texto nombre
 *       publico:
 *           Usuario(texto nombre) { ambiente.nombre = nombre }
 *           texto saludo() { retornar t"Hola {ambiente.nombre}" }
 *   }
 */
public class NodoDeclaracionObjeto extends Nodo {
    public final String                      nombre;
    public final List<NodoDeclaracionVariable> atributosPrivados;
    public final List<NodoDeclaracionVariable> atributosPublicos;
    public final NodoDeclaracionFuncion        constructor;       // puede ser null
    public final List<NodoDeclaracionFuncion>  metodosPublicos;
    public final List<NodoDeclaracionFuncion>  metodosPrivados;

    public NodoDeclaracionObjeto(int linea, String nombre,
                                  List<NodoDeclaracionVariable> atributosPrivados,
                                  List<NodoDeclaracionVariable> atributosPublicos,
                                  NodoDeclaracionFuncion constructor,
                                  List<NodoDeclaracionFuncion> metodosPublicos,
                                  List<NodoDeclaracionFuncion> metodosPrivados) {
        super(linea);
        this.nombre             = nombre;
        this.atributosPrivados  = atributosPrivados;
        this.atributosPublicos  = atributosPublicos;
        this.constructor        = constructor;
        this.metodosPublicos    = metodosPublicos;
        this.metodosPrivados    = metodosPrivados;
    }

    @Override public <T> T aceptar(VisitanteNodo<T> v) { return v.visitarDeclaracionObjeto(this); }
}
