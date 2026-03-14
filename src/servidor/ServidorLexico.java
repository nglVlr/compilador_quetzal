package servidor;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import lexico.AnalizadorLexico;
import lexico.Token;
import lexico.TipoToken;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * API REST para probar el Analizador Léxico de Quetzal desde Postman.
 *
 * Endpoints:
 *   POST http://localhost:8080/lexico
 *   Body (raw JSON): { "codigo": "entero edad = 25\nconsola.mostrar(edad)" }
 *
 *   GET  http://localhost:8080/salud
 *   → Verifica que el servidor esté vivo
 */
public class ServidorLexico {

    private static final int PUERTO = 8080;

    public static void main(String[] args) throws IOException {

        HttpServer servidor = HttpServer.create(new InetSocketAddress(PUERTO), 0);

        // ── GET /salud ────────────────────────────────────────────────────────
        servidor.createContext("/salud", exchange -> {
            if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                responder(exchange, 405, "{\"error\":\"Método no permitido\"}");
                return;
            }
            String respuesta = "{\"estado\":\"OK\",\"mensaje\":\"Compilador Quetzal activo\",\"version\":\"1.0\"}";
            responder(exchange, 200, respuesta);
        });

        // ── POST /lexico ──────────────────────────────────────────────────────
        servidor.createContext("/lexico", exchange -> {
            // Habilitar CORS (para pruebas desde navegador también)
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                responder(exchange, 204, "");
                return;
            }

            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                responder(exchange, 405, "{\"error\":\"Solo se acepta POST\"}");
                return;
            }

            try {
                // Leer el cuerpo de la petición
                String cuerpo = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

                // Extraer el campo "codigo" del JSON manualmente
                String codigo = extraerCampoCodigo(cuerpo);
                if (codigo == null) {
                    responder(exchange, 400,
                            "{\"error\":\"El cuerpo debe ser JSON con el campo 'codigo'\"}");
                    return;
                }

                // Ejecutar el Analizador Léxico
                AnalizadorLexico lexico = new AnalizadorLexico(codigo);
                List<Token> tokens = lexico.analizar();

                // Construir respuesta JSON
                String json = construirJson(codigo, tokens, lexico.getErrores());
                responder(exchange, 200, json);

            } catch (Exception e) {
                responder(exchange, 500,
                        "{\"error\":\"Error interno: " + escaparJson(e.getMessage()) + "\"}");
            }
        });

        servidor.setExecutor(null);
        servidor.start();

        System.out.println("═══════════════════════════════════════════════════");
        System.out.println("  Compilador Quetzal - API Léxica");
        System.out.println("  Puerto: " + PUERTO);
        System.out.println("═══════════════════════════════════════════════════");
        System.out.println("  Endpoints disponibles:");
        System.out.println("  GET  http://localhost:" + PUERTO + "/salud");
        System.out.println("  POST http://localhost:" + PUERTO + "/lexico");
        System.out.println("───────────────────────────────────────────────────");
        System.out.println("  Body para POST (JSON):");
        System.out.println("  { \"codigo\": \"entero edad = 25\" }");
        System.out.println("═══════════════════════════════════════════════════");
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private static void responder(HttpExchange exchange, int codigo, String cuerpo) throws IOException {
        byte[] bytes = cuerpo.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(codigo, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    /**
     * Extrae el valor del campo "codigo" de un JSON simple.
     * Soporta: { "codigo": "entero edad = 25\nconsola.mostrar(edad)" }
     */
    private static String extraerCampoCodigo(String json) {
        // Buscar "codigo" en el JSON
        int idx = json.indexOf("\"codigo\"");
        if (idx == -1) return null;

        // Encontrar el valor después de "codigo":
        int dospuntos = json.indexOf(':', idx);
        if (dospuntos == -1) return null;

        // Encontrar la comilla de apertura del valor
        int abre = json.indexOf('"', dospuntos + 1);
        if (abre == -1) return null;

        // Encontrar la comilla de cierre (respetando escapes)
        StringBuilder sb = new StringBuilder();
        int i = abre + 1;
        while (i < json.length()) {
            char c = json.charAt(i);
            if (c == '\\' && i + 1 < json.length()) {
                char siguiente = json.charAt(i + 1);
                switch (siguiente) {
                    case 'n':  sb.append('\n'); i += 2; continue;
                    case 't':  sb.append('\t'); i += 2; continue;
                    case 'r':  sb.append('\r'); i += 2; continue;
                    case '"':  sb.append('"');  i += 2; continue;
                    case '\\': sb.append('\\'); i += 2; continue;
                    default:   sb.append('\\'); i++;    continue;
                }
            }
            if (c == '"') break;
            sb.append(c);
            i++;
        }
        return sb.toString();
    }

    /**
     * Construye el JSON de respuesta con todos los tokens y errores.
     */
    private static String construirJson(String codigoOriginal, List<Token> tokens, List<String> errores) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"exitoso\": ").append(errores.isEmpty()).append(",\n");
        sb.append("  \"totalTokens\": ").append(contarSignificativos(tokens)).append(",\n");
        sb.append("  \"totalErrores\": ").append(errores.size()).append(",\n");

        // Tokens
        sb.append("  \"tokens\": [\n");
        boolean primero = true;
        for (Token t : tokens) {
            if (t.getTipo() == TipoToken.EOF) continue;
            if (t.getTipo() == TipoToken.NUEVA_LINEA) continue;

            if (!primero) sb.append(",\n");
            primero = false;

            sb.append("    {\n");
            sb.append("      \"tipo\": \"").append(clasificar(t.getTipo())).append("\",\n");
            sb.append("      \"valor\": \"").append(escaparJson(t.getValor())).append("\",\n");
            sb.append("      \"linea\": ").append(t.getLinea()).append(",\n");
            sb.append("      \"columna\": ").append(t.getColumna()).append("\n");
            sb.append("    }");
        }
        sb.append("\n  ],\n");

        // Errores
        sb.append("  \"errores\": [\n");
        for (int i = 0; i < errores.size(); i++) {
            sb.append("    \"").append(escaparJson(errores.get(i))).append("\"");
            if (i < errores.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("  ]\n");
        sb.append("}");

        return sb.toString();
    }

    /**
     * Clasifica cada TipoToken en una de las 5 categorías estándar:
     *   Palabra Reservada | Identificador | Literal | Operador | Delimitador
     */
    private static String clasificar(TipoToken tipo) {
        switch (tipo) {
            // ── Palabras Reservadas ───────────────────────────────────────────
            case ENTERO: case NUMERO: case TEXTO: case LOG: case VACIO:
            case LISTA: case JSN: case VAR:
            case SI: case SINO: case SINO_SI:
            case MIENTRAS: case PARA: case EN: case HACER:
            case RETORNAR: case ROMPER: case CONTINUAR:
            case FUNCION: case ASYNC: case ESPERAR:
            case OBJETO: case NUEVO: case AMBIENTE: case PRIVADO: case PUBLICO: case ESTATICO:
            case IMPORTAR: case EXPORTAR: case DESDE:
            case INTENTAR: case CAPTURAR: case FINALMENTE: case LANZAR:
            case VERDADERO: case FALSO: case NULO:
            case Y: case O: case NO:
                return "Palabra Reservada";

            // ── Identificadores ───────────────────────────────────────────────
            case IDENTIFICADOR:
                return "Identificador";

            // ── Literales ─────────────────────────────────────────────────────
            case LITERAL_ENTERO:
                return "Literal Entero";
            case LITERAL_REAL:
                return "Literal Flotante";
            case LITERAL_TEXTO:
                return "Literal String";
            case LITERAL_TEXTO_INTERP:
                return "Literal String Interpolado";

            // ── Operadores ────────────────────────────────────────────────────
            case OP_SUMA: case OP_RESTA: case OP_MULT: case OP_DIV: case OP_MOD:
                return "Operador Aritmético";
            case OP_INCREMENTO: case OP_DECREMENTO:
                return "Operador Aritmético";
            case OP_IGUAL: case OP_DIFERENTE:
            case OP_MENOR: case OP_MAYOR: case OP_MENOR_IGUAL: case OP_MAYOR_IGUAL:
                return "Operador Relacional";
            case OP_AND: case OP_OR: case OP_NOT:
                return "Operador Lógico";
            case OP_ASIGNACION:
            case OP_SUMA_ASIG: case OP_RESTA_ASIG:
            case OP_MULT_ASIG: case OP_DIV_ASIG: case OP_MOD_ASIG:
                return "Operador Asignación";
            case INTERROGACION:
                return "Operador Ternario";

            // ── Delimitadores / Puntuación ────────────────────────────────────
            case PAREN_IZQ: case PAREN_DER:
            case LLAVE_IZQ: case LLAVE_DER:
            case CORCHETE_IZQ: case CORCHETE_DER:
            case PUNTO: case COMA: case DOS_PUNTOS: case PUNTO_COMA:
            case MENOR_TIPO: case MAYOR_TIPO:
                return "Delimitador";

            // ── Desconocido ───────────────────────────────────────────────────
            default:
                return "Desconocido";
        }
    }

    private static int contarSignificativos(List<Token> tokens) {
        int count = 0;
        for (Token t : tokens) {
            if (t.getTipo() != TipoToken.EOF && t.getTipo() != TipoToken.NUEVA_LINEA) count++;
        }
        return count;
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
