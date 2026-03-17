package lexico;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Analizador Léxico
 * Características del lenguaje que maneja:
 *  - Sin punto y coma las nuevas líneas delimitan instrucciones
 *  - Comentarios de línea: //
 *  - Comentarios de bloque: /* ... * /
 *  - Textos: "hola"
 *  - Textos interpolados: t"Hola {nombre}"
 *  - Tipos: entero, numero, texto, log, vacio, lista, jsn
 *  - Mutabilidad: var (después del tipo)
 *  - Operadores aritméticos: + - * / %
 *  - Incremento/Decremento: ++ --
 *  - Comparación: == != < > <= >=
 *  - Asignación compuesta: += -= *= /= %=
 *  - Lógicos en español: y, o, no
 *  - Lógicos simbólicos: && || !
 *  - Ternario: condicion ? valor1 : valor2
 *  - OOP: objeto, nuevo, ambiente, privado, publico, estatico
 *  - Módulos: importar, exportar, desde
 *  - Excepciones: intentar, capturar, finalmente, lanzar
 *  - Funciones: funcion, async, esperar, retornar
 *  - Bucles: mientras, para, en, hacer, romper, continuar
 */
public class AnalizadorLexico {

    // ── Tabla de palabras reservadas
    private static final Map<String, TipoToken> PALABRAS_RESERVADAS = new HashMap<>();

    static {
        // --- Tipos de datos
        PALABRAS_RESERVADAS.put("entero",     TipoToken.ENTERO);
        PALABRAS_RESERVADAS.put("numero",     TipoToken.NUMERO);
        PALABRAS_RESERVADAS.put("número",     TipoToken.NUMERO);
        PALABRAS_RESERVADAS.put("texto",      TipoToken.TEXTO);
        PALABRAS_RESERVADAS.put("log",        TipoToken.LOG);
        PALABRAS_RESERVADAS.put("vacio",      TipoToken.VACIO);
        PALABRAS_RESERVADAS.put("lista",      TipoToken.LISTA);
        PALABRAS_RESERVADAS.put("jsn",        TipoToken.JSN);
        PALABRAS_RESERVADAS.put("consola",     TipoToken.CONSOLA);
        PALABRAS_RESERVADAS.put("pedir",       TipoToken.PEDIR);
        PALABRAS_RESERVADAS.put ("mostrar",    TipoToken.MOSTRAR);

        // --- Mutabilidad ---
        PALABRAS_RESERVADAS.put("var",        TipoToken.VAR);

        // --- Valores booleanos y nulo ---
        PALABRAS_RESERVADAS.put("verdadero",  TipoToken.VERDADERO);
        PALABRAS_RESERVADAS.put("falso",      TipoToken.FALSO);
        PALABRAS_RESERVADAS.put("nulo",       TipoToken.NULO);

        // --- Control de flujo ---
        PALABRAS_RESERVADAS.put("si",         TipoToken.SI);
        PALABRAS_RESERVADAS.put("sino",       TipoToken.SINO);
        PALABRAS_RESERVADAS.put("sino_si",    TipoToken.SINO_SI);
        PALABRAS_RESERVADAS.put("mientras",   TipoToken.MIENTRAS);
        PALABRAS_RESERVADAS.put("para",       TipoToken.PARA);
        PALABRAS_RESERVADAS.put("en",         TipoToken.EN);
        PALABRAS_RESERVADAS.put("hacer",      TipoToken.HACER);
        PALABRAS_RESERVADAS.put("retornar",   TipoToken.RETORNAR);
        PALABRAS_RESERVADAS.put("romper",     TipoToken.ROMPER);
        PALABRAS_RESERVADAS.put("continuar",  TipoToken.CONTINUAR);

        // --- Funciones ---
        PALABRAS_RESERVADAS.put("funcion",    TipoToken.FUNCION);
        PALABRAS_RESERVADAS.put("async",      TipoToken.ASYNC);
        PALABRAS_RESERVADAS.put("esperar",    TipoToken.ESPERAR);

        // --- OOP ---
        PALABRAS_RESERVADAS.put("objeto",     TipoToken.OBJETO);
        PALABRAS_RESERVADAS.put("nuevo",      TipoToken.NUEVO);
        PALABRAS_RESERVADAS.put("ambiente",   TipoToken.AMBIENTE);
        PALABRAS_RESERVADAS.put("privado",    TipoToken.PRIVADO);
        PALABRAS_RESERVADAS.put("publico",    TipoToken.PUBLICO);
        PALABRAS_RESERVADAS.put("estatico",   TipoToken.ESTATICO);

        // --- Módulos ---
        PALABRAS_RESERVADAS.put("importar",   TipoToken.IMPORTAR);
        PALABRAS_RESERVADAS.put("exportar",   TipoToken.EXPORTAR);
        PALABRAS_RESERVADAS.put("desde",      TipoToken.DESDE);

        // --- Manejo de errores excepciones
        PALABRAS_RESERVADAS.put("intentar",   TipoToken.INTENTAR);
        PALABRAS_RESERVADAS.put("capturar",   TipoToken.CAPTURAR);
        PALABRAS_RESERVADAS.put("finalmente", TipoToken.FINALMENTE);
        PALABRAS_RESERVADAS.put("lanzar",     TipoToken.LANZAR);

        // --- Operadores logicos en español
        PALABRAS_RESERVADAS.put("y",          TipoToken.Y);
        PALABRAS_RESERVADAS.put("o",          TipoToken.O);
        PALABRAS_RESERVADAS.put("no",         TipoToken.NO);
    }

    // ---Estado interno
    private final String       fuente;
    private int                pos;
    private int                linea;
    private int                columna;
    private final List<Token>  tokens;
    private final List<String> errores;

    public AnalizadorLexico(String fuente) {
        this.fuente   = fuente;
        this.pos      = 0;
        this.linea    = 1;
        this.columna  = 1;
        this.tokens   = new ArrayList<>();
        this.errores  = new ArrayList<>();
    }

    // ---API publica

    /**
     * Ejecuta el análisis léxico y retorna todos los tokens encontrados.
     * Los tokens EOF y NUEVA_LINEA también se incluyen para el parser.
     */
    public List<Token> analizar() {
        while (!fin()) {
            saltarEspacios();
            if (fin()) break;

            char c = actual();

            // Comentario de línea: //
            if (c == '/' && siguiente() == '/') {
                leerComentarioLinea();

            // Comentario de bloque: /* ... */
            } else if (c == '/' && siguiente() == '*') {
                leerComentarioBloque();

            // Nueva línea (importante en Quetzal: no hay punto y coma)
            } else if (c == '\n') {
                tokens.add(crearToken(TipoToken.NUEVA_LINEA, "\\n"));
                avanzar();

            // Texto interpolado: t"..."
            } else if (c == 't' && siguiente() == '"') {
                leerTextoInterpolado();

            // Cadena de texto: "..."
            } else if (c == '"') {
                leerTexto();

            // Número: 42 o 3.14
            } else if (Character.isDigit(c)) {
                leerNumero();

            // Identificador o palabra reservada
            } else if (esInicioIdentificador(c)) {
                leerIdentificador();

            // Símbolo / operador
            } else {
                leerSimbolo();
            }
        }

        tokens.add(crearToken(TipoToken.EOF, "EOF"));
        return tokens;
    }

    public List<String> getErrores()  { return errores;          }
    public boolean      hayErrores()  { return !errores.isEmpty(); }

    // --Lectura de comentarios

    private void leerComentarioLinea() {
        avanzar(); avanzar();
        StringBuilder sb = new StringBuilder("//");
        while (!fin() && actual() != '\n') {
            sb.append(actual());
            avanzar();
        }
        // Los comentarios no se agregan a token
    }

    private void leerComentarioBloque() {
        int lineaInicio = linea;
        int colInicio   = columna;
        avanzar(); avanzar(); // consumir /*
        while (!fin()) {
            if (actual() == '*' && siguiente() == '/') {
                avanzar(); avanzar(); // consumir */
                return;
            }
            avanzar();
        }
        // el comentario no fue cerrado
        errores.add("Error léxico en línea " + lineaInicio + ", col " + colInicio
                + ": comentario de bloque /* no fue cerrado.");
    }

    // ---Lectura de cadenas


    private void leerTexto() {
        int lineaInicio = linea;
        int colInicio   = columna;
        avanzar(); // consumir "
        StringBuilder sb = new StringBuilder();

        while (!fin() && actual() != '"') {
            // Error: salto de linea dentro de la cadena sin cerrar
            if (actual() == '\n') {
                errores.add("Error lexico en linea " + lineaInicio
                        + ", col " + colInicio
                        + ": cadena de texto no cerrada (falta \"\").");
                tokens.add(new Token(TipoToken.DESCONOCIDO, sb.toString(), lineaInicio, colInicio));
                return;
            }
            // Secuencias de escape básicas
            if (actual() == '\\' && !fin()) {
                avanzar();
                switch (actual()) {
                    case 'n':  sb.append('\n'); break;
                    case 't':  sb.append('\t'); break;
                    case '"':  sb.append('"');  break;
                    case '\\': sb.append('\\'); break;
                    default:   sb.append('\\'); sb.append(actual());
                }
            } else {
                sb.append(actual());
            }
            avanzar();
        }

        if (!fin()) avanzar();
        tokens.add(new Token(TipoToken.LITERAL_TEXTO, sb.toString(), lineaInicio, colInicio));
    }

    /** Lee: t"Hola {nombre}, tienes {edad} años" */
    private void leerTextoInterpolado() {
        int lineaInicio = linea;
        int colInicio   = columna;
        avanzar(); // consumir t
        avanzar(); // consumir "
        StringBuilder sb = new StringBuilder();

        while (!fin() && actual() != '"') {
            if (actual() == '\n') {
                errores.add("Error léxico en línea " + lineaInicio
                        + ": texto interpolado no cerrado (falta \").");
                tokens.add(new Token(TipoToken.LITERAL_TEXTO_INTERP, sb.toString(), lineaInicio, colInicio));
                return;
            }
            sb.append(actual());
            avanzar();
        }

        if (!fin()) avanzar(); // consumir " de cierre
        tokens.add(new Token(TipoToken.LITERAL_TEXTO_INTERP, sb.toString(), lineaInicio, colInicio));
    }

    // ----Lectura de números

    /** Lee: 42  o  3.14 */
    private void leerNumero() {
        int lineaInicio = linea;
        int colInicio   = columna;
        StringBuilder sb = new StringBuilder();
        boolean esReal = false;

        while (!fin() && Character.isDigit(actual())) {
            sb.append(actual());
            avanzar();
        }

        // ¿Tiene parte decimal? (.14)
        if (!fin() && actual() == '.' && Character.isDigit(siguiente())) {
            esReal = true;
            sb.append('.');
            avanzar();
            while (!fin() && Character.isDigit(actual())) {
                sb.append(actual());
                avanzar();
            }
        }

        TipoToken tipo = esReal ? TipoToken.LITERAL_REAL : TipoToken.LITERAL_ENTERO;
        tokens.add(new Token(tipo, sb.toString(), lineaInicio, colInicio));
    }

    // ----Lectura de identificadores y palabras reservadas--

    private void leerIdentificador() {
        int lineaInicio = linea;
        int colInicio   = columna;
        StringBuilder sb = new StringBuilder();

        while (!fin() && esCuerpoIdentificador(actual())) {
            sb.append(actual());
            avanzar();
        }

        String lexema = sb.toString();
        TipoToken tipo = PALABRAS_RESERVADAS.getOrDefault(lexema, TipoToken.IDENTIFICADOR);
        tokens.add(new Token(tipo, lexema, lineaInicio, colInicio));
    }

    // ----Lectura de símbolos y operadores---

    private void leerSimbolo() {
        int  col = columna;
        char c   = actual();

        switch (c) {
            // Delimitadores simples
            case '(': tokens.add(crearToken(TipoToken.PAREN_IZQ,    "(")); avanzar(); break;
            case ')': tokens.add(crearToken(TipoToken.PAREN_DER,    ")")); avanzar(); break;
            case '{': tokens.add(crearToken(TipoToken.LLAVE_IZQ,    "{")); avanzar(); break;
            case '}': tokens.add(crearToken(TipoToken.LLAVE_DER,    "}")); avanzar(); break;
            case '[': tokens.add(crearToken(TipoToken.CORCHETE_IZQ, "[")); avanzar(); break;
            case ']': tokens.add(crearToken(TipoToken.CORCHETE_DER, "]")); avanzar(); break;

            // Puntuación simple
            case ',': tokens.add(crearToken(TipoToken.COMA,         ",")); avanzar(); break;
            case '.': tokens.add(crearToken(TipoToken.PUNTO,        ".")); avanzar(); break;
            case '?': tokens.add(crearToken(TipoToken.INTERROGACION,"?")); avanzar(); break;

            // Dos puntos (privado: / publico: / ternario :)
            case ':': tokens.add(crearToken(TipoToken.DOS_PUNTOS,   ":")); avanzar(); break;

            // Punto y coma (para bucle estilo C: para (init; cond; paso))
            case ';': tokens.add(crearToken(TipoToken.PUNTO_COMA,   ";")); avanzar(); break;

            // + o ++ o +=
            case '+':
                avanzar();
                if (!fin() && actual() == '+') {
                    tokens.add(new Token(TipoToken.OP_INCREMENTO, "++", linea, col)); avanzar();
                } else if (!fin() && actual() == '=') {
                    tokens.add(new Token(TipoToken.OP_SUMA_ASIG,  "+=", linea, col)); avanzar();
                } else {
                    tokens.add(new Token(TipoToken.OP_SUMA,       "+",  linea, col));
                }
                break;

            // - o -- o -=
            case '-':
                avanzar();
                if (!fin() && actual() == '-') {
                    tokens.add(new Token(TipoToken.OP_DECREMENTO, "--", linea, col)); avanzar();
                } else if (!fin() && actual() == '=') {
                    tokens.add(new Token(TipoToken.OP_RESTA_ASIG, "-=", linea, col)); avanzar();
                } else {
                    tokens.add(new Token(TipoToken.OP_RESTA,      "-",  linea, col));
                }
                break;

            // * o *=
            case '*':
                avanzar();
                if (!fin() && actual() == '=') {
                    tokens.add(new Token(TipoToken.OP_MULT_ASIG, "*=", linea, col)); avanzar();
                } else {
                    tokens.add(new Token(TipoToken.OP_MULT,      "*",  linea, col));
                }
                break;

            // / o /=   (// y /* ya fueron manejados antes de llamar leerSimbolo)
            case '/':
                avanzar();
                if (!fin() && actual() == '=') {
                    tokens.add(new Token(TipoToken.OP_DIV_ASIG, "/=", linea, col)); avanzar();
                } else {
                    tokens.add(new Token(TipoToken.OP_DIV,      "/",  linea, col));
                }
                break;

            // % o %=
            case '%':
                avanzar();
                if (!fin() && actual() == '=') {
                    tokens.add(new Token(TipoToken.OP_MOD_ASIG, "%=", linea, col)); avanzar();
                } else {
                    tokens.add(new Token(TipoToken.OP_MOD,      "%",  linea, col));
                }
                break;

            // = o ==
            case '=':
                avanzar();
                if (!fin() && actual() == '=') {
                    tokens.add(new Token(TipoToken.OP_IGUAL,      "==", linea, col)); avanzar();
                } else {
                    tokens.add(new Token(TipoToken.OP_ASIGNACION, "=",  linea, col));
                }
                break;

            // ! o !=
            case '!':
                avanzar();
                if (!fin() && actual() == '=') {
                    tokens.add(new Token(TipoToken.OP_DIFERENTE, "!=", linea, col)); avanzar();
                } else {
                    tokens.add(new Token(TipoToken.OP_NOT,       "!",  linea, col));
                }
                break;

            // < o <=
            case '<':
                avanzar();
                if (!fin() && actual() == '=') {
                    tokens.add(new Token(TipoToken.OP_MENOR_IGUAL, "<=", linea, col)); avanzar();
                } else {
                    tokens.add(new Token(TipoToken.OP_MENOR,       "<",  linea, col));
                }
                break;

            // > o >=
            case '>':
                avanzar();
                if (!fin() && actual() == '=') {
                    tokens.add(new Token(TipoToken.OP_MAYOR_IGUAL, ">=", linea, col)); avanzar();
                } else {
                    tokens.add(new Token(TipoToken.OP_MAYOR,       ">",  linea, col));
                }
                break;

            // && (AND simbólico)
            case '&':
                avanzar();
                if (!fin() && actual() == '&') {
                    tokens.add(new Token(TipoToken.OP_AND, "&&", linea, col)); avanzar();
                } else {
                    errores.add("Error léxico en línea " + linea + ", col " + col
                            + ": '&' solitario no es válido. ¿Quisiste escribir '&&'?");
                    tokens.add(new Token(TipoToken.DESCONOCIDO, "&", linea, col));
                }
                break;

            // || (OR simbólico)
            case '|':
                avanzar();
                if (!fin() && actual() == '|') {
                    tokens.add(new Token(TipoToken.OP_OR, "||", linea, col)); avanzar();
                } else {
                    errores.add("Error léxico en línea " + linea + ", col " + col
                            + ": '|' solitario no es válido. ¿Quisiste escribir '||'?");
                    tokens.add(new Token(TipoToken.DESCONOCIDO, "|", linea, col));
                }
                break;

            default:
                errores.add("Error léxico en línea " + linea + ", col " + col
                        + ": carácter desconocido '" + c + "'");
                tokens.add(new Token(TipoToken.DESCONOCIDO, String.valueOf(c), linea, col));
                avanzar();
        }
    }

    // ---Utilidades de navegación

    /** Salta espacios, tabulaciones y retornos de carro */
    private void saltarEspacios() {
        while (!fin() && (actual() == ' ' || actual() == '\t' || actual() == '\r')) {
            avanzar();
        }
    }

    private char actual() {
        return fuente.charAt(pos);
    }

    private char siguiente() {
        return (pos + 1 < fuente.length()) ? fuente.charAt(pos + 1) : '\0';
    }

    private void avanzar() {
        if (!fin()) {
            if (fuente.charAt(pos) == '\n') {
                linea++;
                columna = 1;
            } else {
                columna++;
            }
            pos++;
        }
    }

    private boolean fin() {
        return pos >= fuente.length();
    }

    /**
     * Un identificador puede iniciar con: letra, guion bajo, o letra acentuada (á é í ó ú ü ñ)
     * Quetzal soporta unicode para identificadores porque las palabras reservadas usan español.
     */
    private boolean esInicioIdentificador(char c) {
        return Character.isLetter(c) || c == '_';
    }

    /**
     * Cuerpo del identificador: letras, dígitos, guión bajo.
     * Incluye letras con tilde porque sino_si tiene guión bajo y "número" (con tilde) es válido.
     */
    private boolean esCuerpoIdentificador(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }

    private Token crearToken(TipoToken tipo, String valor) {
        return new Token(tipo, valor, linea, columna);
    }
}
