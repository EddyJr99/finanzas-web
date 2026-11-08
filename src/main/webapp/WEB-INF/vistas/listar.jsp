<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, co.edu.sena.finanzas.modelo.Movimiento, co.edu.sena.finanzas.servlet.Html" %>
<%!
    // Declaracion JSP: da formato de pesos colombianos a un monto.
    private String formatoMonto(java.math.BigDecimal monto) {
        return monto == null ? "" : String.format("$ %,.2f", monto);
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>SGFP - Movimientos</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilo.css">
</head>
<body>
<%@ include file="/WEB-INF/jspf/encabezado.jspf" %>
<main>
    <jsp:include page="/WEB-INF/jspf/mensaje.jsp" />
    <div class="tarjeta">
        <h2>Movimientos registrados</h2>
        <%
            // Scriptlet: la lista la dejo preparada el servlet con request.setAttribute.
            @SuppressWarnings("unchecked")
            List<Movimiento> movimientos = (List<Movimiento>) request.getAttribute("movimientos");
            if (movimientos == null || movimientos.isEmpty()) {
        %>
            <p class="vacio">Todavia no hay movimientos registrados.</p>
        <%
            } else {
        %>
        <table>
            <thead>
                <tr>
                    <th>Id</th><th>Fecha</th><th>Descripcion</th><th>Monto</th>
                    <th>Cuenta</th><th>Medio de pago</th><th>Categoria</th><th>Acciones</th>
                </tr>
            </thead>
            <tbody>
            <% for (Movimiento movimiento : movimientos) { %>
                <tr>
                    <td><%= movimiento.getIdMovimiento() %></td>
                    <td><%= movimiento.getFecha() %></td>
                    <td><%= Html.escapar(movimiento.getDescripcion()) %></td>
                    <td class="monto"><%= formatoMonto(movimiento.getMontoAsignado() != null ? movimiento.getMontoAsignado() : movimiento.getMontoTotal()) %></td>
                    <td><%= Html.escapar(movimiento.getNombreCuenta()) %></td>
                    <td><%= Html.escapar(movimiento.getNombreMedioPago()) %></td>
                    <td><%= (movimiento.getNombreCategoria() == null ? "(sin categoria)" : Html.escapar(movimiento.getNombreCategoria())) %></td>
                    <td>
                        <div class="acciones">
                            <a class="boton secundario"
                               href="${pageContext.request.contextPath}/movimientos?accion=editar&amp;id=<%= movimiento.getIdMovimiento() %>">Editar</a>
                            <form method="post" action="${pageContext.request.contextPath}/movimientos"
                                  onsubmit="return confirm('<%= movimiento.isDividido() ? "Quitar la categoria de esta fila del movimiento " : "Eliminar el movimiento " %><%= movimiento.getIdMovimiento() %>?');">
                                <input type="hidden" name="accion" value="eliminar">
                                <input type="hidden" name="id" value="<%= movimiento.getIdMovimiento() %>">
                                <input type="hidden" name="idCategoria" value="<%= movimiento.getIdCategoria() %>">
                                <button type="submit" class="boton peligro">Eliminar</button>
                            </form>
                        </div>
                    </td>
                </tr>
            <% } %>
            </tbody>
        </table>
        <p>Total: <strong>${movimientos.size()}</strong> fila(s). Un movimiento repartido en varias
           categorias aparece en una fila por categoria, con el monto de cada una.</p>
        <% } %>
        <p><a class="boton" href="${pageContext.request.contextPath}/movimientos?accion=nuevo">Registrar movimiento</a></p>
    </div>
</main>
<%@ include file="/WEB-INF/jspf/pie.jspf" %>
</body>
</html>
