package servidor;
import lexico.*;
import sintactico.*;
import sintactico.nodos.*;
import java.util.*;

public class TestParserSimple {
    public static void main(String[] args) {
        String codigo = """
                 a = ±√b²−2a / 2c
            """;

        System.out.println("════════════════════════════════════════");
        System.out.println("  COMPILADOR QUETZAL  FASES 1 Y 2");
        System.out.println("════════════════════════════════════════\n");

        System.out.println("CÓDIGO:");
        System.out.println(codigo);
        System.out.println("\n────────────────────────────────────────");

        // FASE 1
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

        // FASE 2
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

        System.out.println("COMPILACIÓN EXITOSA");
    }
}