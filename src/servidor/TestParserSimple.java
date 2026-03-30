package servidor;
import lexico.*;
import semantico.AnalizadorSemantico;
import sintactico.*;
import sintactico.nodos.*;
import java.util.*;

public class TestParserSimple {
    public static void main(String[] args) {
        String codigo = """
                consola.mostrar("CALCULADORA QUETZAL")
                //c = 1 + a
                // Mostrar menú
                consola.mostrar_informacion("Selecciona una operación:")
                consola.mostrar("1. Suma")
                consola.mostrar("2. Resta")
                consola.mostrar("3. Multiplicación")
                consola.mostrar("4. División")
                consola.mostrar("0. Salir")
                
                // Pedir opción
                texto entrada_opcion = consola.pedir("Ingresa el número de operación: ")
                
                // Convertir opción
                entero var mi_opcion = 0
                
                si (entrada_opcion == "0") {
                    mi_opcion = 0
                } sino si (entrada_opcion == "1") {
                    mi_opcion = 1
                } sino si (entrada_opcion == "2") {
                    mi_opcion = 2
                } sino si (entrada_opcion == "3") {
                    mi_opcion = 3
                } sino si (entrada_opcion == "4") {
                    mi_opcion = 4
                }
                // Validar
                si (mi_opcion < 0 o mi_opcion > 4) {
                    consola.mostrar_error("Opción inválida")
                }
                
                // Salir
                si (mi_opcion == 0) {
                    consola.mostrar_advertencia("Hasta luego")
                }
                
                // Pedir números
                texto entrada_num1 = consola.pedir("Primer número: ")
                texto entrada_num2 = consola.pedir("Segundo número: ")
                
                // Convertir texto a número
                numero primer_numero = entrada_num1.numero()
                numero segundo_numero = entrada_num2.numero()
                
                // Variables resultado
                numero var mi_resultado = 0
                texto var nombre_op = ""
                log var valida = verdadero
                
                // Calcular
                si (mi_opcion == 1) {
                    mi_resultado = primer_numero + segundo_numero
                    nombre_op = "Suma"
                } sino si (mi_opcion == 2) {
                    mi_resultado = primer_numero - segundo_numero
                    nombre_op = "Resta"
                } sino si (mi_opcion == 3) {
                    mi_resultado = primer_numero * segundo_numero
                    nombre_op = "Multiplicación"
                } sino si (mi_opcion == 4) {
                    si (segundo_numero == 0) {
                        consola.mostrar_error("No se puede dividir por cero")
                        valida = falso
                    } sino {
                        mi_resultado = primer_numero / segundo_numero
                        nombre_op = "División"
                    }
                }
                
                // Mostrar resultado
                si (valida) {
                    consola.mostrar_exito("RESULTADO:")
                   \s
                    texto mensaje = t"{nombre_op} de {primer_numero} y {segundo_numero} = {mi_resultado}"
                    consola.mostrar(mensaje)
                   \s
                }
                
            """;

        System.out.println("════════════════════════════════════════");
        System.out.println("  COMPILADOR QUETZAL  FASES 1, 2 Y 3");
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

        // Salida 1: Lista de errores semánticos → ya impresa arriba (ninguno)

        // Salida 2: AST anotado con tipos
        System.out.println("\nTIPOS ANOTADOS (" + semantico.getTiposAnotados().size() + " nodos):");
        semantico.getTiposAnotados().forEach((nodo, tipo) ->
                System.out.println("  " + nodo.getClass().getSimpleName()
                        + " → " + tipo)
        );

        System.out.println("\n════════════════════════════════════════");
        System.out.println("  COMPILACIÓN EXITOSA - LISTO PARA CÓDIGO INTERMEDIO");
        System.out.println("════════════════════════════════════════");
    }
}