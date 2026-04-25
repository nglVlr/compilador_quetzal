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
                x = ( ( 5 * 6 * 3 ) / ( 10 * 2 ) * 3 )
                """;

        System.out.println("════════════════════════════════════════");
        System.out.println("  COMPILADOR QUETZAL  FASES 1, 2, 3, 4 Y 5");
        System.out.println("════════════════════════════════════════\n");

        System.out.println("CÓDIGO:");
        System.out.println(codigo);
        System.out.println("\n────────────────────────────────────────");

        // FASE 1 - LÉXICO
        AnalizadorLexico lexer = new AnalizadorLexico(codigo);
        List<Token> tokens = lexer.analizar();

        System.out.println("\nFASE 1 - LÉXICO:");
        System.out.println("Tokens: " + (tokens.size() - 1));

        if (lexer.hayErrores()) {
            System.out.println("ERRORES:");
            lexer.getErrores().forEach(System.out::println);
            return;
        }
        System.out.println("Sin errores");

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

//        // FASE 3 - SEMÁNTICO
//        AnalizadorSemantico semantico = new AnalizadorSemantico();
//        semantico.analizar(ast);
//
//        System.out.println("\nFASE 3 - SEMÁNTICO:");
//        if (semantico.hayErrores()) {
//            System.out.println("ERRORES:");
//            semantico.getErrores().forEach(e -> System.out.println(e.toString()));
//            return;
//        }
//        System.out.println("Sin errores");
//
//        // Salida 1: Lista de errores semánticos → ya impresa arriba (ninguno)
//
//        // Salida 2: AST anotado con tipos
//        System.out.println("\nTIPOS ANOTADOS (" + semantico.getTiposAnotados().size() + " nodos):");
//        semantico.getTiposAnotados().forEach((nodo, tipo) ->
//                System.out.println("  " + nodo.getClass().getSimpleName()
//                        + " → " + tipo)
//        );
//
//        // FASE 4 - GENERADOR DE CÓDIGO
//        GeneradorCodigo generador = new GeneradorCodigo();
//        String codigoJava = ast.aceptar(generador);
//
//        System.out.println("\nFASE 4 - CÓDIGO JAVA GENERADO:");
//        System.out.println("---------------------------------------------");
//        System.out.println(codigoJava);
//        System.out.println("---------------------------------------------");
//
//        // FASE 5 - COMPILACIÓN A BYTECODE (.class)
//        System.out.println("\nFASE 5 - COMPILACIÓN A BYTECODE:");
//        System.out.println("---------------------------------------------");
//
//        // Carpeta de salida: out/generado/ dentro del directorio de trabajo
//        String carpetaSalida = System.getProperty("user.dir")
//                + java.io.File.separator + "out"
//                + java.io.File.separator + "generado";
//
//        boolean exitoso = CompiladorJava.compilar(codigoJava, carpetaSalida);
//
//        System.out.println("---------------------------------------------");
//        if (exitoso) {
//            System.out.println("COMPILACIÓN EXITOSA - .class listo para JVM");
//        } else {
//            System.out.println("COMPILACIÓN FALLIDA - revisa los errores anteriores");
//        }
//        System.out.println("---------------------------------------");
   }
}