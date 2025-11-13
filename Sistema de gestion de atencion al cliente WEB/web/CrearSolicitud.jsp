<%-- 
    Document   : CrearSolicitud
    Created on : 12 nov 2025, 00:53:11
    Author     : DELL
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>CREAR SOLICITUD - Web</title>
</head>
<body>
    
    <h2>CREAR SOLICITUD</h2>
    <p><a href="MenuCliente.jsp">Atrás</a></p>

    <p style="color: red; font-weight: bold;">${requestScope.error}</p>

    <form method="POST" action="CrearSolicitud">
        <fieldset>
            <legend>Detalles de la Solicitud</legend>
            
            <label>Tipo de Servicio:</label>
            <select name="tipoServicio" required>
                <option value="">-- Seleccione Tipo de Servicio --</option>
                <c:forEach var="servicio" items="${requestScope.listaServicios}">
                    <option value="${servicio.nombreServicio}">${servicio.nombreServicio}</option>
                </c:forEach>
            </select><br><br>

            <label>Descripción:</label><br>
            <textarea name="descripcion" rows="5" cols="40" required></textarea><br><br>
            
            <button type="submit">Crear Solicitud</button>
        </fieldset>
    </form>
</body>
</html>
