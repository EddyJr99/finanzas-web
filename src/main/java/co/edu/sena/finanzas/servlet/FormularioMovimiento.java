package co.edu.sena.finanzas.servlet;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import co.edu.sena.finanzas.modelo.Movimiento;

/**
 * Lee y valida los datos que llegan del formulario HTML de un movimiento.
 *
 * Conserva los textos tal como los escribio el usuario para poder volver a
 * mostrarlos si hay errores, y acumula los mensajes de error encontrados.
 */
public class FormularioMovimiento {

    private static final int LONGITUD_MAXIMA_DESCRIPCION = 150;

    private final String fecha;
    private final String descripcion;
    private final String montoTotal;
    private final String idCuenta;
    private final String idMedioPago;
    private final String idCategoria;
    private final List<String> errores = new ArrayList<>();

    public FormularioMovimiento(HttpServletRequest solicitud) {
        this.fecha = leer(solicitud, "fecha");
        this.descripcion = leer(solicitud, "descripcion");
        this.montoTotal = leer(solicitud, "montoTotal");
        this.idCuenta = leer(solicitud, "idCuenta");
        this.idMedioPago = leer(solicitud, "idMedioPago");
        this.idCategoria = leer(solicitud, "idCategoria");
    }

    private static String leer(HttpServletRequest solicitud, String nombre) {
        String valor = solicitud.getParameter(nombre);
        return valor == null ? "" : valor.trim();
    }

    /**
     * Valida los campos contra los catalogos disponibles.
     *
     * @return el movimiento listo para guardar, o null si hay errores
     *         (consultar {@link #getErrores()})
     */
    public Movimiento validar(List<OpcionCatalogo> cuentas, List<OpcionCatalogo> mediosPago,
            List<OpcionCatalogo> categorias) {
        LocalDate fechaValida = validarFecha();
        BigDecimal montoValido = validarMonto();
        if (descripcion.isEmpty()) {
            errores.add("La descripcion es obligatoria.");
        } else if (descripcion.length() > LONGITUD_MAXIMA_DESCRIPCION) {
            errores.add("La descripcion admite maximo " + LONGITUD_MAXIMA_DESCRIPCION
                    + " caracteres.");
        }
        int cuentaValida = validarOpcion(idCuenta, cuentas, "cuenta");
        int medioPagoValido = validarOpcion(idMedioPago, mediosPago, "medio de pago");
        int categoriaValida = validarOpcion(idCategoria, categorias, "categoria");

        if (!errores.isEmpty()) {
            return null;
        }
        return new Movimiento(fechaValida, descripcion, montoValido, cuentaValida,
                medioPagoValido, categoriaValida);
    }

    private LocalDate validarFecha() {
        if (fecha.isEmpty()) {
            errores.add("La fecha es obligatoria.");
            return null;
        }
        try {
            LocalDate fechaLeida = LocalDate.parse(fecha);
            if (fechaLeida.isAfter(LocalDate.now())) {
                errores.add("La fecha no puede ser futura.");
            }
            return fechaLeida;
        } catch (DateTimeParseException excepcion) {
            errores.add("La fecha no tiene el formato AAAA-MM-DD.");
            return null;
        }
    }

    private BigDecimal validarMonto() {
        if (montoTotal.isEmpty()) {
            errores.add("El monto es obligatorio.");
            return null;
        }
        try {
            BigDecimal monto = new BigDecimal(montoTotal);
            if (monto.signum() <= 0 || monto.scale() > 2) {
                errores.add("El monto debe ser mayor que 0 y tener maximo 2 decimales.");
            }
            return monto;
        } catch (NumberFormatException excepcion) {
            errores.add("El monto debe ser un numero valido, por ejemplo 25000.50.");
            return null;
        }
    }

    private int validarOpcion(String texto, List<OpcionCatalogo> opciones, String nombreCampo) {
        try {
            int id = Integer.parseInt(texto);
            for (OpcionCatalogo opcion : opciones) {
                if (opcion.getId() == id) {
                    return id;
                }
            }
        } catch (NumberFormatException excepcion) {
            // Se informa abajo, igual que cuando el id no esta en el catalogo.
        }
        errores.add("Seleccione una " + nombreCampo + " valida de la lista.");
        return -1;
    }

    public String getFecha() {
        return fecha;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getMontoTotal() {
        return montoTotal;
    }

    public String getIdCuenta() {
        return idCuenta;
    }

    public String getIdMedioPago() {
        return idMedioPago;
    }

    public String getIdCategoria() {
        return idCategoria;
    }

    public List<String> getErrores() {
        return errores;
    }
}
