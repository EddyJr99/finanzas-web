package co.edu.sena.finanzas.dao;

import co.edu.sena.finanzas.conexion.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Consulta las tablas de apoyo (cuentas, medios de pago y categorias) para que
 * el usuario pueda elegir un id valido al registrar un movimiento.
 */
public class CatalogoDAO {

    private static final String SQL_CUENTAS =
            "SELECT id_cuenta, nombre_cuenta FROM cuentas ORDER BY id_cuenta";
    private static final String SQL_MEDIOS_PAGO =
            "SELECT id_medio_pago, nombre_medio_pago FROM medios_pago ORDER BY id_medio_pago";
    private static final String SQL_CATEGORIAS =
            "SELECT id_categoria, nombre_categoria FROM categorias ORDER BY id_categoria";

    public List<String> listarCuentas() throws SQLException {
        return listar(SQL_CUENTAS);
    }

    public List<String> listarMediosPago() throws SQLException {
        return listar(SQL_MEDIOS_PAGO);
    }

    public List<String> listarCategorias() throws SQLException {
        return listar(SQL_CATEGORIAS);
    }

    /** Ejecuta una consulta de dos columnas (id, nombre) y la devuelve como "id - nombre". */
    private List<String> listar(String sql) throws SQLException {
        List<String> elementos = new ArrayList<>();
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet resultado = sentencia.executeQuery()) {

            while (resultado.next()) {
                elementos.add(resultado.getInt(1) + " - " + resultado.getString(2));
            }
        }
        return elementos;
    }
}
