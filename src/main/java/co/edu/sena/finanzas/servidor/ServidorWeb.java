package co.edu.sena.finanzas.servidor;

import java.awt.Desktop;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;

/**
 * Arranca un servidor Tomcat embebido con la aplicacion web "finanzas".
 *
 * Asi el proyecto se ejecuta con un doble clic, sin instalar Tomcat aparte.
 * Los archivos JSP se toman de src/main/webapp y las clases Java (servlets,
 * DAO, modelo) de la carpeta out, que compila el script ejecutar_web.
 *
 * Uso: java -cp "out;lib/*" co.edu.sena.finanzas.servidor.ServidorWeb [puerto]
 */
public class ServidorWeb {

    private static final int PUERTO_POR_DEFECTO = 8080;
    private static final int PUERTOS_A_PROBAR = 10;
    private static final String CONTEXTO = "/finanzas";
    private static final String CARPETA_WEB = "src/main/webapp";
    private static final String CARPETA_TRABAJO = "out/tomcat-trabajo-";

    public static void main(String[] args) throws IOException, LifecycleException {
        Path carpetaWeb = Path.of(CARPETA_WEB).toAbsolutePath();
        if (!Files.isDirectory(carpetaWeb)) {
            System.out.println("No se encontro " + carpetaWeb
                    + ". Ejecute el programa desde la carpeta del proyecto.");
            return;
        }

        int puerto = buscarPuertoLibre(leerPuertoSolicitado(args));
        if (puerto < 0) {
            System.out.println("No hay un puerto libre para el servidor. "
                    + "Cierre otros programas o indique un puerto: ejecutar_web.bat 9090");
            return;
        }

        Tomcat tomcat = new Tomcat();
        // Una carpeta de trabajo por puerto: asi dos servidores abiertos a la vez no se pisan.
        tomcat.setBaseDir(Path.of(CARPETA_TRABAJO + puerto).toAbsolutePath().toString());
        tomcat.setPort(puerto);
        tomcat.getConnector();

        Context contexto = tomcat.addWebapp(CONTEXTO, carpetaWeb.toString());
        contexto.setParentClassLoader(ServidorWeb.class.getClassLoader());

        tomcat.start();

        String direccion = "http://localhost:" + puerto + CONTEXTO + "/";
        System.out.println();
        System.out.println("=== Servidor iniciado ===");
        System.out.println("Abra en el navegador: " + direccion);
        System.out.println("Para detenerlo cierre esta ventana o pulse Ctrl + C.");
        abrirNavegador(direccion);

        tomcat.getServer().await();
    }

    /** Lee el puerto pedido por argumento; si no hay o es invalido usa 8080. */
    private static int leerPuertoSolicitado(String[] args) {
        if (args.length > 0) {
            try {
                return Integer.parseInt(args[0].trim());
            } catch (NumberFormatException excepcion) {
                System.out.println("Puerto invalido, se usara " + PUERTO_POR_DEFECTO + ".");
            }
        }
        return PUERTO_POR_DEFECTO;
    }

    /** Devuelve el primer puerto libre a partir del indicado, o -1 si no hay. */
    private static int buscarPuertoLibre(int puertoInicial) {
        for (int puerto = puertoInicial; puerto < puertoInicial + PUERTOS_A_PROBAR; puerto++) {
            try (ServerSocket prueba = new ServerSocket(puerto)) {
                return puerto;
            } catch (IOException excepcion) {
                System.out.println("El puerto " + puerto + " esta ocupado, probando el siguiente...");
            }
        }
        return -1;
    }

    /** Intenta abrir el navegador; si no se puede, el usuario copia la direccion. */
    private static void abrirNavegador(String direccion) {
        try {
            if (Desktop.isDesktopSupported()
                    && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(URI.create(direccion));
            }
        } catch (IOException | UnsupportedOperationException | SecurityException excepcion) {
            // No es critico: la direccion ya se mostro en la consola.
        }
    }
}
