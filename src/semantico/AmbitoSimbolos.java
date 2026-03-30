package semantico;

import java.util.HashMap;
import java.util.Map;

public class AmbitoSimbolos {
    //Nombre descriptivo para depuracion
    private final String nombre;
    //Referencia al ambito que contiene a este
    private final AmbitoSimbolos padre;
    //Mapa de simbolos declarados en este nivel
    private final Map<String, SimboloEntrada> simbolos;

    //Crea un nuevo ambito
    public AmbitoSimbolos(String nombre, AmbitoSimbolos padre) {
        this.nombre = nombre;
        this.padre = padre;
        simbolos = new HashMap<>();
    }

    //Declara un nuevo simbolo en este ambito
    public void declarar(SimboloEntrada entrada){
        simbolos.put(entrada.getNombre(), entrada);
    }

    //Busca un simbolo unicamente en este nivel del ambito
    public  SimboloEntrada buscarEnNivelActual(String nombre){
        return simbolos.get(nombre);
    }

    /*
    Busca un simbolo en este ambito y si no lo encuentra sube hacia el padre recursivamente
    hasta encontrarlo ollegar al ambito global sin resultado
    */
    public SimboloEntrada buscar(String nombre){
        SimboloEntrada encontrado = simbolos.get(nombre);
        if(encontrado != null){
            return encontrado;
        }
        if(padre != null){
            return padre.buscar(nombre);
        }
        return null;
    }

    /*
    Indica si un simbolo ya fue declarado en este nivel exacto de ambito
    no sube hacia el padre.
     */
    public boolean existeEnNivelActual(String nombre){
        return simbolos.containsKey(nombre);
    }

    public String getNombre() {
        return nombre;
    }

    public AmbitoSimbolos getPadre() {
        return padre;
    }

    public boolean esGlobal(){
        return padre == null;
    }

    //toString para depuracion
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Ambito '").append(nombre).append("' ");
        sb.append(" [").append(simbolos.size()).append(" simbolo(s)] ");
        if(!simbolos.isEmpty()){
            sb.append(": ");
            simbolos.values().forEach(s -> sb.append(s.getNombre()).append(" "));
        }
        return sb.toString();
    }
}
