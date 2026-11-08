package co.edu.sena.finanzas.modelo;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Clase de modelo que representa un movimiento financiero de la base de datos sgfp.
 * Los atributos fecha, descripcion, montoTotal, idCuenta e idMedioPago corresponden
 * a columnas de la tabla "movimientos". El atributo idCategoria se guarda en la
 * tabla "detalle_categoria", que relaciona cada movimiento con su categoria.
 * Los atributos nombreCuenta, nombreMedioPago y nombreCategoria solo se llenan
 * al consultar, para mostrar datos legibles.
 */
public class Movimiento {

    private int idMovimiento;
    private LocalDate fecha;
    private String descripcion;
    private BigDecimal montoTotal;
    private int idCuenta;
    private int idMedioPago;
    private int idCategoria;

    private String nombreCuenta;
    private String nombreMedioPago;
    private String nombreCategoria;
    private int cantidadCategorias;
    private BigDecimal montoAsignado;

    /** Constructor vacio. */
    public Movimiento() {
    }

    /** Constructor para movimientos nuevos (el id lo genera la base de datos). */
    public Movimiento(LocalDate fecha, String descripcion, BigDecimal montoTotal,
                      int idCuenta, int idMedioPago, int idCategoria) {
        this.fecha = fecha;
        this.descripcion = descripcion;
        this.montoTotal = montoTotal;
        this.idCuenta = idCuenta;
        this.idMedioPago = idMedioPago;
        this.idCategoria = idCategoria;
    }

    public int getIdMovimiento() {
        return idMovimiento;
    }

    public void setIdMovimiento(int idMovimiento) {
        this.idMovimiento = idMovimiento;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getMontoTotal() {
        return montoTotal;
    }

    public void setMontoTotal(BigDecimal montoTotal) {
        this.montoTotal = montoTotal;
    }

    public int getIdCuenta() {
        return idCuenta;
    }

    public void setIdCuenta(int idCuenta) {
        this.idCuenta = idCuenta;
    }

    public int getIdMedioPago() {
        return idMedioPago;
    }

    public void setIdMedioPago(int idMedioPago) {
        this.idMedioPago = idMedioPago;
    }

    public int getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(int idCategoria) {
        this.idCategoria = idCategoria;
    }

    public String getNombreCuenta() {
        return nombreCuenta;
    }

    public void setNombreCuenta(String nombreCuenta) {
        this.nombreCuenta = nombreCuenta;
    }

    public String getNombreMedioPago() {
        return nombreMedioPago;
    }

    public void setNombreMedioPago(String nombreMedioPago) {
        this.nombreMedioPago = nombreMedioPago;
    }

    public String getNombreCategoria() {
        return nombreCategoria;
    }

    public void setNombreCategoria(String nombreCategoria) {
        this.nombreCategoria = nombreCategoria;
    }

    /** Parte del monto total asignada a la categoria de esta fila (null si no tiene categoria). */
    public BigDecimal getMontoAsignado() {
        return montoAsignado;
    }

    public void setMontoAsignado(BigDecimal montoAsignado) {
        this.montoAsignado = montoAsignado;
    }

    /** Cantidad de categorias en que esta repartido el movimiento (0 si no tiene ninguna). */
    public int getCantidadCategorias() {
        return cantidadCategorias;
    }

    public void setCantidadCategorias(int cantidadCategorias) {
        this.cantidadCategorias = cantidadCategorias;
    }

    /** Indica si el movimiento esta repartido en dos o mas categorias. */
    public boolean isDividido() {
        return cantidadCategorias > 1;
    }

    @Override
    public String toString() {
        return String.format("[%d] %s | %s | $%s | cuenta: %s | medio de pago: %s | categoria: %s",
                idMovimiento, fecha, descripcion, montoTotal,
                nombreCuenta, nombreMedioPago,
                nombreCategoria == null ? "(sin categoria)" : nombreCategoria);
    }
}
