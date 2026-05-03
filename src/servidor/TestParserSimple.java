package servidor;
import generador.CompiladorJava;
import generador.GeneradorCodigo;
import lexico.*;
import semantico.AnalizadorSemantico;
import sintactico.*;
import sintactico.nodos.*;
import java.util.*;


public class TestParserSimple {
    public static void main(String[] args) {
        String codigo = """
                entero suma(entero a, entero b){
                  retornar a + b
                }
                
                entero var r
                
                r = suma(2, 3)
                consola.mostrar(r)
                """;

        System.out.println("════════════════════════════════════════");
        System.out.println("  COMPILADOR QUETZAL  FASES 1 Y 2");
        System.out.println("════════════════════════════════════════\n");

        System.out.println("CÓDIGO:");
        System.out.println(codigo);
        System.out.println("\n────────────────────────────────────────");

        // FASE 1 - LÉXICO
        AnalizadorLexico lexer = new AnalizadorLexico(codigo);
        List<Token> tokens = lexer.analizar();


        int total = 0;
        for (Token t : tokens) {
            if (t.getTipo() != TipoToken.EOF && t.getTipo() != TipoToken.NUEVA_LINEA) {
                total++;
            }
        }

        System.out.println("\nFASE 1 - LÉXICO:");
        System.out.println("Total Tokens: " + total);

        if (lexer.hayErrores()) {
            System.out.println("ERRORES:");
            lexer.getErrores().forEach(System.out::println);
            return;
        }
        System.out.println("Sin errores\n");

        // Imprimir tabla de tokens
        System.out.println("────────────────────────────────────────────────────────────────────────");
        System.out.printf("%-5s %-30s %-30s %-8s %-8s%n",
                "Núm", "Tipo", "Valor", "Línea", "Columna");
        System.out.println("────────────────────────────────────────────────────────────────────────");

        int num = 1;
        for (Token t : tokens) {
            if (t.getTipo() == TipoToken.EOF || t.getTipo() == TipoToken.NUEVA_LINEA) continue;
            System.out.printf("%-5d %-30s %-30s %-8d %-8d%n",
                    num++,
                    clasificar(t.getTipo()),
                    t.getValor(),
                    t.getLinea(),
                    t.getColumna());
        }
        System.out.println("────────────────────────────────────────────────────────────────────────");

        // FASE 2 - SINTÁCTICO
        AnalizadorSintactico parser = new AnalizadorSintactico(tokens);
        NodoPrograma ast = parser.analizar();

        System.out.println("\nFASE 2 - SINTÁCTICO:");
        if (parser.hayErrores()) {
            System.out.println("ERRORES:");
            parser.getErrores().forEach(System.out::println);
            return;
        }
        System.out.println("Sin errores");

        System.out.println("\nAST:");
        ImpressorAST imp = new ImpressorAST();
        System.out.println(ast.aceptar(imp));

        // FASE 3 - SEMÁNTICO
        AnalizadorSemantico semantico = new AnalizadorSemantico();
        semantico.analizar(ast);

        System.out.println("\nFASE 3 - SEMÁNTICO:");
        if (semantico.hayErrores()) {
            System.out.println("ERRORES:");
            semantico.getErrores().forEach(e -> System.out.println(e.toString()));
            return;
        }
        System.out.println("Sin errores");

        // Salida 1: Lista de errores semánticos - ya impresa arriba (ninguno)

        // Salida 2: AST anotado con tipos
        System.out.println("\nTIPOS ANOTADOS (" + semantico.getTiposAnotados().size() + " nodos):");
        semantico.getTiposAnotados().forEach((nodo, tipo) ->
                System.out.println("  " + nodo.getClass().getSimpleName()
                        + " + " + tipo)
        );

        // FASE 4 - GENERADOR DE CÓDIGO
        GeneradorCodigo generador = new GeneradorCodigo();
        String codigoJava = ast.aceptar(generador);

        System.out.println("\nFASE 4 - CÓDIGO JAVA GENERADO:");
        System.out.println("---------------------------------------------");
        System.out.println(codigoJava);
        System.out.println("---------------------------------------------");

        // FASE 5 - COMPILACIÓN A BYTECODE (.class)
        System.out.println("\nFASE 5 - COMPILACIÓN A BYTECODE:");
        System.out.println("---------------------------------------------");

        // Carpeta de salida: out/generado/ dentro del directorio de trabajo
        String carpetaSalida = System.getProperty("user.dir")
                + java.io.File.separator + "out"
                + java.io.File.separator + "generado";

        boolean exitoso = CompiladorJava.compilar(codigoJava, carpetaSalida);

        System.out.println("---------------------------------------------");
        if (exitoso) {
            System.out.println("COMPILACIÓN EXITOSA - .class listo para JVM");
        } else {
            System.out.println("COMPILACIÓN FALLIDA - revisa los errores anteriores");
        }
        System.out.println("---------------------------------------");

    }

    private static String clasificar(TipoToken tipo) {
        switch (tipo) {
            case ENTERO: case NUMERO: case TEXTO: case LOG: case VACIO:
            case LISTA: case JSN: case VAR:
            case SI: case SINO: case SINO_SI: case MIENTRAS: case PARA:
            case EN: case HACER: case RETORNAR: case ROMPER: case CONTINUAR:
            case FUNCION: case ASYNC: case ESPERAR:
            case OBJETO: case NUEVO: case AMBIENTE: case PRIVADO: case PUBLICO: case ESTATICO:
            case IMPORTAR: case EXPORTAR: case DESDE:
            case INTENTAR: case CAPTURAR: case FINALMENTE: case LANZAR:
            case VERDADERO: case FALSO: case NULO:
            case Y: case O: case NO:
                return "Palabra Reservada";
            case IDENTIFICADOR:
                return "Identificador";
            case LITERAL_ENTERO:
                return "Literal Entero";
            case LITERAL_REAL:
                return "Literal Flotante";
            case LITERAL_TEXTO:
                return "Literal String";
            case LITERAL_TEXTO_INTERP:
                return "Literal String Interpolado";
            case OP_SUMA: case OP_RESTA: case OP_MULT: case OP_DIV: case OP_MOD:
            case OP_INCREMENTO: case OP_DECREMENTO:
                return "Operador Aritmetico";
            case OP_IGUAL: case OP_DIFERENTE:
            case OP_MENOR: case OP_MAYOR: case OP_MENOR_IGUAL: case OP_MAYOR_IGUAL:
                return "Operador Relacional";
            case OP_AND: case OP_OR: case OP_NOT:
                return "Operador Logico";
            case OP_ASIGNACION:
            case OP_SUMA_ASIG: case OP_RESTA_ASIG:
            case OP_MULT_ASIG: case OP_DIV_ASIG: case OP_MOD_ASIG:
                return "Operador Asignacion";
            case INTERROGACION:
                return "Operador Ternario";
            case PAREN_IZQ: case PAREN_DER:
            case LLAVE_IZQ: case LLAVE_DER:
            case CORCHETE_IZQ: case CORCHETE_DER:
            case PUNTO: case COMA: case DOS_PUNTOS: case PUNTO_COMA:
                return "Delimitador";
            default:
                return "Desconocido";
        }
    }
}