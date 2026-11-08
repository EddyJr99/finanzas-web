<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="co.edu.sena.finanzas.modelo.Movimiento, co.edu.sena.finanzas.servlet.Html" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>SGFP - Buscar movimiento</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilo.css">
</head>
<body>
<%@ include file="/WEB-INF/jspf/encabezado.jspf" %>
<main>
    <div class="tarjeta">
        <h2>Buscar movimiento por id</h2>
        <%-- Formulario con metodo GET: el id viaja en la URL (?accion=buscar&id=...). --%>
        <form method="get" action="${pageContext.request.contextPath}/movimientos">
            <input type="hidden" name="accion" value="buscar">
            <label for="id">Id del movimiento</label>
            <input type="number" id="id" name="id" min="1" required
                   value="<%= Html.escapar((String) request.getAttribute("textoBuscado")) %>">
            <p><button type="submit" class="boton">Buscar</button></p>
        </form>
    </div>

    <jsp:include page="/WEB-INF/jspf/mensaje.jsp" />

    <%
        Movimiento movimiento = (Movimiento) request.getAttribute("movimiento");
        if (movimiento != null) {
    %>
    <div class="tarjeta">
        <h2>Resultado</h2>
        <table>
            <tr><th>Id</th><td><%= movimiento.getIdMovimiento() %></td></tr>
            <tr><th>Fecha</th><td><%= movimiento.getFecha() %></td></tr>
            <tr><th>Descripcion</th><td><%= Html.escapar(movimiento.getDescripcion()) %></td></tr>
            <tr><th>Monto</th><td>$ <%= movimiento.getMontoTotal() %></td></tr>
            <tr><th>Cuenta</th><td><%= Html.escapar(movimiento.getNombreCuenta()) %></td></tr>
            <tr><th>Medio de pago</th><td><%= Html.escapar(movimiento.getNombreMedioPago()) %></td></tr>
            <tr><th>Categoria</th><td><%= (movimiento.getNombreCategoria() == null ? "(sin categoria)" : Html.escapar(movimiento.getNombreCategoria())) %></td></tr>
        </table>
        <p><a class="boton secundario"
              href="${pageContext.request.contextPath}/movimientos?accion=editar&amp;id=<%= movimiento.getIdMovimiento() %>">Editar este movimiento</a></p>
    </div>
    <% } %>
</main>
<%@ include file="/WEB-INF/jspf/pie.jspf" %>
</body>
</html>
