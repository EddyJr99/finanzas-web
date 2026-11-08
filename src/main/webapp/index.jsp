<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.time.LocalDate, java.time.LocalDateTime, java.time.format.DateTimeFormatter" %>
<%!
    // Declaracion JSP: este metodo pasa a ser parte de la clase generada para la pagina.
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private String ahora() {
        return LocalDateTime.now().format(FORMATO);
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>SGFP - Inicio</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilo.css">
</head>
<body>
<%@ include file="/WEB-INF/jspf/encabezado.jspf" %>
<main>
    <div class="tarjeta">
        <h2>Modulo Movimientos</h2>
        <p>Aplicacion web con servlets y JSP conectada a la base de datos MySQL <strong>sgfp</strong>.
           Fecha y hora del servidor: <strong><%= ahora() %></strong></p>
        <div class="grid-menu">
            <a href="${pageContext.request.contextPath}/movimientos">Listar movimientos
                <small>Consulta (GET). Desde aqui se edita y elimina.</small></a>
            <a href="${pageContext.request.contextPath}/movimientos?accion=nuevo">Registrar movimiento
                <small>Formulario de insercion (POST).</small></a>
            <a href="${pageContext.request.contextPath}/movimientos?accion=buscar">Buscar por id
                <small>Formulario de busqueda (GET).</small></a>
        </div>
    </div>
</main>
<%@ include file="/WEB-INF/jspf/pie.jspf" %>
</body>
</html>
