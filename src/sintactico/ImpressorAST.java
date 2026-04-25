package sintactico;

import sintactico.nodos.*;

import java.util.List;

/**
 * Imprime el AST de forma indentada para visualizar el resultado del parser.
 * Implementa VisitanteNodo<String> -- devuelve el árbol como texto.
 */
public class ImpressorAST implements VisitanteNodo<String> {

        // ─── Caracteres de árbol ──────────────────────────────────────────────────
        private static final String RAMA       = "├── ";   // nodo intermedio
        private static final String ULTIMO     = "└── ";   // último nodo del nivel
        private static final String VERTICAL   = "│   ";   // continuación vertical
        private static final String ESPACIO    = "    ";   // sin continuación

        // prefijo acumulado que se pasa recursivamente
        private String prefijo = "";

        // ─── Helpers ─────────────────────────────────────────────────────────────

        /**
         * Imprime una lista de hijos con el prefijo correcto.
         * @param nodos  lista de nodos hijos
         * @return texto con todas las ramas hijas
         */
        private String hijos(List<Nodo> nodos) {
            if (nodos == null || nodos.isEmpty()) return "";

            StringBuilder sb = new StringBuilder();
            String prefijoAnterior = prefijo;

            for (int i = 0; i < nodos.size(); i++) {
                Nodo n = nodos.get(i);
                if (n == null) continue;

                boolean esUltimo = (i == nodos.size() - 1);
                sb.append("\n");
                sb.append(prefijoAnterior);
                sb.append(esUltimo ? ULTIMO : RAMA);

                // El hijo hereda el prefijo + continuación vertical (o espacio si es último)
                prefijo = prefijoAnterior + (esUltimo ? ESPACIO : VERTICAL);
                sb.append(n.aceptar(this));
            }

            prefijo = prefijoAnterior;
            return sb.toString();
        }

        /**
         * Imprime un único hijo (siempre como "último" del nivel).
         */
        private String hijo(Nodo n) {
            if (n == null) return " <nulo>";

            String prefijoAnterior = prefijo;
            StringBuilder sb = new StringBuilder();

            sb.append("\n");
            sb.append(prefijoAnterior);
            sb.append(ULTIMO);

            prefijo = prefijoAnterior + ESPACIO;
            sb.append(n.aceptar(this));

            prefijo = prefijoAnterior;
            return sb.toString();
        }

        /**
         * Igual que hijo() pero NO es el último (usa ├── en lugar de └──).
         * Útil cuando después vendrán más ramas en el mismo nivel.
         */
        private String hijoIntermedio(Nodo n) {
            if (n == null) return " <nulo>";

            String prefijoAnterior = prefijo;
            StringBuilder sb = new StringBuilder();

            sb.append("\n");
            sb.append(prefijoAnterior);
            sb.append(RAMA);

            prefijo = prefijoAnterior + VERTICAL;
            sb.append(n.aceptar(this));

            prefijo = prefijoAnterior;
            return sb.toString();
        }

        // ─── Visitantes ──────────────────────────────────────────────────────────

        @Override
        public String visitarPrograma(NodoPrograma n) {
            return "Programa" + hijos(n.sentencias);
        }

        @Override
        public String visitarDeclaracionVariable(NodoDeclaracionVariable n) {
            String etiqueta = "DeclVar " + n.tipo + (n.mutable ? " var" : "") + " " + n.nombre;
            return etiqueta + (n.valor != null ? hijo(n.valor) : "");
        }

        @Override
        public String visitarDeclaracionLista(NodoDeclaracionLista n) {
            String tipoStr = n.tipoElemento != null ? "<" + n.tipoElemento + ">" : "";
            String etiqueta = "DeclLista" + tipoStr + (n.mutable ? " var" : "") + " " + n.nombre;
            return etiqueta + (n.valor != null ? hijo(n.valor) : "");
        }

        @Override
        public String visitarDeclaracionJsn(NodoDeclaracionJsn n) {
            return "DeclJsn" + (n.mutable ? " var" : "") + " " + n.nombre + hijo(n.valor);
        }

        @Override
        public String visitarAsignacion(NodoAsignacion n) {
            String prefijoAnterior = prefijo;
            StringBuilder sb = new StringBuilder("Asignar");

            // objetivo es intermedio, valor es último
            sb.append("\n").append(prefijoAnterior).append(RAMA);
            prefijo = prefijoAnterior + VERTICAL;
            sb.append(n.objetivo.aceptar(this));

            sb.append("\n").append(prefijoAnterior).append(ULTIMO);
            prefijo = prefijoAnterior + ESPACIO;
            sb.append(n.valor.aceptar(this));

            prefijo = prefijoAnterior;
            return sb.toString();
        }

        @Override
        public String visitarAsignacionCompuesta(NodoAsignacionCompuesta n) {
            String prefijoAnterior = prefijo;
            StringBuilder sb = new StringBuilder("AsignarComp " + n.operador);

            sb.append("\n").append(prefijoAnterior).append(RAMA);
            prefijo = prefijoAnterior + VERTICAL;
            sb.append(n.objetivo.aceptar(this));

            sb.append("\n").append(prefijoAnterior).append(ULTIMO);
            prefijo = prefijoAnterior + ESPACIO;
            sb.append(n.valor.aceptar(this));

            prefijo = prefijoAnterior;
            return sb.toString();
        }

        @Override
        public String visitarIncrementoDecremento(NodoIncrementoDecremento n) {
            return "IncDec " + n.operador + hijo(n.objetivo);
        }

        @Override
        public String visitarBloque(NodoBloque n) {
            return "Bloque" + hijos(n.sentencias);
        }

        @Override
        public String visitarSi(NodoSi n) {
            String prefijoAnterior = prefijo;
            StringBuilder sb = new StringBuilder("Si");

            // Determinar cuántas ramas hay para saber cuál es la última
            boolean hayElseIf = !n.ramasElseIf.isEmpty();
            boolean hayElse   = n.cuerpoElse != null;

            // Condición: siempre intermedia (hay al menos cuerpoIf después)
            sb.append("\n").append(prefijoAnterior).append(RAMA).append("Condicion:");
            prefijo = prefijoAnterior + VERTICAL;
            sb.append(hijo(n.condicion));

            // CuerpoSi: intermedio si hay elseif o else
            boolean cuerpoSiEsUltimo = !hayElseIf && !hayElse;
            sb.append("\n").append(prefijoAnterior)
                    .append(cuerpoSiEsUltimo ? ULTIMO : RAMA).append("CuerpoSi:");
            prefijo = prefijoAnterior + (cuerpoSiEsUltimo ? ESPACIO : VERTICAL);
            sb.append(hijo(n.cuerpoIf));

            // Ramas ElseIf
            for (int i = 0; i < n.ramasElseIf.size(); i++) {
                NodoSi.RamaElseIf r = n.ramasElseIf.get(i);
                boolean esUltimaRama = (i == n.ramasElseIf.size() - 1) && !hayElse;

                sb.append("\n").append(prefijoAnterior)
                        .append(esUltimaRama ? ULTIMO : RAMA).append("SinoSi:");
                prefijo = prefijoAnterior + (esUltimaRama ? ESPACIO : VERTICAL);

                // Condición elseif es intermedia (cuerpo viene después)
                String prefijoRama = prefijo;
                sb.append("\n").append(prefijoRama).append(RAMA).append("Condicion:");
                prefijo = prefijoRama + VERTICAL;
                sb.append(hijo(r.condicion));

                sb.append("\n").append(prefijoRama).append(ULTIMO).append("Cuerpo:");
                prefijo = prefijoRama + ESPACIO;
                sb.append(hijo(r.cuerpo));
            }

            // Else
            if (hayElse) {
                sb.append("\n").append(prefijoAnterior).append(ULTIMO).append("Sino:");
                prefijo = prefijoAnterior + ESPACIO;
                sb.append(hijo(n.cuerpoElse));
            }

            prefijo = prefijoAnterior;
            return sb.toString();
        }

        @Override
        public String visitarMientras(NodoMientras n) {
            String prefijoAnterior = prefijo;
            StringBuilder sb = new StringBuilder("Mientras");

            sb.append("\n").append(prefijoAnterior).append(RAMA);
            prefijo = prefijoAnterior + VERTICAL;
            sb.append(n.condicion.aceptar(this));

            sb.append("\n").append(prefijoAnterior).append(ULTIMO);
            prefijo = prefijoAnterior + ESPACIO;
            sb.append(n.cuerpo.aceptar(this));

            prefijo = prefijoAnterior;
            return sb.toString();
        }

        @Override
        public String visitarHacer(NodoHacer n) {
            String prefijoAnterior = prefijo;
            StringBuilder sb = new StringBuilder("Hacer");

            sb.append("\n").append(prefijoAnterior).append(RAMA);
            prefijo = prefijoAnterior + VERTICAL;
            sb.append(n.cuerpo.aceptar(this));

            sb.append("\n").append(prefijoAnterior).append(ULTIMO);
            prefijo = prefijoAnterior + ESPACIO;
            sb.append(n.condicion.aceptar(this));

            prefijo = prefijoAnterior;
            return sb.toString();
        }

        @Override
        public String visitarPara(NodoPara n) {
            String prefijoAnterior = prefijo;
            StringBuilder sb = new StringBuilder("Para");

            // init, condicion, paso son intermedios; cuerpo es último
            sb.append("\n").append(prefijoAnterior).append(RAMA);
            prefijo = prefijoAnterior + VERTICAL;
            sb.append(n.init != null ? n.init.aceptar(this) : "<nulo>");

            sb.append("\n").append(prefijoAnterior).append(RAMA);
            prefijo = prefijoAnterior + VERTICAL;
            sb.append(n.condicion != null ? n.condicion.aceptar(this) : "<nulo>");

            sb.append("\n").append(prefijoAnterior).append(RAMA);
            prefijo = prefijoAnterior + VERTICAL;
            sb.append(n.paso != null ? n.paso.aceptar(this) : "<nulo>");

            sb.append("\n").append(prefijoAnterior).append(ULTIMO);
            prefijo = prefijoAnterior + ESPACIO;
            sb.append(n.cuerpo.aceptar(this));

            prefijo = prefijoAnterior;
            return sb.toString();
        }

        @Override
        public String visitarParaEn(NodoParaEn n) {
            String prefijoAnterior = prefijo;
            StringBuilder sb = new StringBuilder("ParaEn " + n.variable);

            sb.append("\n").append(prefijoAnterior).append(RAMA);
            prefijo = prefijoAnterior + VERTICAL;
            sb.append(n.coleccion.aceptar(this));

            sb.append("\n").append(prefijoAnterior).append(ULTIMO);
            prefijo = prefijoAnterior + ESPACIO;
            sb.append(n.cuerpo.aceptar(this));

            prefijo = prefijoAnterior;
            return sb.toString();
        }

        @Override
        public String visitarRetornar(NodoRetornar n) {
            return "Retornar" + (n.valor != null ? hijo(n.valor) : "");
        }

        @Override public String visitarRomper(NodoRomper n)       { return "Romper"; }
        @Override public String visitarContinuar(NodoContinuar n) { return "Continuar"; }

        @Override
        public String visitarDeclaracionFuncion(NodoDeclaracionFuncion n) {
            String params = n.parametros.stream()
                    .map(p -> p.tipo + " " + p.nombre)
                    .reduce((a, b) -> a + ", " + b).orElse("");
            String etiqueta = "Funcion" + (n.esAsync ? "(async)" : "")
                    + " " + n.tipoRetorno + " " + n.nombre + "(" + params + ")";
            return etiqueta + hijo(n.cuerpo);
        }

        @Override
        public String visitarLlamadaFuncion(NodoLlamadaFuncion n) {
            return "LlamarFun " + n.nombre + hijos(n.argumentos);
        }

        @Override
        public String visitarLlamadaMetodo(NodoLlamadaMetodo n) {
            String prefijoAnterior = prefijo;
            StringBuilder sb = new StringBuilder("LlamarMetodo ." + n.metodo);

            boolean hayArgs = n.argumentos != null && !n.argumentos.isEmpty();

            // objeto es intermedio si hay argumentos
            sb.append("\n").append(prefijoAnterior).append(hayArgs ? RAMA : ULTIMO);
            prefijo = prefijoAnterior + (hayArgs ? VERTICAL : ESPACIO);
            sb.append(n.objeto.aceptar(this));

            if (hayArgs) {
                prefijo = prefijoAnterior;
                sb.append(hijos(n.argumentos));
            }

            prefijo = prefijoAnterior;
            return sb.toString();
        }

        @Override
        public String visitarDeclaracionObjeto(NodoDeclaracionObjeto n) {
            String prefijoAnterior = prefijo;
            StringBuilder sb = new StringBuilder("Objeto " + n.nombre);

            boolean hayPriv   = !n.atributosPrivados.isEmpty();
            boolean hayCtor   = n.constructor != null;
            boolean hayPublic = !n.metodosPublicos.isEmpty();

            if (hayPriv) {
                boolean esUltimo = !hayCtor && !hayPublic;
                sb.append("\n").append(prefijoAnterior).append(esUltimo ? ULTIMO : RAMA).append("privado:");
                prefijo = prefijoAnterior + (esUltimo ? ESPACIO : VERTICAL);
                for (NodoDeclaracionVariable a : n.atributosPrivados) sb.append(hijo(a));
            }
            if (hayCtor) {
                boolean esUltimo = !hayPublic;
                sb.append("\n").append(prefijoAnterior).append(esUltimo ? ULTIMO : RAMA).append("constructor:");
                prefijo = prefijoAnterior + (esUltimo ? ESPACIO : VERTICAL);
                sb.append(hijo(n.constructor));
            }
            if (hayPublic) {
                sb.append("\n").append(prefijoAnterior).append(ULTIMO).append("publico:");
                prefijo = prefijoAnterior + ESPACIO;
                for (NodoDeclaracionFuncion m : n.metodosPublicos) sb.append(hijo(m));
            }

            prefijo = prefijoAnterior;
            return sb.toString();
        }

        @Override
        public String visitarNuevoObjeto(NodoNuevoObjeto n) {
            return "Nuevo " + n.tipoObjeto + hijos(n.argumentos);
        }

        @Override
        public String visitarAccesoMiembro(NodoAccesoMiembro n) {
            return "AccesoMiembro ." + n.campo + hijo(n.objeto);
        }

        @Override
        public String visitarAccesoIndice(NodoAccesoIndice n) {
            String prefijoAnterior = prefijo;
            StringBuilder sb = new StringBuilder("AccesoIndice");

            sb.append("\n").append(prefijoAnterior).append(RAMA);
            prefijo = prefijoAnterior + VERTICAL;
            sb.append(n.coleccion.aceptar(this));

            sb.append("\n").append(prefijoAnterior).append(ULTIMO);
            prefijo = prefijoAnterior + ESPACIO;
            sb.append(n.indice.aceptar(this));

            prefijo = prefijoAnterior;
            return sb.toString();
        }

        @Override
        public String visitarIntentar(NodoIntentar n) {
            String prefijoAnterior = prefijo;
            StringBuilder sb = new StringBuilder("Intentar");

            boolean hayCapturar  = n.cuerpoCapturar != null;
            boolean hayFinalmente = n.cuerpoFinalmente != null;

            // cuerpoIntentar
            sb.append("\n").append(prefijoAnterior)
                    .append((hayCapturar || hayFinalmente) ? RAMA : ULTIMO).append("Try:");
            prefijo = prefijoAnterior + ((hayCapturar || hayFinalmente) ? VERTICAL : ESPACIO);
            sb.append(hijo(n.cuerpoIntentar));

            if (hayCapturar) {
                sb.append("\n").append(prefijoAnterior)
                        .append(hayFinalmente ? RAMA : ULTIMO)
                        .append("Capturar(" + n.variableError + "):");
                prefijo = prefijoAnterior + (hayFinalmente ? VERTICAL : ESPACIO);
                sb.append(hijo(n.cuerpoCapturar));
            }

            if (hayFinalmente) {
                sb.append("\n").append(prefijoAnterior).append(ULTIMO).append("Finalmente:");
                prefijo = prefijoAnterior + ESPACIO;
                sb.append(hijo(n.cuerpoFinalmente));
            }

            prefijo = prefijoAnterior;
            return sb.toString();
        }

        @Override
        public String visitarLanzar(NodoLanzar n) {
            return "Lanzar" + hijo(n.expresion);
        }

        @Override
        public String visitarImportar(NodoImportar n) {
            return "Importar " + n.simbolos + " desde \"" + n.ruta + "\"";
        }

        @Override
        public String visitarExportar(NodoExportar n) {
            return "Exportar " + n.simbolos;
        }

        @Override
        public String visitarBinaria(NodoBinaria n) {
            String prefijoAnterior = prefijo;
            StringBuilder sb = new StringBuilder("Binaria " + n.operador);

            sb.append("\n").append(prefijoAnterior).append(RAMA);
            prefijo = prefijoAnterior + VERTICAL;
            sb.append(n.izquierda.aceptar(this));

            sb.append("\n").append(prefijoAnterior).append(ULTIMO);
            prefijo = prefijoAnterior + ESPACIO;
            sb.append(n.derecha.aceptar(this));

            prefijo = prefijoAnterior;
            return sb.toString();
        }

        @Override
        public String visitarUnaria(NodoUnaria n) {
            return "Unaria " + n.operador + hijo(n.operando);
        }

        @Override
        public String visitarTernaria(NodoTernaria n) {
            String prefijoAnterior = prefijo;
            StringBuilder sb = new StringBuilder("Ternaria ?:");

            sb.append("\n").append(prefijoAnterior).append(RAMA);
            prefijo = prefijoAnterior + VERTICAL;
            sb.append(n.condicion.aceptar(this));

            sb.append("\n").append(prefijoAnterior).append(RAMA);
            prefijo = prefijoAnterior + VERTICAL;
            sb.append(n.siVerdadero.aceptar(this));

            sb.append("\n").append(prefijoAnterior).append(ULTIMO);
            prefijo = prefijoAnterior + ESPACIO;
            sb.append(n.siFalso.aceptar(this));

            prefijo = prefijoAnterior;
            return sb.toString();
        }

        // ─── Literales e identificadores ─────────────────────────────────────────

        @Override public String visitarLiteralEntero(NodoLiteralEntero n)     { return "EnteroLit " + n.valor; }
        @Override public String visitarLiteralReal(NodoLiteralReal n)         { return "RealLit " + n.valor; }
        @Override public String visitarLiteralTexto(NodoLiteralTexto n)       { return "TextoLit \"" + n.valor + "\""; }
        @Override public String visitarLiteralTextoInterp(NodoLiteralTextoInterp n) { return "TextoInterp t\"" + n.plantilla + "\""; }
        @Override public String visitarLiteralLog(NodoLiteralLog n)           { return "LogLit " + n.valor; }
        @Override public String visitarLiteralNulo(NodoLiteralNulo n)         { return "NuloLit"; }
        @Override public String visitarIdentificador(NodoIdentificador n)     { return "Id " + n.nombre; }
        @Override public String visitarConsola(NodoConsola n)                 { return "Consola." + n.metodo + hijos(n.argumentos); }

        @Override
        public String visitarLiteralLista(NodoLiteralLista n) {
            return "ListaLit" + hijos(n.elementos);
        }

        @Override
        public String visitarLiteralJsn(NodoLiteralJsn n) {
            String prefijoAnterior = prefijo;
            StringBuilder sb = new StringBuilder("JsnLit");

            List<java.util.Map.Entry<String, Nodo>> entradas = new java.util.ArrayList<>(n.pares.entrySet());
            for (int i = 0; i < entradas.size(); i++) {
                var e = entradas.get(i);
                boolean esUltimo = (i == entradas.size() - 1);

                sb.append("\n").append(prefijoAnterior).append(esUltimo ? ULTIMO : RAMA).append(e.getKey() + ":");
                prefijo = prefijoAnterior + (esUltimo ? ESPACIO : VERTICAL);
                sb.append(hijo(e.getValue()));
            }

            prefijo = prefijoAnterior;
            return sb.toString();
        }

    }
