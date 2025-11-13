<%-- 
    Document   : asignarSolicitud
    Created on : 12 nov 2025, 00:37:10
    Author     : DELL
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>ASIGNAR SOLICITUDES - Web</title>
</head>
<body>
    
    <h2>ASIGNAR SOLICITUDES</h2>
    
    <p style="color: green; font-weight: bold;">${requestScope.mensajeExito}</p>
    <p style="color: red; font-weight: bold;">${requestScope.error}</p>

    <form method="POST" action="Asignacion">
        <fieldset>
            <legend>Datos de Solicitud y Asignación</legend>
            
            <label>Nº Ticket:</label> <input type="text" name="numeroTicket" id="txtNumeroTicket" readonly><br>
            
            <label>Fecha Creación:</label> <input type="text" id="txtFechaCreacion" readonly><br>
            <label>Descripción:</label> <textarea id="txtDescripcion" readonly></textarea><br>

            <label>Fecha Asignación:</label> <input type="text" name="fechaAsignacion" value="<fmt:formatDate value="<%= new java.util.Date() %>" pattern="yyyy-MM-dd"/>"><br>
            
            <label>Nivel de Prioridad:</label>
            <select name="nivelPrioridad">
                <option value="">-- Seleccione Prioridad --</option>
                <c:forEach var="prioridad" items="${listaPrioridades}">
                    <option value="${prioridad}">${prioridad}</option>
                </c:forEach>
            </select><br>

            <label>Cargo:</label>
            <select name="cargo" id="cbCargo" onchange="cargarTecnicos()">
                <option value="">-- Seleccione Cargo --</option>
                <c:forEach var="cargo" items="${listaCargos}">
                    <option value="${cargo}">${cargo}</option>
                </c:forEach>
            </select><br>

            <label>Nombre Técnico:</label>
            <select name="tecnicoNombre" id="cbNombre">
                <option value="">-- Seleccione Técnico --</option>
                </select><br>
            
            <label>Estado Solicitud:</label>
            <select name="estadoSolicitud">
                <option value="">-- Seleccione Estado --</option>
                <c:forEach var="estado" items="${listaEstados}">
                    <option value="${estado}">${estado}</option>
                </c:forEach>
            </select><br>

            <button type="submit">Asignar</button>
        </fieldset>
    </form>
    
    <hr>

    <h3>Tabla de Solicitudes</h3>
    <table border="1" id="tablaSolicitudes">
        <thead>
            <tr>
                <th>Ticket</th>
                <th>Fec. Creación</th>
                <th>Servicio</th>
                <th>Descripción</th>
                <th>Estado Sol.</th>
                <th>Técnico Asignado</th>
                <th>Prioridad</th>
            </tr>
        </thead>
        <tbody>
            <c:forEach var="sol" items="${listaSolicitudes}">
                <tr onclick="cargarTicketDatos('${sol.ticket.numeroTicket}', 
                                             '<fmt:formatDate value="${sol.fechaCreacion}" pattern="yyyy-MM-dd"/>', 
                                             '${sol.descripcion}', 
                                             '${sol.estadoSolicitud.estadoSolicitud}', 
                                             '${sol.ticket.estadoTicket.nivelPrioridad}')"> 
                    <td>${sol.ticket.numeroTicket}</td>
                    <td><fmt:formatDate value="${sol.fechaCreacion}" pattern="yyyy-MM-dd"/></td>
                    <td>${sol.tipoServicio.nombreServicio}</td>
                    <td>${sol.descripcion}</td>
                    <td>${sol.estadoSolicitud.estadoSolicitud}</td>
                    <td>
                        <c:choose>
                            <c:when test="${sol.ticket.tecnicoAsignado != null}">
                                ${sol.ticket.tecnicoAsignado.nombres} ${sol.ticket.tecnicoAsignado.apellidos}
                            </c:when>
                            <c:otherwise>
                                PENDIENTE
                            </c:otherwise>
                        </c:choose>
                    </td>
                    <td>${sol.ticket.estadoTicket.nivelPrioridad}</td>
                </tr>
            </c:forEach>
        </tbody>
    </table>
    
    <a href="MenuAdmin.jsp">Atrás</a>

    <script>
        // Función para cargar los datos del ticket seleccionado en la tabla a los campos del formulario
        function cargarTicketDatos(numeroTicket, fechaCreacion, descripcion, estadoSol, prioridad) {
            document.getElementById('txtNumeroTicket').value = numeroTicket;
            document.getElementById('txtFechaCreacion').value = fechaCreacion;
            document.getElementById('txtDescripcion').value = descripcion;
            
            // Seleccionar el estado y la prioridad en los combos
            document.querySelector('select[name="estadoSolicitud"]').value = estadoSol;
            document.querySelector('select[name="nivelPrioridad"]').value = prioridad;

            // Nota: Aquí faltaría la lógica AJAX para cargar el técnico asignado si ya lo tuviera.
        }

        // Función AJAX para cargar técnicos al cambiar el cargo (SIMULADO)
        function cargarTecnicos() {
            var cargo = document.getElementById('cbCargo').value;
            var cbNombre = document.getElementById('cbNombre');
            
            // Limpiar opciones anteriores
            cbNombre.innerHTML = '<option value="">-- Seleccione Técnico --</option>';

            if (cargo) {
                // *** AQUÍ DEBERÍA IR UNA LLAMADA AJAX A UN SEGUNDO SERVLET (Ej: /TecnicosPorCargo) ***
                // Por ahora, simularemos la carga de técnicos estáticamente o tendrás que implementarla.

                // Simulando carga si el cargo es 'Técnico'
                if (cargo === 'Técnico') {
                    cbNombre.innerHTML += '<option value="Ana Martinez">Ana Martinez</option>';
                    cbNombre.innerHTML += '<option value="Bernardo Nuñez">Bernardo Nuñez</option>';
                } else if (cargo === 'Programador') {
                    cbNombre.innerHTML += '<option value="Erick Vigil">Erick Vigil</option>';
                }
            }
        }
    </script>
</body>
</html>
