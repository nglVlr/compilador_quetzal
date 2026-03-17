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

public class ServidorLexico {

    private static final int PUERTO = 8080;

    public static void main(String[] args) throws IOException {

        HttpServer servidor = HttpServer.create(new InetSocketAddress(PUERTO), 0);

        servidor.createContext("/salud", exchange -> {
            if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                responder(exchange, 405, "{\"error\":\"Metodo no permitido\"}");
                return;
            }
            responder(exchange, 200,
                    "{\"estado\":\"OK\",\"mensaje\":\"Compilador Quetzal activo\",\"version\":\"1.0\"}");
        });

        servidor.createContext("/lexico", exchange -> {
            agregarCORS(exchange);
            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) { responder(exchange, 204, ""); return; }
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) { responder(exchange, 405, "{\"error\":\"Solo POST\"}"); return; }
            try {
                String cuerpo = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                String codigo = extraerCampoCodigo(cuerpo);
                if (codigo == null) { responder(exchange, 400, "{\"error\":\"El cuerpo debe ser JSON con campo 'codigo'\"}"); return; }
                AnalizadorLexico lexico = new AnalizadorLexico(codigo);
                List<Token> tokens = lexico.analizar();
                responder(exchange, 200, construirJson(tokens, lexico.getErrores()));
            } catch (Exception e) {
                responder(exchange, 500, "{\"error\":\"" + escaparJson(e.getMessage()) + "\"}");
            }
        });

        servidor.createContext("/lexico/texto", exchange -> {
            agregarCORS(exchange);
            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) { responder(exchange, 204, ""); return; }
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) { responder(exchange, 405, "{\"error\":\"Solo POST\"}"); return; }
            try {
                String codigo = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                if (codigo.isBlank()) { responder(exchange, 400, "{\"error\":\"Cuerpo vacio\"}"); return; }
                AnalizadorLexico lexico = new AnalizadorLexico(codigo);
                List<Token> tokens = lexico.analizar();
                responder(exchange, 200, construirJson(tokens, lexico.getErrores()));
            } catch (Exception e) {
                responder(exchange, 500, "{\"error\":\"" + escaparJson(e.getMessage()) + "\"}");
            }
        });

        servidor.setExecutor(null);
        servidor.start();
        System.out.println("Servidor corriendo en puerto " + PUERTO);
        System.out.println("POST http://localhost:" + PUERTO + "/lexico        body: JSON ");
        System.out.println("POST http://localhost:" + PUERTO + "/lexico/texto  body: Text ");
    }

    private static void agregarCORS(HttpExchange e) {
        e.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        e.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
        e.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
    }

    private static void responder(HttpExchange exchange, int codigo, String cuerpo) throws IOException {
        byte[] bytes = cuerpo.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(codigo, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) { os.write(bytes); }
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

    private static String construirJson(List<Token> tokens, List<String> errores) {
        int total = 0;
        for (Token t : tokens)
            if (t.getTipo() != TipoToken.EOF && t.getTipo() != TipoToken.NUEVA_LINEA) total++;

        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"exitoso\": ").append(errores.isEmpty()).append(",\n");
        sb.append("  \"totalTokens\": ").append(total).append(",\n");
        sb.append("  \"totalErrores\": ").append(errores.size()).append(",\n");
        sb.append("  \"tokens\": [\n");

        boolean primero = true;
        for (Token t : tokens) {
            if (t.getTipo() == TipoToken.EOF || t.getTipo() == TipoToken.NUEVA_LINEA) continue;
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
        sb.append("  \"errores\": [\n");
        for (int i = 0; i < errores.size(); i++) {
            sb.append("    \"").append(escaparJson(errores.get(i))).append("\"");
            if (i < errores.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("  ]\n}");
        return sb.toString();
    }

    private static String clasificar(TipoToken tipo) {
        switch (tipo) {
            case ENTERO: case NUMERO: case TEXTO: case LOG: case VACIO:
            case LISTA: case JSN: case VAR:
            case SI: case SINO: case SINO_SI: case MIENTRAS: case PARA: case EN: case HACER:
            case RETORNAR: case ROMPER: case CONTINUAR:
            case FUNCION: case ASYNC: case ESPERAR:
            case OBJETO: case NUEVO: case AMBIENTE: case PRIVADO: case PUBLICO: case ESTATICO:
            case IMPORTAR: case EXPORTAR: case DESDE:
            case INTENTAR: case CAPTURAR: case FINALMENTE: case LANZAR:
            case VERDADERO: case FALSO: case NULO:
            case Y: case O: case NO: case CONSOLA: case PEDIR: case MOSTRAR:
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
            case OP_SUMA_ASIG: case OP_RESTA_ASIG: case OP_MULT_ASIG: case OP_DIV_ASIG: case OP_MOD_ASIG:
                return "Operador Asignacion";
            case INTERROGACION:
                return "Operador Ternario";
            case PAREN_IZQ: case PAREN_DER:
            case LLAVE_IZQ: case LLAVE_DER:
            case CORCHETE_IZQ: case CORCHETE_DER:
            case PUNTO: case COMA: case DOS_PUNTOS: case PUNTO_COMA:
            case MENOR_TIPO: case MAYOR_TIPO:
                return "Delimitador";
            default:
                return "Desconocido";
        }
    }

    private static String escaparJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }
}