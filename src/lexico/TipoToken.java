package lexico;

/**
 * Tipos de tokens del Lenguaje Quetzal.
 * Basado EXACTAMENTE en la documentación oficial:
 * https://lenguaje-quetzal.antaresgt.com
 *
 * Reglas aplicadas:
 *  - Solo se incluyen palabras tal como aparecen en la doc (sin variantes con tilde).
 *  - La doc dice: "numero y número son equivalentes" → ambas se mapean a NUMERO.
 *  - No se inventan tokens que no existan en la gramática.
 */
public enum TipoToken {

    // ─── TIPOS DE DATOS PRIMITIVOS ───────────────────────────────────────────
    // Fuente: tabla de tipos en /fundamentos/sintaxis-basica/
    ENTERO,         // entero   → números enteros con signo
    NUMERO,         // numero   → punto flotante (también acepta "número" con tilde)
    TEXTO,          // texto    → cadenas Unicode
    LOG,            // log      → booleano (verdadero / falso)
    VACIO,          // vacio    → ausencia de valor (solo para funciones)

    // ─── ESTRUCTURAS DE DATOS ────────────────────────────────────────────────
    // Fuente: /fundamentos/sintaxis-basica/ sección Listas y JSN
    LISTA,          // lista                  → lista tipada o no tipada
    JSN,            // jsn                    → objeto JSON nativo

    // ─── MUTABILIDAD ─────────────────────────────────────────────────────────
    // Fuente: "Para permitir cambios se utiliza la palabra reservada var tras el tipo"
    VAR,            // var      → hace que una variable sea mutable

    // ─── VALORES LITERALES BOOLEANOS Y NULO ──────────────────────────────────
    VERDADERO,      // verdadero
    FALSO,          // falso
    NULO,           // nulo

    // ─── ESTRUCTURAS DE CONTROL ──────────────────────────────────────────────
    // Fuente: /control/condicionales/ y /control/bucles/
    SI,             // si
    SINO,           // sino
    SINO_SI,        // sino_si
    MIENTRAS,       // mientras
    PARA,           // para
    EN,             // en       → usado en: para x en lista
    HACER,          // hacer    → do-while: hacer { } mientras (cond)

    // ─── CONTROL DE FLUJO ────────────────────────────────────────────────────
    // Fuente: /control/flujo/
    RETORNAR,       // retornar
    ROMPER,         // romper
    CONTINUAR,      // continuar

    // ─── FUNCIONES ───────────────────────────────────────────────────────────
    // Fuente: /funciones/definicion/ y /funciones/asincronas/
    FUNCION,        // funcion
    ASYNC,          // async    → función asíncrona
    ESPERAR,        // esperar  → equivalente a await

    // ─── PROGRAMACIÓN ORIENTADA A OBJETOS ────────────────────────────────────
    // Fuente: /oop/clases-objetos/ y /oop/constructores/
    OBJETO,         // objeto   → declaración de clase/objeto
    NUEVO,          // nuevo    → instanciar: nuevo NombreObjeto(...)
    AMBIENTE,       // ambiente → referencia al objeto actual (como "this")
    PRIVADO,        // privado  → bloque de miembros privados
    PUBLICO,        // publico  → bloque de miembros públicos
    ESTATICO,       // estatico → miembro estático

    // ─── MÓDULOS ─────────────────────────────────────────────────────────────
    // Fuente: /modulos/importar-exportar/
    IMPORTAR,       // importar
    EXPORTAR,       // exportar
    DESDE,          // desde    → importar { X } desde "ruta"

    // ─── MANEJO DE ERRORES ───────────────────────────────────────────────────
    // Fuente: /errores/try-catch/
    INTENTAR,       // intentar
    CAPTURAR,       // capturar
    FINALMENTE,     // finalmente
    LANZAR,         // lanzar

    // ─── OPERADORES LÓGICOS EN ESPAÑOL ───────────────────────────────────────
    // Fuente: sección "Logicos" en /fundamentos/sintaxis-basica/
    Y,              // y   → AND en español
    O,              // o   → OR en español
    NO,             // no  → NOT en español

    // ─── LITERALES CON VALOR ─────────────────────────────────────────────────
    LITERAL_ENTERO,       // 42, -5, 100
    LITERAL_REAL,         // 3.14, 9.99
    LITERAL_TEXTO,        // "hola mundo"
    LITERAL_TEXTO_INTERP, // t"Hola, {nombre}!"   (prefijo t)
    IDENTIFICADOR,        // nombreVariable, miFuncion, NombreObjeto

    // ─── OPERADORES ARITMÉTICOS ──────────────────────────────────────────────
    // Fuente: sección "Aritmeticos"
    OP_SUMA,        // +
    OP_RESTA,       // -
    OP_MULT,        // *
    OP_DIV,         // /
    OP_MOD,         // %

    // ─── OPERADORES DE INCREMENTO / DECREMENTO ───────────────────────────────
    // Fuente: sección "Incremento y decremento"
    OP_INCREMENTO,  // ++
    OP_DECREMENTO,  // --

    // ─── OPERADORES DE COMPARACIÓN ───────────────────────────────────────────
    // Fuente: sección "Comparacion"
    OP_IGUAL,       // ==
    OP_DIFERENTE,   // !=
    OP_MENOR,       // <
    OP_MAYOR,       // >
    OP_MENOR_IGUAL, // <=
    OP_MAYOR_IGUAL, // >=

    // ─── OPERADORES DE ASIGNACIÓN ────────────────────────────────────────────
    // Fuente: sección "Asignacion compuesta"
    OP_ASIGNACION,  // =
    OP_SUMA_ASIG,   // +=
    OP_RESTA_ASIG,  // -=
    OP_MULT_ASIG,   // *=
    OP_DIV_ASIG,    // /=
    OP_MOD_ASIG,    // %=

    // ─── OPERADORES LÓGICOS SIMBÓLICOS ───────────────────────────────────────
    // Fuente: "Operadores simbolicos (equivalentes)" en la doc
    OP_AND,         // &&
    OP_OR,          // ||
    OP_NOT,         // !

    // ─── OPERADOR TERNARIO ───────────────────────────────────────────────────
    // Fuente: sección "Operador ternario" → edad >= 18 ? "Mayor" : "Menor"
    INTERROGACION,  // ?
    DOS_PUNTOS,     // :   (también usado en bloques privado: / publico:)

    // ─── DELIMITADORES ───────────────────────────────────────────────────────
    PAREN_IZQ,      // (
    PAREN_DER,      // )
    LLAVE_IZQ,      // {
    LLAVE_DER,      // }
    CORCHETE_IZQ,   // [
    CORCHETE_DER,   // ]

    // ─── PUNTUACIÓN ──────────────────────────────────────────────────────────
    PUNTO,          // .   (acceso a miembro: persona.nombre, consola.mostrar)
    COMA,           // ,
    MENOR_TIPO,     // <   cuando se usa en lista<entero> (lo resuelve el parser)
    MAYOR_TIPO,     // >   cuando se usa en lista<entero>

    // ─── COMENTARIOS ─────────────────────────────────────────────────────────
    // Fuente: sección "Comentarios" → // y /* */
    COMENTARIO_LINEA,   // // texto
    COMENTARIO_BLOQUE,  // /* texto */

    // ─── ESPECIALES ──────────────────────────────────────────────────────────
    NUEVA_LINEA,    // \n  (Quetzal no usa ; → la nueva línea delimita instrucciones)
    EOF,            // fin del archivo
    PUNTO_COMA, DESCONOCIDO     // carácter no reconocido → error léxico
}
