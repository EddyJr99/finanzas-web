package co.edu.sena.finanzas.servlet;

/** Una opcion de lista desplegable: id y nombre de una cuenta, medio de pago o categoria. */
public class OpcionCatalogo {

    private final int id;
    private final String nombre;

    public OpcionCatalogo(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    /**
     * Convierte un texto con formato "id - nombre" (como lo devuelve CatalogoDAO)
     * en una opcion. Devuelve null si el texto no tiene ese formato.
     */
    public static OpcionCatalogo desdeTexto(String texto) {
        int separador = texto.indexOf(" - ");
        if (separador < 0) {
            return null;
        }
        try {
            int id = Integer.parseInt(texto.substring(0, separador).trim());
            return new OpcionCatalogo(id, texto.substring(separador + 3));
        } catch (NumberFormatException excepcion) {
            return null;
        }
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }
}
