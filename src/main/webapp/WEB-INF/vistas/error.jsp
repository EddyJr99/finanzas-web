<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<%@ page import="co.edu.sena.finanzas.servlet.Html" %>
<%
    // El objeto implicito "exception" solo existe en paginas con isErrorPage="true".
    // Se recorre la cadena de causas: si alguna es un error de base de datos se muestra primero.
    Throwable causa = exception;
    Throwable errorBD = null;
    for (Throwable actual = exception; actual != null; actual = actual.getCause()) {
        if (actual instanceof java.sql.SQLException && errorBD == null) {
            errorBD = actual;
        }
        causa = actual;
    }
    Object codigo = request.getAttribute("javax.servlet.error.status_code");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>SGFP - Error</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilo.css">
</head>
<body>
<%@ include file="/WEB-INF/jspf/encabezado.jspf" %>
<main>
    <div class="tarjeta">
        <h2>No se pudo completar la operacion</h2>
        <% if (errorBD != null) { %>
            <div class="aviso error">
                Error de base de datos: <%= Html.escapar(errorBD.getMessage()) %>
            </div>
            <p>Revise que MySQL este iniciado (Laragon), que exista la base <strong>sgfp</strong>
               y que los datos de <code>conexion.properties</code> sean correctos.</p>
        <% } else if (causa != null) { %>
            <div class="aviso error">Ocurrio un error inesperado: <%= Html.escapar(causa.toString()) %></div>
        <% } else { %>
            <div class="aviso error">La pagina solicitada no existe o la solicitud no es valida
                (codigo <%= codigo %>).</div>
        <% } %>
        <p><a class="boton" href="${pageContext.request.contextPath}/">Volver al inicio</a></p>
    </div>
</main>
<%@ include file="/WEB-INF/jspf/pie.jspf" %>
</body>
</html>
