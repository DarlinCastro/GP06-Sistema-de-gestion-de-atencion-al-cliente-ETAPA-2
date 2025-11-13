<%-- 
    Document   : MenuTecnico
    Created on : 12 nov 2025, 00:31:23
    Author     : DELL
--%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Menú Técnico y Programador</title>
</head>
<body>
    
    <% 
        if (session.getAttribute("usuarioActual") == null) {
            response.sendRedirect("login.jsp");
            return;
        }
    %>

    <h2>MENÚ TÉCNICO Y PROGRAMADOR</h2>
    
    <p>Bienvenido, ${sessionScope.usuarioActual.nombres} ${sessionScope.usuarioActual.apellidos}</p>
    <hr>

    <a href="generarReporte.jsp">Generar Reportes</a><br><br>
    <a href="seguimientoSolicitud.jsp">Seguimiento Solicitud</a><br><br>
    
    <hr>
    <a href="Logout">Cerrar Sesión</a> 

</body>
</html>
