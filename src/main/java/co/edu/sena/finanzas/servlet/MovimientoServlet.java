package co.edu.sena.finanzas.servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import co.edu.sena.finanzas.dao.CatalogoDAO;
import co.edu.sena.finanzas.dao.MovimientoDAO;
import co.edu.sena.finanzas.modelo.Movimiento;

/**
 * Servlet controlador del modulo Movimientos (URL: /movimientos).
 *
 * - doGet: consultas que no modifican datos (listar, buscar, mostrar formularios).
 * - doPost: operaciones que modifican datos (crear, actualizar, eliminar).
 *
 * Despues de un POST exitoso se redirige a la lista (patron Post-Redirect-Get)
 * para que al recargar la pagina no se repita la operacion.
 */
public class MovimientoServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final String VISTA_LISTAR = "/WEB-INF/vistas/listar.jsp";
    private static final String VISTA_BUSCAR = "/WEB-INF/vistas/buscar.jsp";
    private static final String VISTA_FORMULARIO = "/WEB-INF/vistas/formulario.jsp";

    private static final String ACCION_LISTAR = "listar";
    private static final String ACCION_BUSCAR = "buscar";
    private static final String ACCION_NUEVO = "nuevo";
    private static final String ACCION_EDITAR = "editar";
    private static final String ACCION_CREAR = "crear";
    private static final String ACCION_ACTUALIZAR = "actualizar";
    private static final String ACCION_ELIMINAR = "eliminar";

    private final MovimientoDAO movimientoDAO = new MovimientoDAO();
    private final CatalogoDAO catalogoDAO = new CatalogoDAO();

    /** Atiende las solicitudes GET: solo lee datos y muestra paginas. */
    @Override
    protected void doGet(HttpServletRequest solicitud, HttpServletResponse respuesta)
            throws ServletException, IOException {
        solicitud.setCharacterEncoding("UTF-8");
        pasarMensajeDeSesion(solicitud);
        String accion = leerAccion(solicitud, ACCION_LISTAR);
        try {
            switch (accion) {
                case ACCION_BUSCAR:
                    buscar(solicitud, respuesta);
                    break;
                case ACCION_NUEVO:
                    mostrarFormularioNuevo(solicitud, respuesta);
                    break;
                case ACCION_EDITAR:
                    mostrarFormularioEdicion(solicitud, respuesta);
                    break;
                case ACCION_LISTAR:
                default:
                    listar(solicitud, respuesta);
                    break;
            }
        } catch (SQLException excepcion) {
            throw new ServletException("Error al consultar la base de datos", excepcion);
        }
    }

    /** Atiende las solicitudes POST: crea, actualiza o elimina movimientos. */
    @Override
    protected void doPost(HttpServletRequest solicitud, HttpServletResponse respuesta)
            throws ServletException, IOException {
        solicitud.setCharacterEncoding("UTF-8");
        String accion = leerAccion(solicitud, "");
        try {
            switch (accion) {
                case ACCION_CREAR:
                    guardar(solicitud, respuesta, false);
                    break;
                case ACCION_ACTUALIZAR:
                    guardar(solicitud, respuesta, true);
                    break;
                case ACCION_ELIMINAR:
                    eliminar(solicitud, respuesta);
                    break;
                default:
                    respuesta.sendError(HttpServletResponse.SC_BAD_REQUEST, "Accion no valida");
                    break;
            }
        } catch (SQLException excepcion) {
            throw new ServletException("Error al modificar la base de datos", excepcion);
        }
    }

    // ------------------------------------------------------------------ GET

    private void listar(HttpServletRequest solicitud, HttpServletResponse respuesta)
            throws SQLException, ServletException, IOException {
        solicitud.setAttribute("movimientos", movimientoDAO.consultarTodos());
        solicitud.getRequestDispatcher(VISTA_LISTAR).forward(solicitud, respuesta);
    }

    private void buscar(HttpServletRequest solicitud, HttpServletResponse respuesta)
            throws SQLException, ServletException, IOException {
        String textoId = solicitud.getParameter("id");
        if (textoId != null && !textoId.trim().isEmpty()) {
            solicitud.setAttribute("textoBuscado", textoId.trim());
            Integer id = convertirEntero(textoId);
            if (id == null) {
                solicitud.setAttribute("error", "El id debe ser un numero entero.");
            } else {
                Optional<Movimiento> encontrado = movimientoDAO.consultarPorId(id);
                if (encontrado.isPresent()) {
                    solicitud.setAttribute("movimiento", encontrado.get());
                } else {
                    solicitud.setAttribute("error", "No existe un movimiento con id " + id + ".");
                }
            }
        }
        solicitud.getRequestDispatcher(VISTA_BUSCAR).forward(solicitud, respuesta);
    }

    private void mostrarFormularioNuevo(HttpServletRequest solicitud,
            HttpServletResponse respuesta) throws SQLException, ServletException, IOException {
        cargarCatalogos(solicitud);
        solicitud.setAttribute("accionFormulario", ACCION_CREAR);
        solicitud.getRequestDispatcher(VISTA_FORMULARIO).forward(solicitud, respuesta);
    }

    private void mostrarFormularioEdicion(HttpServletRequest solicitud,
            HttpServletResponse respuesta) throws SQLException, ServletException, IOException {
        Integer id = convertirEntero(solicitud.getParameter("id"));
        Optional<Movimiento> existente = id == null ? Optional.<Movimiento>empty()
                : movimientoDAO.consultarPorId(id);
        if (!existente.isPresent()) {
            guardarMensaje(solicitud, "No existe el movimiento que intenta editar.");
            respuesta.sendRedirect(urlLista(solicitud));
            return;
        }
        cargarCatalogos(solicitud);
        solicitud.setAttribute("movimiento", existente.get());
        solicitud.setAttribute("accionFormulario", ACCION_ACTUALIZAR);
        solicitud.getRequestDispatcher(VISTA_FORMULARIO).forward(solicitud, respuesta);
    }

    // ----------------------------------------------------------------- POST

    private void guardar(HttpServletRequest solicitud, HttpServletResponse respuesta,
            boolean esActualizacion) throws SQLException, ServletException, IOException {
        List<OpcionCatalogo> cuentas = convertir(catalogoDAO.listarCuentas());
        List<OpcionCatalogo> mediosPago = convertir(catalogoDAO.listarMediosPago());
        List<OpcionCatalogo> categorias = convertir(catalogoDAO.listarCategorias());

        FormularioMovimiento formulario = new FormularioMovimiento(solicitud);
        Movimiento movimiento = formulario.validar(cuentas, mediosPago, categorias);

        Integer id = null;
        if (esActualizacion) {
            id = convertirEntero(solicitud.getParameter("idMovimiento"));
            if (id == null) {
                formulario.getErrores().add("Falta el id del movimiento a actualizar.");
                movimiento = null;
            }
        }

        if (movimiento == null) {
            // Datos invalidos: se vuelve al formulario con los errores y lo escrito.
            solicitud.setAttribute("cuentas", cuentas);
            solicitud.setAttribute("mediosPago", mediosPago);
            solicitud.setAttribute("categorias", categorias);
            solicitud.setAttribute("formulario", formulario);
            solicitud.setAttribute("idMovimiento", id);
            solicitud.setAttribute("accionFormulario",
                    esActualizacion ? ACCION_ACTUALIZAR : ACCION_CREAR);
            respuesta.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            solicitud.getRequestDispatcher(VISTA_FORMULARIO).forward(solicitud, respuesta);
            return;
        }

        if (esActualizacion) {
            movimiento.setIdMovimiento(id);
            boolean actualizado = movimientoDAO.actualizar(movimiento);
            guardarMensaje(solicitud, actualizado
                    ? "Movimiento " + id + " actualizado correctamente."
                    : "No se pudo actualizar: el movimiento " + id + " ya no existe.");
        } else {
            int idGenerado = movimientoDAO.insertar(movimiento);
            guardarMensaje(solicitud, "Movimiento registrado con id " + idGenerado + ".");
        }
        respuesta.sendRedirect(urlLista(solicitud));
    }

    /**
     * Elimina una fila de la lista. Si el movimiento esta repartido en varias
     * categorias solo se quita la categoria de esa fila; si no, se elimina el movimiento.
     */
    private void eliminar(HttpServletRequest solicitud, HttpServletResponse respuesta)
            throws SQLException, IOException {
        Integer id = convertirEntero(solicitud.getParameter("id"));
        Integer idCategoria = convertirEntero(solicitud.getParameter("idCategoria"));
        if (id == null) {
            guardarMensaje(solicitud, "No se indico un id valido para eliminar.");
        } else {
            Optional<Movimiento> actual = movimientoDAO.consultarPorId(id);
            boolean dividido = actual.isPresent() && actual.get().isDividido() && idCategoria != null;
            boolean eliminado = dividido
                    ? movimientoDAO.eliminarCategoria(id, idCategoria)
                    : movimientoDAO.eliminar(id);
            if (!eliminado) {
                guardarMensaje(solicitud, "No existe un movimiento con id " + id + ".");
            } else if (dividido) {
                guardarMensaje(solicitud, "Se quito la categoria del movimiento " + id
                        + ". Las demas categorias se conservaron.");
            } else {
                guardarMensaje(solicitud, "Movimiento " + id + " eliminado correctamente.");
            }
        }
        respuesta.sendRedirect(urlLista(solicitud));
    }

    // ------------------------------------------------------------- utilidades

    private void cargarCatalogos(HttpServletRequest solicitud) throws SQLException {
        solicitud.setAttribute("cuentas", convertir(catalogoDAO.listarCuentas()));
        solicitud.setAttribute("mediosPago", convertir(catalogoDAO.listarMediosPago()));
        solicitud.setAttribute("categorias", convertir(catalogoDAO.listarCategorias()));
    }

    private static List<OpcionCatalogo> convertir(List<String> textos) {
        List<OpcionCatalogo> opciones = new ArrayList<>();
        for (String texto : textos) {
            OpcionCatalogo opcion = OpcionCatalogo.desdeTexto(texto);
            if (opcion != null) {
                opciones.add(opcion);
            }
        }
        return opciones;
    }

    private static String leerAccion(HttpServletRequest solicitud, String porDefecto) {
        String accion = solicitud.getParameter("accion");
        return accion == null || accion.trim().isEmpty() ? porDefecto : accion.trim();
    }

    private static Integer convertirEntero(String texto) {
        if (texto == null) {
            return null;
        }
        try {
            return Integer.valueOf(texto.trim());
        } catch (NumberFormatException excepcion) {
            return null;
        }
    }

    private static String urlLista(HttpServletRequest solicitud) {
        return solicitud.getContextPath() + "/movimientos";
    }

    /** Guarda un aviso en la sesion para mostrarlo una sola vez en la pagina siguiente. */
    private static void guardarMensaje(HttpServletRequest solicitud, String mensaje) {
        solicitud.getSession().setAttribute("mensaje", mensaje);
    }

    /** Pasa el aviso guardado en la sesion a la solicitud actual y lo borra de la sesion. */
    private static void pasarMensajeDeSesion(HttpServletRequest solicitud) {
        HttpSession sesion = solicitud.getSession(false);
        if (sesion != null && sesion.getAttribute("mensaje") != null) {
            solicitud.setAttribute("mensaje", sesion.getAttribute("mensaje"));
            sesion.removeAttribute("mensaje");
        }
    }
}
