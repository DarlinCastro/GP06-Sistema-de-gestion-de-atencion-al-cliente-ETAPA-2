<%-- 
    Document   : GenerarReporte
    Created on : 12 nov 2025, 01:08:05
    Author     : DELL
--%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>Generar Reporte de Tickets</title>
        <style>
            body {
                font-family: Arial, sans-serif;
                margin: 20px;
            }
            h2 {
                text-align: center;
                color: #333;
            }
            .container {
                width: 95%;
                margin: 0 auto;
            }
            .filtro-form {
                margin-bottom: 20px;
                display: flex;
                align-items: center;
                gap: 10px;
            }
            table {
                width: 100%;
                border-collapse: collapse;
                margin-top: 20px;
            }
            th, td {
                border: 1px solid #ddd;
                padding: 8px;
                text-align: left;
                font-size: 14px;
            }
            th {
                background-color: #f2f2f2;
                font-weight: bold;
            }
            .mensaje-vacio {
                text-align: center;
                color: #888;
                margin-top: 30px;
                padding: 10px;
                background-color: #ffe0e0;
                border: 1px solid #ff9999;
            }
        </style>
    </head>
    <body>
        <div class="container">

            <div style="text-align: right; margin-bottom: 15px;">
                <a href="GenerarReporteController?accion=atras&origen=${requestScope.origen}">
                    <button>Atrás</button>
                </a>
            </div>

            <h2>REPORTE GENERAL DE TICKETS DE SOPORTE</h2>

            <form action="GenerarReporteController" method="GET" class="filtro-form"> 

                <input type="hidden" name="origen" value="${requestScope.origen}">

                <label for="filtroServicio">Tipo de Servicio:</label>
                <select name="filtroServicio" id="filtroServicio" onchange="this.form.submit()">
                    <option value="todos" <c:if test="${filtroSeleccionado == 'todos'}">selected</c:if>>-- Todos los Servicios --</option>

                    <c:forEach var="servicio" items="${tiposServicio}">
                        <option value="${servicio.nombre}" <c:if test="${filtroSeleccionado == servicio.nombre}">selected</c:if>>
                            <c:out value="${servicio.nombre}" />
                        </option>
                    </c:forEach>
                </select>
            </form>

            <hr>

            <c:choose>
                <c:when test="${not empty listaReportes}">
                    <table>
                        <thead>
                            <tr>
                                <th>N° Ticket</th>
                                <th>Fecha Creación</th>
                                <th>Estado Ticket</th>
                                <th>Tipo Servicio</th>
                                <th>Descripción Solicitud</th>
                                <th>Nombre Cliente</th>
                                <th>Fecha Asignación</th>
                                <th>Encargado Soporte</th>
                                <th>Cargo</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="data" items="${listaReportes}">
                                <tr>
                                    <td><c:out value="${data.numeroTicket}" /></td>
                                    <td><fmt:formatDate value="${data.fechaCreacion}" pattern="dd/MM/yyyy"/></td> 
                                    <td><c:out value="${data.estadoTicket}" /></td>
                                    <td><c:out value="${data.tipoServicio}" /></td>
                                    <td><c:out value="${data.descripcionServicio}" /></td>
                                    <td><c:out value="${data.nombreCliente}" /></td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${not empty data.fechaAsignacion}">
                                                <fmt:formatDate value="${data.fechaAsignacion}" pattern="dd/MM/yyyy"/>
                                            </c:when>
                                            <c:otherwise>N/A</c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td><c:out value="${data.nombreEncargadoSoporte}" /></td>
                                    <td><c:out value="${data.cargoEncargado}" /></td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </c:when>
                <c:otherwise>
                    <div class="mensaje-vacio">
                        <p>️ No se encontraron tickets para el filtro seleccionado.</p>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
        <div style="text-align: center; margin-top: 30px; font-size: small; color: #666;">
            © 2025 KIA. Todos los derechos reservados
        </div>
    </body>
</html>