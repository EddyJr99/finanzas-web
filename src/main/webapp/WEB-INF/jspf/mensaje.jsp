<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="co.edu.sena.finanzas.servlet.Html" %>
<%-- Se incluye con la accion <jsp:include>: se ejecuta en cada solicitud y muestra avisos. --%>
<%
    Object mensaje = request.getAttribute("mensaje");
    Object error = request.getAttribute("error");
    if (mensaje != null) {
%>
    <div class="aviso exito"><%= Html.escapar(mensaje.toString()) %></div>
<%
    }
    if (error != null) {
%>
    <div class="aviso error"><%= Html.escapar(error.toString()) %></div>
<%
    }
%>
