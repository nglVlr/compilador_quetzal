package servidor;


import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import lexico.AnalizadorLexico;
import lexico.Token;
import sintactico.ASTaJSON;
import sintactico.AnalizadorSintactico;
import sintactico.ImpressorAST;
import sintactico.nodos.NodoPrograma;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ServidorSintactico {

    private static final int PUERTO = 8081;

    public static void main(String[] args) throws IOException {

        HttpServer servidor = HttpServer.create(new InetSocketAddress(PUERTO), 0);

        // Endpoint de salud
        servidor.createContext("/salud", exchange -> {
            if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                responder(exchange, 405, "{\"error\":\"Metodo no permitido\"}");
                return;
            }
            responder(exchange, 200,
                    "{\"estado\":\"OK\",\"mensaje\":\"Analizador Sintactico activo\",\"version\":\"1.0\"}");
        });

        // Endpoint principal: recibe JSON
        servidor.createContext("/sintactico", exchange -> {
            agregarCORS(exchange);
            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                responder(exchange, 204, "");
                return;
            }
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                responder(exchange, 405, "{\"error\":\"Solo POST\"}");
                return;
            }

            try {
                String cuerpo = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                String codigo = extraerCampoCodigo(cuerpo);

                if (codigo == null) {
                    responder(exchange, 400, "{\"error\":\"El cuerpo debe ser JSON con campo 'codigo'\"}");
                    return;
                }

                String resultado = analizarCodigo(codigo);
                responder(exchange, 200, resultado);

            } catch (Exception e) {
                responder(exchange, 500, "{\"error\":\"" + escaparJson(e.getMessage()) + "\"}");
            }
        });

        // Endpoint alternativo: recibe texto plano
        servidor.createContext("/sintactico/texto", exchange -> {
            agregarCORS(exchange);
            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                responder(exchange, 204, "");
                return;
            }
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                responder(exchange, 405, "{\"error\":\"Solo POST\"}");
                return;
            }

            try {
                String codigo = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

                if (codigo.isBlank()) {
                    responder(exchange, 400, "{\"error\":\"Cuerpo vacio\"}");
                    return;
                }

                String resultado = analizarCodigo(codigo);
                responder(exchange, 200, resultado);

            } catch (Exception e) {
                responder(exchange, 500, "{\"error\":\"" + escaparJson(e.getMessage()) + "\"}");
            }
        });

        servidor.setExecutor(null);
        servidor.start();

        System.out.println("╔════════════════════════════════════════════════════╗");
        System.out.println("║   SERVIDOR ANALIZADOR SINTÁCTICO - QUETZAL        ║");
        System.out.println("╚════════════════════════════════════════════════════╝");
        System.out.println("\n Servidor corriendo en puerto " + PUERTO);
        System.out.println("\n Endpoints disponibles:");
        System.out.println("   POST http://localhost:" + PUERTO + "/sintactico        (body: JSON)");
        System.out.println("   POST http://localhost:" + PUERTO + "/sintactico/texto  (body: Text)");
    }

    /**
     * Ejecuta el análisis léxico y sintáctico del código.
     * Retorna JSON con el resultado.
     */
    private static String analizarCodigo(String codigo) {
        StringBuilder resultado = new StringBuilder();
        resultado.append("{\n");

        // FASE 1: Análisis Léxico
        AnalizadorLexico lexer = new AnalizadorLexico(codigo);
        List<Token> tokens = lexer.analizar();

        resultado.append("  \"lexico\": {\n");
        resultado.append("    \"exitoso\": ").append(!lexer.hayErrores()).append(",\n");
        resultado.append("    \"totalTokens\": ").append(tokens.size() - 1).append(",\n");
        resultado.append("    \"errores\": ").append(construirArrayErrores(lexer.getErrores())).append("\n");
        resultado.append("  },\n");

        // Si hay errores léxicos, no continuar
        if (lexer.hayErrores()) {
            resultado.append("  \"sintactico\": null\n");
            resultado.append("}");
            return resultado.toString();
        }

        // FASE 2: Análisis Sintáctico
        AnalizadorSintactico parser = new AnalizadorSintactico(tokens);
        NodoPrograma ast = parser.analizar();

        resultado.append("  \"sintactico\": {\n");
        resultado.append("    \"exitoso\": ").append(!parser.hayErrores()).append(",\n");
        resultado.append("    \"errores\": ").append(construirArrayErrores(parser.getErrores())).append(",\n");

        // Imprimir el AST si no hay errores
        if (!parser.hayErrores()) {
           // ASTaJSON astJson = new ASTaJSON();
            //String astTexto = ast.aceptar(astJson);
            //resultado.append("    \"ast\": ").append(astTexto).append("\n");
           //con estas para el testParserSimple
            ImpressorAST impresor = new ImpressorAST();
            String astTexto = ast.aceptar(impresor);
            resultado.append("    \"ast\": \"").append(escaparJson(astTexto)).append("\"\n");
        } else {
            resultado.append("    \"ast\": null\n");
        }

        resultado.append("  }\n");
        resultado.append("}");

        return resultado.toString();
    }

    private static String construirArrayErrores(List<String> errores) {
        if (errores.isEmpty()) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder("[\n");
        for (int i = 0; i < errores.size(); i++) {
            sb.append("      \"").append(escaparJson(errores.get(i))).append("\"");
            if (i < errores.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("    ]");
        return sb.toString();
    }

    private static void agregarCORS(HttpExchange e) {
        e.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        e.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
        e.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
    }

    private static void responder(HttpExchange exchange, int codigo, String cuerpo) throws IOException {
        byte[] bytes = cuerpo.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(codigo, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static String extraerCampoCodigo(String json) {
        int idx = json.indexOf("\"codigo\"");
        if (idx == -1) return null;
        int dp = json.indexOf(':', idx);
        if (dp == -1) return null;
        int abre = json.indexOf('"', dp + 1);
        if (abre == -1) return null;

        StringBuilder sb = new StringBuilder();
        int i = abre + 1;
        while (i < json.length()) {
            char c = json.charAt(i);
            if (c == '\\' && i + 1 < json.length()) {
                char s = json.charAt(i + 1);
                switch (s) {
                    case 'n': sb.append('\n'); i += 2; continue;
                    case 't': sb.append('\t'); i += 2; continue;
                    case 'r': sb.append('\r'); i += 2; continue;
                    case '"': sb.append('"');  i += 2; continue;
                    case '\\': sb.append('\\'); i += 2; continue;
                    default: sb.append('\\'); i++; continue;
                }
            }
            if (c == '"') break;
            sb.append(c);
            i++;
        }
        return sb.toString();
    }

    private static String escaparJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}