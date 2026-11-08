package co.edu.sena.finanzas.servlet;

/** Utilidades para mostrar texto de forma segura dentro de paginas HTML. */
public final class Html {

    private Html() {
    }

    /**
     * Reemplaza los caracteres especiales de HTML para evitar que el texto
     * escrito por el usuario se interprete como codigo (ataque XSS).
     */
    public static String escapar(String texto) {
        if (texto == null) {
            return "";
        }
        StringBuilder resultado = new StringBuilder(texto.length());
        for (char caracter : texto.toCharArray()) {
            switch (caracter) {
                case '&':
                    resultado.append("&amp;");
                    break;
                case '<':
                    resultado.append("&lt;");
                    break;
                case '>':
                    resultado.append("&gt;");
                    break;
                case '"':
                    resultado.append("&quot;");
                    break;
                case '\'':
                    resultado.append("&#39;");
                    break;
                default:
                    resultado.append(caracter);
            }
        }
        return resultado.toString();
    }
}
