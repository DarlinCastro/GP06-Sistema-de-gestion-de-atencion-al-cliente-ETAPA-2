<%-- 
    Document   : MenuCliente
    Created on : 12 nov 2025, 00:30:45
    Author     : DELL
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Menú Cliente</title>
</head>
<body>
    
    <% 
        if (session.getAttribute("usuarioActual") == null) {
            response.sendRedirect("login.jsp");
            return;
        }
    %>

    <h2>MENÚ CLIENTE</h2>
    
    <p>Bienvenido, ${sessionScope.usuarioActual.nombres} ${sessionScope.usuarioActual.apellidos}</p>
    <hr>

    <a href="crearSolicitud.jsp">Crear Solicitud</a><br><br>
    <a href="seguimientoSolicitud.jsp">Seguimiento Solicitud</a><br><br>
    
    <hr>
    <a href="Logout">Cerrar Sesión</a> 

</body>
</html>
