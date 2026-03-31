package semantico;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Interfas publica de la tabla de simbolos
 * Maneja una pila de AmbitoSimbolos. Cada vez que el analizador
 * semantico entra aun bloque nuevo empuja un ambito; al salir lo
 * saca. La busqueda de simbolo sube automaticamente por
 * la cadena de ambito hsata el global.
 * Responsabilidades de esta clase:
 *    - Crear y destruir ámbitos
 *    - Registrar símbolos en el ámbito actual
 *    - Buscar símbolos en la cadena de ámbitos
 *    - Detectar redeclaraciones en el mismo ámbito
 *
 *  Lo que NO hace esta clase:
 *    - No verifica tipos (eso es del AnalizadorSemantico)
 *    - No genera errores (solo informa si un símbolo existe o no)
 *    - No conoce el AST
 */
public class TablaSimbolos {
    /**
     * Pila de ambito activo
     * El tope (peekFirst) es siembre el ambito mas interno actual
     * El fondo se ssiempre el ambito global
     */
    private final Deque<AmbitoSimbolos> pila;

    /**
    * Crea la tabla de simbolos e inicializa el ambito global,
    * registra automaticamente los simbolos predefinidos del lenguaje
    */
    public TablaSimbolos() {
        this.pila = new ArrayDeque<>();
        //Crea el ambito global (padre null)
        AmbitoSimbolos global = new AmbitoSimbolos("global", null);
        pila.push(global);
        //Registra simbolos predefinidos
        registrarSimbolosPredefinidos();
    }

    /**
     * Entra a un nuevo ambito creando un hijo acutal,
     * Se llama al entrar a cualquier bloque del lenjuaje
     */
    public void entrarAmbito(String nombre){
        AmbitoSimbolos nuevo = new AmbitoSimbolos(nombre, ambitoActual());
        pila.push(nuevo);
    }

    /**
     * Sale del ambito actual descartandolo,
     * se llama al salir de cualquier bloque
     */
    public void salirAmbito(){
        if(pila.size() > 1){
            pila.pop();
        }
    }

    //Retorna el ambito que esta actualmente en el tope de la pila
    public AmbitoSimbolos ambitoActual(){
        return pila.peek();
    }

    //Retorna true si actualmente se esta en el ambito global
    public boolean enAmbitoGlobal(){
        return pila.size() == 1;
    }

    //Retorna la profundidad actual de anidamiento (1 = solo global)
    public int profundidad(){
        return pila.size();
    }

    /**
     * Declaracion de simbolos
     * Intenta declarar simbolos en el ambito actual
     * Antes de declarar verifica si ya existen en el mismo ambito
     * Si existe retorna falso para que en AnalizadorSemantico genere el error.
     */

    public boolean declarar(SimboloEntrada entrada){
        AmbitoSimbolos actual = ambitoActual();
        if(actual.existeEnNivelActual(entrada.getNombre())){
            return false;
        }
        actual.declarar(entrada);
        return true;
    }

    /**
     * Busqueda de simbolos
     * Busca un simbolo subiendo por toda la cadena de ambitos
     * Empieza en el ambito actual y sube hasta el global
     */
    public SimboloEntrada buscar(String nombre){
        return ambitoActual().buscar(nombre);
    }

    /**
     * Busca un simbolo unicamente en el ambito actual sin subir
     * Se usa para  verifcar redeclaracione antes de declarar()
     */
    public SimboloEntrada buscarEnActual(String nombre){
        return ambitoActual().buscarEnNivelActual(nombre);
    }

    /**
     * Indica si un simbolo existe en algun ambito visible desde el actual
     */
    public boolean existe(String nombre){
        return buscar(nombre) != null;
    }

    /**
     * Simbolos predefinidos del lenguaje
     * Registra en el ambito global los simbolos que el lenguaje
     * ofrece sin necesidad de declaracion ni importacion
     */
    private void registrarSimbolosPredefinidos(){
        //Consola disponible globalmente, sin necesidad de importar
        SimboloEntrada consola = new SimboloEntrada(
                "consola",
                "consola",
                null,
                false,
                TipoSimbolo.VARIABLE,
                0,
                null,
                null
        );
        ambitoActual().declarar(consola);
    }

    //toString para depuracion
    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("TablaSimbolo [profundidad=").append(pila.size()).append("]\n");
        int nivel = pila.size();
        for (AmbitoSimbolos ambito : pila) {
            sb.append(" ".repeat(nivel)).append(ambito.toString()).append("\n");
            nivel --;
        }
        return sb.toString();
    }
}
