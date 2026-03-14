import lexico.AnalizadorLexico;
import lexico.Token;
import sintactico.AnalizadorSintactico;
import sintactico.ImpressorAST;
import sintactico.nodos.NodoPrograma;

import java.util.List;

/**
 * Punto de entrada: prueba el Léxico + Sintáctico juntos.
 *
 * Compilar:
 *   javac -d out -encoding UTF-8 src/lexico/*.java src/sintactico/*.java src/sintactico/nodos/*.java Main.java
 *
 * Ejecutar:
 *   java -cp out Main
 */
public class Main {

    public static void main(String[] args) {

        titulo("COMPILADOR QUETZAL → JAVA  |  Fase 2: Analizador Sintáctico");

        // ── 1. Variables y tipos ─────────────────────────────────────────────
        probar("1. Variables y tipos primitivos",
                "entero edad = 27\n" +
                        "numero temperatura = 22.5\n" +
                        "texto mensaje = \"Hola Quetzal\"\n" +
                        "log activo = verdadero\n" +
                        "entero var contador = 0\n" +
                        "texto nombre = nulo"
        );

        // ── 2. Operadores aritméticos y comparación ──────────────────────────
        probar("2. Operadores aritméticos y comparación",
                "entero suma = a + b\n" +
                        "entero producto = a * b\n" +
                        "log mayor = a > b\n" +
                        "log igual = a == b\n" +
                        "log resultado = (a + b) >= (c - d)"
        );

        // ── 3. Operador ternario ─────────────────────────────────────────────
        probar("3. Operador ternario",
                "texto estado = edad >= 18 ? \"Mayor de edad\" : \"Menor de edad\""
        );

        // ── 4. Condicional si / sino_si / sino ───────────────────────────────
        probar("4. Condicional si / sino_si / sino",
                "si (edad >= 18) {\n" +
                        "    consola.mostrar(\"Mayor de edad\")\n" +
                        "} sino_si (edad >= 12) {\n" +
                        "    consola.mostrar(\"Adolescente\")\n" +
                        "} sino {\n" +
                        "    consola.mostrar(\"Menor de edad\")\n" +
                        "}"
        );

        // ── 5. Bucle mientras ────────────────────────────────────────────────
        probar("5. Bucle mientras",
                "entero var i = 0\n" +
                        "mientras (i < 10) {\n" +
                        "    consola.mostrar(i)\n" +
                        "    i++\n" +
                        "}"
        );

        // ── 6. Bucle hacer mientras (do-while) ───────────────────────────────
        probar("6. Bucle hacer...mientras",
                "hacer {\n" +
                        "    consola.mostrar(valor)\n" +
                        "    valor--\n" +
                        "} mientras (valor > 0)"
        );

        // ── 7. Para foreach ──────────────────────────────────────────────────
        probar("7. Para elemento en lista (foreach)",
                "para elemento en nombres {\n" +
                        "    consola.mostrar(elemento)\n" +
                        "}"
        );

        // ── 8. Declaración y llamada de funciones ────────────────────────────
        probar("8. Funciones",
                "entero funcion sumar(entero a, entero b) {\n" +
                        "    retornar a + b\n" +
                        "}\n" +
                        "vacio funcion saludar(texto nombre) {\n" +
                        "    consola.mostrar(t\"Hola {nombre}\")\n" +
                        "}\n" +
                        "entero resultado = sumar(10, 5)"
        );

        // ── 9. Función async con esperar ─────────────────────────────────────
        probar("9. Función asíncrona",
                "async funcion obtenerDatos() {\n" +
                        "    jsn datos = esperar peticion(\"url\")\n" +
                        "    retornar datos\n" +
                        "}"
        );

        // ── 10. Listas ───────────────────────────────────────────────────────
        probar("10. Listas tipadas y no tipadas",
                "lista<entero> numeros = [1, 2, 3]\n" +
                        "lista<texto> var nombres = [\"Ana\", \"Luis\"]\n" +
                        "lista valores = [1, \"dos\", verdadero]\n" +
                        "nombres.agregar(\"Pedro\")\n" +
                        "consola.mostrar(nombres)"
        );

        // ── 11. JSN ──────────────────────────────────────────────────────────
        probar("11. Objeto JSN",
                "jsn var persona = {\n" +
                        "    nombre: \"Ana\",\n" +
                        "    edad: 30,\n" +
                        "    activo: verdadero\n" +
                        "}\n" +
                        "texto nom = persona.nombre\n" +
                        "persona.establecer(\"activo\", falso)"
        );

        // ── 12. Texto interpolado ────────────────────────────────────────────
        probar("12. Texto interpolado",
                "texto saludo = t\"Hola, {nombre}!\"\n" +
                        "consola.mostrar(t\"Suma: {a + b}\")"
        );

        // ── 13. Clases / Objetos (OOP) ───────────────────────────────────────
        probar("13. Declaración de objeto (OOP)",
                "objeto Usuario {\n" +
                        "    privado:\n" +
                        "        texto nombre\n" +
                        "        entero edad\n" +
                        "    publico:\n" +
                        "        Usuario(texto nombre, entero edad) {\n" +
                        "            ambiente.nombre = nombre\n" +
                        "            ambiente.edad = edad\n" +
                        "        }\n" +
                        "        texto saludo() {\n" +
                        "            retornar t\"Hola, {ambiente.nombre}\"\n" +
                        "        }\n" +
                        "}\n" +
                        "Usuario persona = nuevo Usuario(\"Maria\", 29)\n" +
                        "consola.mostrar(persona.saludo())"
        );

        // ── 14. Manejo de errores ────────────────────────────────────────────
        probar("14. Intentar / capturar / finalmente",
                "intentar {\n" +
                        "    entero resultado = dividir(10, 0)\n" +
                        "} capturar (error) {\n" +
                        "    consola.mostrar_error(error)\n" +
                        "    lanzar error\n" +
                        "} finalmente {\n" +
                        "    consola.mostrar(\"Finalizado\")\n" +
                        "}"
        );

        // ── 15. Módulos ──────────────────────────────────────────────────────
        probar("15. Importar y exportar módulos",
                "importar { Matematica } desde \"quetzal/matematica\"\n" +
                        "exportar { sumar }"
        );

        // ── 16. Operadores lógicos y asignación compuesta ────────────────────
        probar("16. Lógicos y asignación compuesta",
                "log resultado = condA y condB\n" +
                        "log otra = condA o condB\n" +
                        "log neg = no condA\n" +
                        "contador += 5\n" +
                        "total *= 2"
        );

        // ── 17. romper y continuar ───────────────────────────────────────────
        probar("17. Romper y continuar",
                "mientras (verdadero) {\n" +
                        "    si (x > 10) {\n" +
                        "        romper\n" +
                        "    }\n" +
                        "    si (x == 5) {\n" +
                        "        continuar\n" +
                        "    }\n" +
                        "    x++\n" +
                        "}"
        );

        // ── 18. Programa completo integrado ──────────────────────────────────
        probar("18. Programa completo (calculadora)",
                "entero funcion calcular(texto op, entero a, entero b) {\n" +
                        "    si (op == \"suma\") {\n" +
                        "        retornar a + b\n" +
                        "    } sino_si (op == \"resta\") {\n" +
                        "        retornar a - b\n" +
                        "    } sino {\n" +
                        "        retornar 0\n" +
                        "    }\n" +
                        "}\n" +
                        "entero resultado = calcular(\"suma\", 10, 5)\n" +
                        "consola.mostrar_exito(t\"Resultado: {resultado}\")"
        );
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    private static void titulo(String texto) {
        String linea = "═".repeat(68);
        System.out.println("\n" + linea);
        System.out.println("  " + texto);
        System.out.println(linea + "\n");
    }

    private static void probar(String titulo, String codigoQuetzal) {
        System.out.println("┌─ " + titulo + " " + "─".repeat(Math.max(0, 62 - titulo.length())));

        // Fase 1: Léxico
        AnalizadorLexico lexico = new AnalizadorLexico(codigoQuetzal);
        List<Token> tokens = lexico.analizar();

        if (lexico.hayErrores()) {
            System.out.println("│ ✗ Errores léxicos:");
            lexico.getErrores().forEach(e -> System.out.println("│   " + e));
            System.out.println("└" + "─".repeat(68) + "\n");
            return;
        }

        // Fase 2: Sintáctico
        AnalizadorSintactico parser = new AnalizadorSintactico(tokens);
        NodoPrograma ast = parser.analizar();

        if (parser.hayErrores()) {
            System.out.println("│ ✗ Errores sintácticos:");
            parser.getErrores().forEach(e -> System.out.println("│   " + e));
        } else {
            System.out.println("│ ✓ AST generado correctamente:");
            ImpressorAST imp = new ImpressorAST();
            String arbol = ast.aceptar(imp);
            for (String linea : arbol.split("\n")) {
                System.out.println("│   " + linea);
            }
        }

        System.out.println("└" + "─".repeat(68) + "\n");
    }
}
