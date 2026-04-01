package generador;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class CompiladorJava {
    // Nombre fijo de la clase generada (debe coincidir con GeneradorCodigo)
    private static final String NOMBRE_CLASE = "ProgramaQuetzal";

    /**
     * Compila el código Java generado a un archivo .class.
     *
     * @param codigoJava   String con el código Java generado por GeneradorCodigo
     * @param carpetaSalida Ruta de la carpeta donde se escribirá el .java y el .class
     * @return true si la compilación fue exitosa, false si hubo errores
     */
    public static boolean compilar(String codigoJava, String carpetaSalida) {

        // ── Paso 1: Crear la carpeta de salida si no existe ──────────────────
        File carpeta = new File(carpetaSalida);
        if (!carpeta.exists()) {
            boolean creada = carpeta.mkdirs();
            if (!creada) {
                System.err.println("[FASE 5] Error: No se pudo crear la carpeta de salida: "
                        + carpetaSalida);
                return false;
            }
        }

        // ── Paso 2: Escribir el String de código Java al archivo .java ───────
        File archivoJava = new File(carpeta, NOMBRE_CLASE + ".java");

        try (FileWriter escritor = new FileWriter(archivoJava, StandardCharsets.UTF_8)) {
            escritor.write(codigoJava);
        } catch (Exception e) {
            System.err.println("[FASE 5] Error al escribir el archivo .java: " + e.getMessage());
            return false;
        }

        System.out.println("[FASE 5] Archivo Java escrito en: " + archivoJava.getAbsolutePath());

        // ── Paso 3: Localizar javac en el JDK actual ─────────────────────────
        String rutaJavac = obtenerRutaJavac();
        if (rutaJavac == null) {
            System.err.println("[FASE 5] Error: No se encontró javac. "
                    + "Verifica que estás ejecutando con JDK y no solo JRE.");
            return false;
        }

        System.out.println("[FASE 5] Usando javac: " + rutaJavac);

        // ── Paso 4: Invocar javac con ProcessBuilder ──────────────────────────
        ProcessBuilder pb = new ProcessBuilder(
                rutaJavac,
                "-encoding", "UTF-8",
                "-d", carpeta.getAbsolutePath(),
                archivoJava.getAbsolutePath()
        );

        // Redirigir stderr al mismo stream que stdout para capturarlo junto
        pb.redirectErrorStream(true);

        int codigoSalida;
        StringBuilder salidaJavac = new StringBuilder();

        try {
            Process proceso = pb.start();

            // Leer la salida de javac línea a línea
            BufferedReader lector = new BufferedReader(
                    new InputStreamReader(proceso.getInputStream(), StandardCharsets.UTF_8)
            );

            String linea;
            while ((linea = lector.readLine()) != null) {
                salidaJavac.append(linea).append("\n");
            }

            // Esperar a que javac termine y obtener su código de salida
            codigoSalida = proceso.waitFor();

        } catch (Exception e) {
            System.err.println("[FASE 5] Error al ejecutar javac: " + e.getMessage());
            return false;
        }

        // ── Paso 5: Reportar resultado ────────────────────────────────────────
        if (codigoSalida == 0) {
            File archivoClass = new File(carpeta, NOMBRE_CLASE + ".class");
            System.out.println("[FASE 5] Compilación exitosa.");
            System.out.println("[FASE 5] Archivo .class generado en: "
                    + archivoClass.getAbsolutePath());
            System.out.println("[FASE 5] Para ejecutar:");
            System.out.println("         java -cp \"" + carpeta.getAbsolutePath()
                    + "\" " + NOMBRE_CLASE);
            return true;
        } else {
            System.err.println("[FASE 5] Errores de compilación Java:");
            System.err.println(salidaJavac.toString());
            return false;
        }
    }

    /**
     * Localiza el ejecutable javac usando la propiedad java.home del JDK activo.
     * En JDK 9+ java.home apunta a la raíz del JDK directamente.
     * En JDK 8 java.home apunta a jre/ dentro del JDK, por eso se sube un nivel.
     *
     * @return Ruta absoluta al ejecutable javac, o null si no se encuentra
     */
    private static String obtenerRutaJavac() {
        String javaHome = System.getProperty("java.home");
        if (javaHome == null) return null;

        // JDK 9+ : java.home = /ruta/jdk-17
        File javac = new File(javaHome, "bin" + File.separator + "javac");
        if (javac.exists()) return javac.getAbsolutePath();

        // JDK 8 : java.home = /ruta/jdk1.8/jre → subir un nivel
        javac = new File(javaHome + File.separator + "..", "bin" + File.separator + "javac");
        if (javac.exists()) return javac.getAbsolutePath();

        // Fallback: confiar en que javac está en el PATH del sistema
        return "javac";
    }
}
