<%-- 
    Document   : seguimientoSolicitud
    Created on : 12 nov 2025, 01:06:07
    Author     : DELL
--%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>SEGUIMIENTO DE SOLICITUD - Web</title>
</head>
<body>
    
    <h2>SEGUIMIENTO DE SOLICITUD</h2>
    <p>Rol: **${sessionScope.usuarioActual.tipoUsuario.cargo}**</p>
    <p><a href="Menu${sessionScope.usuarioActual.tipoUsuario.cargo}.jsp">Atrás</a></p>

    <p style="color: green; font-weight: bold;">${requestScope.mensajeExito}</p>
    <p style="color: red; font-weight: bold;">${requestScope.error}</p>

    <h3>Mis Solicitudes</h3>
    <table border="1">
        <thead>
            <tr>
                <th>Ticket</th>
                <th>Fec. Creación</th>
                <th>Servicio</th>
                <th>Descripción</th>
                <th>Estado</th>
                <c:if test="${sessionScope.usuarioActual.tipoUsuario.cargo != 'Cliente'}">
                    <th>Prioridad</th>
                </c:if>
            </tr>
        </thead>
        <tbody>
            <c:forEach var="sol" items="${requestScope.listaSolicitudes}">
                <tr onclick="cargarDatosTicket('${sol.ticket.numeroTicket}', 
                                             '${sol.descripcion}', 
                                             '${sol.estadoSolicitud.estadoSolicitud}')"> 
                    <td>${sol.ticket.numeroTicket}</td>
                    <td><fmt:formatDate value="${sol.fechaCreacion}" pattern="yyyy-MM-dd"/></td>
                    <td>${sol.tipoServicio.nombreServicio}</td>
                    <td>${sol.descripcion}</td>
                    <td>${sol.estadoSolicitud.estadoSolicitud}</td>
                    <c:if test="${sessionScope.usuarioActual.tipoUsuario.cargo != 'Cliente'}">
                        <td>${sol.ticket.estadoTicket.nivelPrioridad}</td>
                    </c:if>
                </tr>
            </c:forEach>
        </tbody>
    </table>
    
    <hr>

    <fieldset>
        <legend>Detalle y Acción</legend>
        
        <form method="POST" action="Seguimiento" id="formAccion">
            <label>Nº Ticket Seleccionado:</label> 
            <input type="text" name="numeroTicket" id="txtNumeroTicket" readonly required><br>
            
            <label>Estado Actual:</label> <input type="text" id="txtEstadoActual" readonly><br>
            
            <c:choose>
                <c:when test="${sessionScope.usuarioActual.tipoUsuario.cargo == 'Cliente'}">
                    <input type="hidden" name="nuevoEstado" value="Cancelado">
                    <button type="button" onclick="confirmarAccion('CANCELAR')">Cancelar Solicitud</button>
                    
                </c:when>
                <c:otherwise>
                    <label>Nuevo Estado:</label>
                    <select name="nuevoEstado" id="cbNuevoEstado" required>
                        <option value="">-- Seleccione Nuevo Estado --</option>
                        <c:forEach var="estado" items="${requestScope.listaTodosLosEstados}">
                            <option value="${estado.estadoSolicitud}">${estado.estadoSolicitud}</option>
                        </c:forEach>
                    </select><br><br>
                    <button type="button" onclick="confirmarAccion('ACTUALIZAR')">Actualizar Solicitud</button>
                </c:otherwise>
            </c:choose>
            <input type="hidden" name="accion" id="accionInput">
        </form>
    </fieldset>

    <script>
        function cargarDatosTicket(numeroTicket, descripcion, estadoActual) {
            document.getElementById('txtNumeroTicket').value = numeroTicket;
            document.getElementById('txtEstadoActual').value = estadoActual;
            
            // Si eres Soporte, puedes pre-seleccionar el estado actual
            var cbNuevoEstado = document.getElementById('cbNuevoEstado');
            if (cbNuevoEstado) {
                cbNuevoEstado.value = estadoActual;
            }
        }
        
        function confirmarAccion(accion) {
            var ticket = document.getElementById('txtNumeroTicket').value;
            if (!ticket) {
                alert("Debe seleccionar un ticket primero.");
                return;
            }
            
            var confirmMsg = (accion === 'CANCELAR') 
                ? "¿Está seguro que desea CANCELAR el ticket #" + ticket + "?"
                : "¿Está seguro de ACTUALIZAR el estado del ticket #" + ticket + "?";
            
            if (confirm(confirmMsg)) {
                document.getElementById('accionInput').value = accion;
                document.getElementById('formAccion').submit();
            }
        }
    </script>
</body>
</html>