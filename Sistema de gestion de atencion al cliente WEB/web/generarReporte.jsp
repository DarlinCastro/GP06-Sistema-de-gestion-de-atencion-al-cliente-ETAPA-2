<%-- 
    Document   : generarReporte
    Created on : 12 nov 2025, 01:08:05
    Author     : DELL
--%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>GENERAR REPORTES - Web</title>
</head>
<body>
    
    <h2>GENERAR REPORTES</h2>
    <p><a href="MenuAdmin.jsp">Atrás</a></p>

    <p style="color: red; font-weight: bold;">${requestScope.error}</p>

    <form method="GET" action="Reporte">
        <label>Tipo de Servicio:</label>
        <select name="filtroServicio">
            <option value="Todos los Servicios">Todos los Servicios</option>
            <c:forEach var="servicio" items="${requestScope.listaServicios}">
                <option value="${servicio.nombreServicio}">${servicio.nombreServicio}</option>
            </c:forEach>
        </select>
        <button type="submit">Generar Reporte General</button>
        </form>
    
    <hr>

    <h3>${requestScope.reporteTitulo} (Total: ${fn:length(requestScope.listaReporte)})</h3>
    
    <table border="1">
        <thead>
            <tr>
                <th>Ticket</th>
                <th>Fec. Creación</th>
                <th>Estado</th>
                <th>Tipo Servicio</th>
                <th>Cliente</th>
                <th>Fec. Asignación</th>
                <th>Encargado</th>
                <th>Cargo</th>
            </tr>
        </thead>
        <tbody>
            <c:forEach var="reporte" items="${requestScope.listaReporte}">
                <tr>
                    <td>${reporte.numeroTicket}</td>
                    <td><fmt:formatDate value="${reporte.fechaCreacion}" pattern="dd/MM/yyyy"/></td>
                    <td>${reporte.estadoTicket}</td>
                    <td>${reporte.tipoServicio}</td>
                    <td>${reporte.nombreCliente}</td>
                    <td><fmt:formatDate value="${reporte.fechaAsignacion}" pattern="dd/MM/yyyy"/></td>
                    <td>${reporte.nombreEncargadoSoporte}</td>
                    <td>${reporte.cargoEncargado}</td>
                </tr>
            </c:forEach>
        </tbody>
    </table>
</body>
</html>