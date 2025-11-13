<%-- 
    Document   : MenuAdmin
    Created on : 12 nov 2025, 00:29:53
    Author     : DELL
--%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Menú Administrador</title>
</head>
<body>
    
    <% 
        if (session.getAttribute("usuarioActual") == null) {
            response.sendRedirect("login.jsp");
            return;
        }
    %>

    <h2>MENÚ ADMINISTRADOR</h2>
    
    <p>Bienvenido, ${sessionScope.usuarioActual.nombres} ${sessionScope.usuarioActual.apellidos}</p>
    <hr>
    
    <p style="color: green; font-weight: bold;">${requestScope.mensajeExito}</p>
    <p style="color: red; font-weight: bold;">${requestScope.error}</p>

    <a href="Asignacion">Asignar Solicitudes</a><br><br> 
    <a href="gestionarUsuarios.jsp">Gestionar Usuarios</a><br><br>
    <a href="generarReporte.jsp">Generar Reportes</a><br><br>
    
    <hr>
    <a href="Logout">Cerrar Sesión</a> 

</body>
</html>
