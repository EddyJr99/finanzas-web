<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.time.LocalDate, java.util.List" %>
<%@ page import="co.edu.sena.finanzas.modelo.Movimiento, co.edu.sena.finanzas.servlet.*" %>
<%!
    // Declaracion JSP: genera las etiquetas <option> de una lista desplegable.
    private String opciones(List<OpcionCatalogo> lista, String seleccionado) {
        StringBuilder html = new StringBuilder();
        for (OpcionCatalogo opcion : lista) {
            boolean marcada = String.valueOf(opcion.getId()).equals(seleccionado);
            html.append("<option value=\"").append(opcion.getId()).append('"')
                .append(marcada ? " selected" : "").append('>')
                .append(Html.escapar(opcion.getNombre())).append("</option>");
        }
        return html.toString();
    }
%>
<%
    @SuppressWarnings("unchecked")
    List<OpcionCatalogo> cuentas = (List<OpcionCatalogo>) request.getAttribute("cuentas");
    @SuppressWarnings("unchecked")
    List<OpcionCatalogo> mediosPago = (List<OpcionCatalogo>) request.getAttribute("mediosPago");
    @SuppressWarnings("unchecked")
    List<OpcionCatalogo> categorias = (List<OpcionCatalogo>) request.getAttribute("categorias");

    String accionFormulario = (String) request.getAttribute("accionFormulario");
    boolean esEdicion = "actualizar".equals(accionFormulario);

    // Valores a mostrar: lo que el usuario escribio (si hubo errores), el movimiento
    // que se edita, o valores vacios para un registro nuevo.
    FormularioMovimiento enviado = (FormularioMovimiento) request.getAttribute("formulario");
    Movimiento existente = (Movimiento) request.getAttribute("movimiento");
    Object idMovimiento = esEdicion
            ? (existente != null ? (Object) existente.getIdMovimiento() : request.getAttribute("idMovimiento"))
            : null;

    // Un movimiento repartido en varias categorias no permite cambiar monto ni categoria aqui.
    boolean dividido = esEdicion && existente != null && existente.isDividido();

    String fecha = LocalDate.now().toString();
    String descripcion = "";
    String monto = "";
    String idCuenta = "";
    String idMedioPago = "";
    String idCategoria = "";
    if (enviado != null) {
        fecha = enviado.getFecha();
        descripcion = enviado.getDescripcion();
        monto = enviado.getMontoTotal();
        idCuenta = enviado.getIdCuenta();
        idMedioPago = enviado.getIdMedioPago();
        idCategoria = enviado.getIdCategoria();
    } else if (existente != null) {
        fecha = existente.getFecha().toString();
        descripcion = existente.getDescripcion();
        monto = existente.getMontoTotal().toPlainString();
        idCuenta = String.valueOf(existente.getIdCuenta());
        idMedioPago = String.valueOf(existente.getIdMedioPago());
        idCategoria = String.valueOf(existente.getIdCategoria());
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>SGFP - <%= esEdicion ? "Editar" : "Registrar" %> movimiento</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilo.css">
</head>
<body>
<%@ include file="/WEB-INF/jspf/encabezado.jspf" %>
<main>
    <div class="tarjeta">
        <h2><%= esEdicion ? "Editar movimiento " + idMovimiento : "Registrar movimiento" %></h2>

        <% if (dividido) { %>
            <div class="aviso error">
                Este movimiento esta repartido en <%= existente.getCantidadCategorias() %> categorias
                (<%= Html.escapar(existente.getNombreCategoria()) %>). Para no perder ese reparto
                solo se pueden cambiar la fecha, la descripcion, la cuenta y el medio de pago.
            </div>
        <% } %>

        <% if (enviado != null && !enviado.getErrores().isEmpty()) { %>
            <div class="aviso error">
                Corrija los siguientes datos:
                <ul>
                <% for (String error : enviado.getErrores()) { %>
                    <li><%= Html.escapar(error) %></li>
                <% } %>
                </ul>
            </div>
        <% } %>

        <%-- Formulario con metodo POST: los datos viajan en el cuerpo de la solicitud. --%>
        <form method="post" action="${pageContext.request.contextPath}/movimientos">
            <input type="hidden" name="accion" value="<%= accionFormulario %>">
            <% if (esEdicion) { %>
                <input type="hidden" name="idMovimiento" value="<%= idMovimiento %>">
            <% } %>

            <label for="fecha">Fecha</label>
            <input type="date" id="fecha" name="fecha" required max="<%= LocalDate.now() %>"
                   value="<%= Html.escapar(fecha) %>">

            <label for="descripcion">Descripcion (maximo 150 caracteres)</label>
            <input type="text" id="descripcion" name="descripcion" required maxlength="150"
                   value="<%= Html.escapar(descripcion) %>">

            <label for="montoTotal">Monto total (mayor que 0, hasta 2 decimales)</label>
            <input type="number" id="montoTotal" name="montoTotal" required min="0.01" step="0.01"
                   value="<%= Html.escapar(monto) %>" <%= dividido ? "readonly" : "" %>>

            <label for="idCuenta">Cuenta</label>
            <select id="idCuenta" name="idCuenta" required>
                <option value="">-- Seleccione --</option>
                <%= opciones(cuentas, idCuenta) %>
            </select>

            <label for="idMedioPago">Medio de pago</label>
            <select id="idMedioPago" name="idMedioPago" required>
                <option value="">-- Seleccione --</option>
                <%= opciones(mediosPago, idMedioPago) %>
            </select>

            <label for="idCategoria">Categoria</label>
            <% if (dividido) { %>
                <%-- Un select deshabilitado no se envia: se manda el valor actual en un campo oculto. --%>
                <input type="hidden" name="idCategoria" value="<%= Html.escapar(idCategoria) %>">
                <select id="idCategoria" disabled>
                    <option><%= Html.escapar(existente.getNombreCategoria()) %></option>
                </select>
            <% } else { %>
            <select id="idCategoria" name="idCategoria" required>
                <option value="">-- Seleccione --</option>
                <%= opciones(categorias, idCategoria) %>
            </select>
            <% } %>

            <p class="acciones">
                <button type="submit" class="boton"><%= esEdicion ? "Guardar cambios" : "Registrar" %></button>
                <a class="boton secundario" href="${pageContext.request.contextPath}/movimientos">Cancelar</a>
            </p>
        </form>
    </div>
</main>
<%@ include file="/WEB-INF/jspf/pie.jspf" %>
</body>
</html>
