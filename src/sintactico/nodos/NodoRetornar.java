package sintactico.nodos;

import sintactico.Nodo;
import sintactico.VisitanteNodo;

/** retornar [expresion] */
public class NodoRetornar extends Nodo {
    public final Nodo valor; // puede ser null (retornar sin valor)

    public NodoRetornar(int linea, Nodo valor) {
        super(linea);
        this.valor = valor;
    }

    @Override public <T> T aceptar(VisitanteNodo<T> v) { return v.visitarRetornar(this); }
}
