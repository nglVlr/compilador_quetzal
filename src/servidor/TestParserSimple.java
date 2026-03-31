package servidor;
import generador.GeneradorCodigo;
import lexico.*;
import semantico.AnalizadorSemantico;
import sintactico.*;
import sintactico.nodos.*;
import java.util.*;

public class TestParserSimple {
    public static void main(String[] args) {
        String codigo = """
                // Programa de gestión de notas con funciones
                
                    // Lista de notas
                    lista<numero> var notas = [85, 92, 78, 65, 90]
                
                    // Variables
                    numero var suma = 0
                    numero var mayor = 0
                    numero var menor = 100
                    entero var cantidad = 0
                    texto var resultado = ""
                
                    // Calcular suma y encontrar mayor y menor
                    entero var i = 0
                    mientras (i < 5) {
                        numero nota_actual = notas.obtener(i)
                        suma = suma + nota_actual
                        si (nota_actual > mayor) {
                            mayor = nota_actual
                        }
                        si (nota_actual < menor) {
                            menor = nota_actual
                        }
                        cantidad++
                        i++
                    }
                
                    // Calcular promedio
                    numero promedio = suma / cantidad
                
                    // Determinar resultado
                    si (promedio >= 60) {
                        resultado = "APROBADO"
                    } sino {
                        resultado = "REPROBADO"
                    }
                
                    // Mostrar reporte
                    consola.mostrar_informacion("══════════════════════════")
                    consola.mostrar_informacion("   REPORTE DE NOTAS")
                    consola.mostrar_informacion("══════════════════════════")
                    consola.mostrar(t"Total notas  : {cantidad}")
                    consola.mostrar(t"Nota mayor   : {mayor}")
                    consola.mostrar(t"Nota menor   : {menor}")
                    consola.mostrar(t"Suma total   : {suma}")
                    consola.mostrar(t"Promedio     : {promedio}")
                
                    si (resultado == "APROBADO") {
                        consola.mostrar_exito(t"Resultado: {resultado}")
                    } sino {
                        consola.mostrar_error(t"Resultado: {resultado}")
                    }
                
                    // Manejo de errores
                    intentar {
                        entero var divisor = 0
                        si (divisor == 0) {
                            lanzar "Division por cero no permitida"
                        }
                        numero division = suma / divisor
                    } capturar (error) {
                        consola.mostrar_error("Error capturado correctamente")
                    } finalmente {
                        consola.mostrar_informacion("Proceso finalizado")
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

        // FASE 4 - GENERADOR DE CÓDIGO
        GeneradorCodigo generador = new GeneradorCodigo();
        String codigoJava = ast.aceptar(generador);

        System.out.println("\nFASE 4 - CÓDIGO JAVA GENERADO:");
        System.out.println("---------------------------------------------");
        System.out.println(codigoJava);
        System.out.println("--------------------------------------");
        System.out.println("COMPILACIÓN EXITOSA");
        System.out.println("---------------------------------------");
    }
}