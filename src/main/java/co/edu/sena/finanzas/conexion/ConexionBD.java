package co.edu.sena.finanzas.conexion;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Administra la conexion JDBC con la base de datos MySQL "sgfp".
 *
 * Los datos de conexion se leen, en este orden de prioridad:
 * 1. Variables de entorno: SGFP_DB_URL, SGFP_DB_USER y SGFP_DB_PASSWORD.
 * 2. El archivo conexion.properties, si existe en la carpeta desde donde se
 *    ejecuta el programa (claves: url, usuario y clave).
 * 3. Valores por defecto: MySQL local en el puerto 3306, usuario root sin clave
 *    (la configuracion tipica de Laragon y XAMPP).
 */
public final class ConexionBD {

    private static final String ARCHIVO_CONFIGURACION = "conexion.properties";

    private static final String URL_POR_DEFECTO =
            "jdbc:mysql://localhost:3306/sgfp"
                    + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Bogota";
    private static final String USUARIO_POR_DEFECTO = "root";
    private static final String CLAVE_POR_DEFECTO = "";

    /** Clase de utilidad: no se instancia. */
    private ConexionBD() {
    }

    /**
     * Abre una nueva conexion. Quien la solicita debe cerrarla,
     * idealmente con try-with-resources.
     *
     * @return conexion abierta a la base de datos
     * @throws SQLException si no se logra conectar
     */
    public static Connection obtenerConexion() throws SQLException {
        Properties configuracion = leerArchivoConfiguracion();
        String url = leerValor("SGFP_DB_URL", configuracion, "url", URL_POR_DEFECTO);
        String usuario = leerValor("SGFP_DB_USER", configuracion, "usuario", USUARIO_POR_DEFECTO);
        String clave = leerValor("SGFP_DB_PASSWORD", configuracion, "clave", CLAVE_POR_DEFECTO);
        return DriverManager.getConnection(url, usuario, clave);
    }

    /** Lee conexion.properties si existe; si no, devuelve un conjunto vacio. */
    private static Properties leerArchivoConfiguracion() {
        Properties configuracion = new Properties();
        Path ruta = Path.of(ARCHIVO_CONFIGURACION);
        if (Files.isRegularFile(ruta)) {
            try (InputStream entrada = Files.newInputStream(ruta)) {
                configuracion.load(entrada);
            } catch (IOException excepcion) {
                System.out.println("Aviso: no se pudo leer " + ARCHIVO_CONFIGURACION
                        + ". Se usaran los valores por defecto.");
            }
        }
        return configuracion;
    }

    /** Devuelve la variable de entorno, o la clave del archivo, o el valor por defecto. */
    private static String leerValor(String variableEntorno, Properties configuracion,
                                    String clave, String valorPorDefecto) {
        String valorEntorno = System.getenv(variableEntorno);
        if (valorEntorno != null && !valorEntorno.isBlank()) {
            return valorEntorno;
        }
        return configuracion.getProperty(clave, valorPorDefecto).trim();
    }
}
