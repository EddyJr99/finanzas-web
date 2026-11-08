package co.edu.sena.finanzas.dao;

import co.edu.sena.finanzas.conexion.ConexionBD;
import co.edu.sena.finanzas.modelo.Movimiento;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Acceso a datos (DAO) del modulo Movimientos sobre la base sgfp.
 * Implementa las cuatro operaciones CRUD con JDBC y PreparedStatement,
 * que evita la inyeccion SQL al enviar los valores como parametros.
 *
 * Un movimiento se guarda en dos tablas: "movimientos" y "detalle_categoria".
 * Por eso insertar, actualizar y eliminar usan una transaccion: o se hacen
 * todos los cambios o no se hace ninguno.
 */
public class MovimientoDAO {

    private static final String SQL_INSERTAR_MOVIMIENTO =
            "INSERT INTO movimientos (fecha, descripcion, monto_total, id_cuenta, id_medio_pago) "
                    + "VALUES (?, ?, ?, ?, ?)";
    private static final String SQL_INSERTAR_DETALLE =
            "INSERT INTO detalle_categoria (id_movimiento, id_categoria, monto_asignado) "
                    + "VALUES (?, ?, ?)";
    private static final String SQL_ACTUALIZAR_MOVIMIENTO =
            "UPDATE movimientos SET fecha = ?, descripcion = ?, monto_total = ?, "
                    + "id_cuenta = ?, id_medio_pago = ? WHERE id_movimiento = ?";
    /** Para movimientos repartidos en varias categorias: no se toca el monto ni el detalle. */
    private static final String SQL_ACTUALIZAR_SIN_MONTO =
            "UPDATE movimientos SET fecha = ?, descripcion = ?, "
                    + "id_cuenta = ?, id_medio_pago = ? WHERE id_movimiento = ?";
    private static final String SQL_CONTAR_DETALLES =
            "SELECT COUNT(*) FROM detalle_categoria WHERE id_movimiento = ?";
    private static final String SQL_MONTO_DE_DETALLE =
            "SELECT monto_asignado FROM detalle_categoria "
                    + "WHERE id_movimiento = ? AND id_categoria = ?";
    private static final String SQL_ELIMINAR_UN_DETALLE =
            "DELETE FROM detalle_categoria WHERE id_movimiento = ? AND id_categoria = ?";
    private static final String SQL_RESTAR_MONTO =
            "UPDATE movimientos SET monto_total = monto_total - ? WHERE id_movimiento = ?";
    private static final String SQL_ELIMINAR_DETALLE =
            "DELETE FROM detalle_categoria WHERE id_movimiento = ?";
    private static final String SQL_ELIMINAR_MOVIMIENTO =
            "DELETE FROM movimientos WHERE id_movimiento = ?";

    /** Consulta base: une el movimiento con los nombres de cuenta, medio de pago y categoria. */
    private static final String SQL_CONSULTA_BASE =
            "SELECT m.id_movimiento, m.fecha, m.descripcion, m.monto_total, "
                    + "m.id_cuenta, c.nombre_cuenta, "
                    + "m.id_medio_pago, mp.nombre_medio_pago, "
                    + "dc.id_categoria, ca.nombre_categoria, dc.monto_asignado "
                    + "FROM movimientos m "
                    + "LEFT JOIN cuentas c ON c.id_cuenta = m.id_cuenta "
                    + "LEFT JOIN medios_pago mp ON mp.id_medio_pago = m.id_medio_pago "
                    + "LEFT JOIN detalle_categoria dc ON dc.id_movimiento = m.id_movimiento "
                    + "LEFT JOIN categorias ca ON ca.id_categoria = dc.id_categoria ";
    private static final String SQL_CONSULTAR_TODOS =
            SQL_CONSULTA_BASE + "ORDER BY m.fecha DESC, m.id_movimiento DESC";
    private static final String SQL_CONSULTAR_POR_ID =
            SQL_CONSULTA_BASE + "WHERE m.id_movimiento = ?";

    /**
     * INSERCION: guarda un movimiento nuevo y le asigna su categoria.
     *
     * @return id generado por la base de datos
     */
    public int insertar(Movimiento movimiento) throws SQLException {
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            conexion.setAutoCommit(false);
            try {
                int idGenerado = insertarMovimiento(conexion, movimiento);
                insertarDetalle(conexion, idGenerado, movimiento);
                conexion.commit();
                return idGenerado;
            } catch (SQLException excepcion) {
                conexion.rollback();
                throw excepcion;
            }
        }
    }

    /**
     * CONSULTA: devuelve los movimientos, los mas recientes primero.
     *
     * Un movimiento repartido en varias categorias aparece en una fila por cada
     * categoria, con el monto que le corresponde a esa categoria.
     */
    public List<Movimiento> consultarTodos() throws SQLException {
        List<Movimiento> movimientos = new ArrayList<>();
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_CONSULTAR_TODOS);
             ResultSet resultado = sentencia.executeQuery()) {

            while (resultado.next()) {
                movimientos.add(convertirFila(resultado));
            }
        }
        contarCategorias(movimientos);
        return movimientos;
    }

    /** CONSULTA: busca un movimiento por su id. */
    public Optional<Movimiento> consultarPorId(int idMovimiento) throws SQLException {
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_CONSULTAR_POR_ID)) {

            sentencia.setInt(1, idMovimiento);
            try (ResultSet resultado = sentencia.executeQuery()) {
                List<Movimiento> filas = new ArrayList<>();
                while (resultado.next()) {
                    agregarFila(filas, resultado);
                }
                return filas.isEmpty() ? Optional.<Movimiento>empty() : Optional.of(filas.get(0));
            }
        }
    }

    /**
     * ACTUALIZACION: modifica un movimiento existente y su categoria.
     *
     * Si el movimiento esta repartido en varias categorias solo se actualizan la
     * fecha, la descripcion, la cuenta y el medio de pago; el monto y el reparto
     * se conservan para no perder esa informacion.
     *
     * @return true si el movimiento existia y fue actualizado
     */
    public boolean actualizar(Movimiento movimiento) throws SQLException {
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            conexion.setAutoCommit(false);
            try {
                boolean actualizado;
                boolean dividido = contarDetalles(conexion, movimiento.getIdMovimiento()) > 1;
                if (dividido) {
                    try (PreparedStatement sentencia =
                                 conexion.prepareStatement(SQL_ACTUALIZAR_SIN_MONTO)) {
                        sentencia.setDate(1, Date.valueOf(movimiento.getFecha()));
                        sentencia.setString(2, movimiento.getDescripcion());
                        sentencia.setInt(3, movimiento.getIdCuenta());
                        sentencia.setInt(4, movimiento.getIdMedioPago());
                        sentencia.setInt(5, movimiento.getIdMovimiento());
                        actualizado = sentencia.executeUpdate() > 0;
                    }
                } else {
                    try (PreparedStatement sentencia =
                                 conexion.prepareStatement(SQL_ACTUALIZAR_MOVIMIENTO)) {
                        asignarParametrosMovimiento(sentencia, movimiento);
                        sentencia.setInt(6, movimiento.getIdMovimiento());
                        actualizado = sentencia.executeUpdate() > 0;
                    }
                    if (actualizado) {
                        eliminarDetalle(conexion, movimiento.getIdMovimiento());
                        insertarDetalle(conexion, movimiento.getIdMovimiento(), movimiento);
                    }
                }
                conexion.commit();
                return actualizado;
            } catch (SQLException excepcion) {
                conexion.rollback();
                throw excepcion;
            }
        }
    }

    /**
     * ELIMINACION: borra un movimiento y su detalle de categoria.
     *
     * @return true si el movimiento existia y fue eliminado
     */
    public boolean eliminar(int idMovimiento) throws SQLException {
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            conexion.setAutoCommit(false);
            try {
                eliminarDetalle(conexion, idMovimiento);
                boolean eliminado;
                try (PreparedStatement sentencia =
                             conexion.prepareStatement(SQL_ELIMINAR_MOVIMIENTO)) {
                    sentencia.setInt(1, idMovimiento);
                    eliminado = sentencia.executeUpdate() > 0;
                }
                conexion.commit();
                return eliminado;
            } catch (SQLException excepcion) {
                conexion.rollback();
                throw excepcion;
            }
        }
    }

    /**
     * ELIMINACION de una fila de la lista (una categoria de un movimiento).
     *
     * Si el movimiento esta repartido en varias categorias, solo se quita la
     * categoria indicada y su monto se resta del total del movimiento; las demas
     * categorias se conservan. Si era la unica categoria (o el movimiento no tiene
     * ninguna), se elimina el movimiento completo.
     *
     * @return true si algo fue eliminado
     */
    public boolean eliminarCategoria(int idMovimiento, int idCategoria) throws SQLException {
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            conexion.setAutoCommit(false);
            try {
                if (contarDetalles(conexion, idMovimiento) <= 1) {
                    conexion.rollback();
                    return eliminar(idMovimiento);
                }
                java.math.BigDecimal montoAsignado = null;
                try (PreparedStatement sentencia = conexion.prepareStatement(SQL_MONTO_DE_DETALLE)) {
                    sentencia.setInt(1, idMovimiento);
                    sentencia.setInt(2, idCategoria);
                    try (ResultSet resultado = sentencia.executeQuery()) {
                        if (resultado.next()) {
                            montoAsignado = resultado.getBigDecimal(1);
                        }
                    }
                }
                if (montoAsignado == null) {
                    conexion.rollback();
                    return false;
                }
                try (PreparedStatement sentencia =
                             conexion.prepareStatement(SQL_ELIMINAR_UN_DETALLE)) {
                    sentencia.setInt(1, idMovimiento);
                    sentencia.setInt(2, idCategoria);
                    sentencia.executeUpdate();
                }
                try (PreparedStatement sentencia = conexion.prepareStatement(SQL_RESTAR_MONTO)) {
                    sentencia.setBigDecimal(1, montoAsignado);
                    sentencia.setInt(2, idMovimiento);
                    sentencia.executeUpdate();
                }
                conexion.commit();
                return true;
            } catch (SQLException excepcion) {
                conexion.rollback();
                throw excepcion;
            }
        }
    }

    private int insertarMovimiento(Connection conexion, Movimiento movimiento)
            throws SQLException {
        try (PreparedStatement sentencia = conexion.prepareStatement(
                SQL_INSERTAR_MOVIMIENTO, Statement.RETURN_GENERATED_KEYS)) {
            asignarParametrosMovimiento(sentencia, movimiento);
            sentencia.executeUpdate();
            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                if (claves.next()) {
                    return claves.getInt(1);
                }
                throw new SQLException("La base de datos no devolvio el id generado.");
            }
        }
    }

    /** Asigna toda la cantidad del movimiento a la categoria elegida. */
    private void insertarDetalle(Connection conexion, int idMovimiento, Movimiento movimiento)
            throws SQLException {
        try (PreparedStatement sentencia = conexion.prepareStatement(SQL_INSERTAR_DETALLE)) {
            sentencia.setInt(1, idMovimiento);
            sentencia.setInt(2, movimiento.getIdCategoria());
            sentencia.setBigDecimal(3, movimiento.getMontoTotal());
            sentencia.executeUpdate();
        }
    }

    private int contarDetalles(Connection conexion, int idMovimiento) throws SQLException {
        try (PreparedStatement sentencia = conexion.prepareStatement(SQL_CONTAR_DETALLES)) {
            sentencia.setInt(1, idMovimiento);
            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next() ? resultado.getInt(1) : 0;
            }
        }
    }

    private void eliminarDetalle(Connection conexion, int idMovimiento) throws SQLException {
        try (PreparedStatement sentencia = conexion.prepareStatement(SQL_ELIMINAR_DETALLE)) {
            sentencia.setInt(1, idMovimiento);
            sentencia.executeUpdate();
        }
    }

    /** Asigna los cinco campos editables (posiciones 1 a 5) a la sentencia. */
    private void asignarParametrosMovimiento(PreparedStatement sentencia, Movimiento movimiento)
            throws SQLException {
        sentencia.setDate(1, Date.valueOf(movimiento.getFecha()));
        sentencia.setString(2, movimiento.getDescripcion());
        sentencia.setBigDecimal(3, movimiento.getMontoTotal());
        sentencia.setInt(4, movimiento.getIdCuenta());
        sentencia.setInt(5, movimiento.getIdMedioPago());
    }

    /**
     * Agrega la fila actual a la lista. Un movimiento repartido en varias categorias
     * produce varias filas en la consulta; aqui se unen en un solo Movimiento cuyo
     * nombre de categoria lista todas las categorias.
     */
    private void agregarFila(List<Movimiento> movimientos, ResultSet resultado)
            throws SQLException {
        Movimiento fila = convertirFila(resultado);
        for (Movimiento existente : movimientos) {
            if (existente.getIdMovimiento() == fila.getIdMovimiento()) {
                if (fila.getNombreCategoria() != null) {
                    existente.setNombreCategoria(existente.getNombreCategoria() == null
                            ? fila.getNombreCategoria()
                            : existente.getNombreCategoria() + ", " + fila.getNombreCategoria());
                    existente.setCantidadCategorias(existente.getCantidadCategorias() + 1);
                }
                return;
            }
        }
        contarCategoriaInicial(fila);
        movimientos.add(fila);
    }

    /** Calcula, para cada fila, en cuantas categorias esta repartido su movimiento. */
    private void contarCategorias(List<Movimiento> filas) {
        for (Movimiento fila : filas) {
            int cantidad = 0;
            for (Movimiento otra : filas) {
                if (otra.getIdMovimiento() == fila.getIdMovimiento()
                        && otra.getNombreCategoria() != null) {
                    cantidad++;
                }
            }
            fila.setCantidadCategorias(cantidad);
        }
    }

    private void contarCategoriaInicial(Movimiento fila) {
        fila.setCantidadCategorias(fila.getNombreCategoria() == null ? 0 : 1);
    }

    /** Convierte la fila actual del ResultSet en un objeto Movimiento. */
    private Movimiento convertirFila(ResultSet resultado) throws SQLException {
        Movimiento movimiento = new Movimiento();
        movimiento.setIdMovimiento(resultado.getInt("id_movimiento"));
        Date fecha = resultado.getDate("fecha");
        movimiento.setFecha(fecha == null ? null : fecha.toLocalDate());
        movimiento.setDescripcion(resultado.getString("descripcion"));
        movimiento.setMontoTotal(resultado.getBigDecimal("monto_total"));
        movimiento.setIdCuenta(resultado.getInt("id_cuenta"));
        movimiento.setNombreCuenta(resultado.getString("nombre_cuenta"));
        movimiento.setIdMedioPago(resultado.getInt("id_medio_pago"));
        movimiento.setNombreMedioPago(resultado.getString("nombre_medio_pago"));
        movimiento.setIdCategoria(resultado.getInt("id_categoria"));
        movimiento.setNombreCategoria(resultado.getString("nombre_categoria"));
        movimiento.setMontoAsignado(resultado.getBigDecimal("monto_asignado"));
        return movimiento;
    }
}
