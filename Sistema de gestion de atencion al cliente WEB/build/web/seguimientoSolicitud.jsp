<%--
    Document   : seguimientoSolicitud
    Created on : 12 nov 2025, 01:06:07
    Author     : Gemini (Modificado para lógica de roles)
--%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>SEGUIMIENTO Y ACTUALIZACIÓN DE SOLICITUDES</title>
    </head>
    <body style="font-family: Arial, sans-serif; margin: 0; padding: 0; background-color: #f4f4f4;">

        <%-- Definición de Variables de Control --%>
        <c:set var="esCliente" value="${sessionScope.usuarioActual.tipoUsuario.cargo.trim() == 'Cliente'}" />
        <c:set var="esTecnicoOProgramador" value="${!esCliente}" />

        <%-- BARRA SUPERIOR MORADA (HEADER) --%>
        <div style="background-color: #5B2C6F; color: white; padding: 10px 20px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
            <h2 style="margin: 0; display: inline-block;">SEGUIMIENTO DE SOLICITUD</h2>

            <%-- Lógica para establecer el URL de Atrás (De la respuesta anterior) --%>
            <c:set var="urlDestino" value="${requestScope.urlAtras}" scope="page"/>
            <c:if test="${empty urlDestino}">
                <c:set var="urlDestino" value="Menu${sessionScope.usuarioActual.tipoUsuario.cargo.trim()}.jsp" scope="page"/>
            </c:if>

            <a href="${urlDestino}"
               style="float: right; color: white; text-decoration: none; padding: 5px 10px; background-color: #3498DB; border-radius: 3px;">Atrás</a>
            <div style="clear: both;"></div>
        </div>

        <%-- CONTENEDOR PRINCIPAL --%>
        <div style="width: 500px; margin: 20px auto; padding: 20px; background-color: white; border: 1px solid #ccc; box-shadow: 0 0 10px rgba(0,0,0,0.1);">

            <%-- Mensajes de Éxito o Error --%>
            <p style="color: green; font-weight: bold;">${sessionScope.mensajeExito}</p>
            <p style="color: red; font-weight: bold;">${sessionScope.error}</p>

            <c:remove var="mensajeExito" scope="session"/>
            <c:remove var="error" scope="session"/>

            <%-- FORMULARIO DE DETALLE Y ACCIÓN --%>
            <form method="POST" action="Seguimiento" id="formAccion">
                <input type="hidden" name="accion" id="accionInput">

                <%-- Estilo común para Labels y Inputs --%>
                <style>
                    .field-row {
                        margin-bottom: 8px;
                    }
                    .field-row label {
                        display: inline-block;
                        width: 150px;
                        font-weight: bold;
                        vertical-align: top;
                        line-height: 25px;
                    }
                    .field-row input[type="text"],
                    .field-row select,
                    .field-row textarea {
                        padding: 5px;
                        width: 250px;
                        border: 1px solid #ccc;
                    }
                    .readonly-field {
                        background-color: #eee;
                    }
                </style>

                <script>
                    var solicitudesData = [];
                    <c:forEach var="sol" items="${requestScope.listaSolicitudes}">
                    solicitudesData.push({
                        numeroTicket: '${sol.ticket.numeroTicket.trim()}',
                        fechaCreacion: '<fmt:formatDate value="${sol.fechaCreacion}" pattern="dd/MM/yyyy"/>',
                        tipoServicio: '${sol.tipoServicio.nombreServicio.trim()}',
                        descripcion: '${sol.descripcion.trim()}',
                        estadoActual: '${sol.estadoSolicitud.estadoSolicitud.trim()}',
                        prioridad: '${sol.ticket.estadoTicket.nivelPrioridad.trim()}'
                    });
                    </c:forEach>
                </script>

                <%-- Campos de Usuario (Solo Lectura) --%>
                <div class="field-row">
                    <label>Cargo</label>
                    <input type="text" value="${sessionScope.usuarioActual.tipoUsuario.cargo.trim()}" readonly class="readonly-field">
                </div>
                <div class="field-row">
                    <label>Nombre</label>
                    <input type="text" value="${sessionScope.usuarioActual.nombres.trim()} ${sessionScope.usuarioActual.apellidos.trim()}" readonly class="readonly-field">
                </div>

                <hr style="border-top: 1px solid #ddd; margin: 15px 0;">

                <%-- Campo Clave: N° Ticket (Dropdown) --%>
                <div class="field-row">
                    <label>N° ticket</label>
                    <select name="numeroTicket" id="txtNumeroTicketDropdown" required onchange="cargarDatosTicketSeleccionado()">
                        <option value="">-- Seleccione un Ticket --</option>
                        <c:forEach var="sol" items="${requestScope.listaSolicitudes}">
                            <option value="${sol.ticket.numeroTicket.trim()}">${sol.ticket.numeroTicket.trim()}</option>
                        </c:forEach>
                    </select>
                    <input type="hidden" name="numeroTicketHidden" id="txtTicketSeleccionado">
                </div>

                <%-- Campos de Detalle (Se llenan al seleccionar el ticket) --%>
                <div class="field-row">
                    <label>Fecha Creacion</label>
                    <input type="text" id="txtFechaCreacion" readonly class="readonly-field">
                </div>
                <div class="field-row">
                    <label>Tipo Servicio</label>
                    <input type="text" id="txtTipoServicio" readonly class="readonly-field">
                </div>
                <div class="field-row">
                    <label>Descripcion</label>
                    <textarea id="txtDescripcion" rows="3" readonly class="readonly-field"></textarea>
                </div>

                <%-- NIVEL DE PRIORIDAD (VISIBLE SOLO PARA TÉCNICO/PROGRAMADOR) --%>
                <c:if test="${esTecnicoOProgramador}">
                    <div class="field-row">
                        <label>Nivel de prioridad</label>
                        <input type="text" id="txtNivelPrioridad" readonly class="readonly-field">
                    </div>
                </c:if>

                <%-- CAMPO DE ESTADO (EDITABILIDAD CONDICIONAL) --%>
                <div class="field-row">
                    <label>Estado de Solicitud</label>
                    <c:choose>
                        <c:when test="${esCliente}">
                            <%-- Cliente: Solo ve el estado actual (readonly) --%>
                            <input type="text" id="txtEstadoActual" name="nuevoEstado" readonly class="readonly-field">
                        </c:when>
                        <c:otherwise>
                            <%-- Técnico/Programador: Puede cambiar el estado (select) --%>
                            <select name="nuevoEstado" id="cbNuevoEstado" required>
                                <option value="">-- Seleccione Nuevo Estado --</option>
                                <c:forEach var="estado" items="${requestScope.listaTodosLosEstados}">
                                    <option value="${estado.estadoSolicitud}">${estado.estadoSolicitud}</option>
                                </c:forEach>
                            </select>
                            <input type="hidden" id="txtEstadoActual">
                        </c:otherwise>
                    </c:choose>
                </div>

                <%-- BOTONES DE ACCIÓN --%>
                <div style="text-align: center; margin-top: 30px;">

                    <%-- BOTÓN ACTUALIZAR (VISIBLE SOLO PARA TÉCNICO/PROGRAMADOR) --%>
                    <c:if test="${esTecnicoOProgramador}">
                        <button type="button" onclick="confirmarAccion('ACTUALIZAR')"
                                style="background-color: #3498DB; color: white; padding: 10px 20px; border: none; cursor: pointer; font-size: 16px; margin-right: 20px; border-radius: 3px;">
                            Actualizar Solicitud
                        </button>
                    </c:if>

                    <%-- BOTÓN CANCELAR (VISIBLE SOLO PARA CLIENTE) --%>
                    <c:if test="${esCliente}">
                        <button type="button" onclick="confirmarAccion('CANCELAR')"
                                style="background-color: #6C3483; color: white; padding: 10px 20px; border: none; cursor: pointer; font-size: 16px; border-radius: 3px;">
                            Cancelar Solicitud
                        </button>
                    </c:if>
                </div>
            </form>
        </div>

        <script>
            // Función para limpiar todos los campos del formulario
            function limpiarCampos() {
                document.getElementById('txtFechaCreacion').value = '';
                document.getElementById('txtTipoServicio').value = '';
                document.getElementById('txtDescripcion').value = '';

                // Limpia Nivel de Prioridad (si existe en el DOM)
                var prioridad = document.getElementById('txtNivelPrioridad');
                if (prioridad)
                    prioridad.value = '';

                // Limpia Campo de Estado
                var estadoActualInput = document.getElementById('txtEstadoActual');
                if (estadoActualInput)
                    estadoActualInput.value = '';

                var cbNuevoEstado = document.getElementById('cbNuevoEstado');
                if (cbNuevoEstado)
                    cbNuevoEstado.value = '';

                document.getElementById('txtTicketSeleccionado').value = '';
            }

            // Función principal para cargar los datos del ticket seleccionado
            function cargarDatosTicketSeleccionado() {
                var ticketNum = document.getElementById('txtNumeroTicketDropdown').value;
                if (!ticketNum) {
                    limpiarCampos();
                    return;
                }

                var selectedSol = solicitudesData.find(s => s.numeroTicket === ticketNum);

                if (selectedSol) {
                    document.getElementById('txtFechaCreacion').value = selectedSol.fechaCreacion;
                    document.getElementById('txtTipoServicio').value = selectedSol.tipoServicio;
                    document.getElementById('txtDescripcion').value = selectedSol.descripcion;

                    // Carga Nivel de Prioridad (solo si el campo existe en el DOM)
                    var prioridad = document.getElementById('txtNivelPrioridad');
                    if (prioridad) {
                        prioridad.value = selectedSol.prioridad;
                    }

                    // Carga Estado de Solicitud (ya sea en input readonly o en select)
                    var estadoActualInput = document.getElementById('txtEstadoActual');
                    if (estadoActualInput) {
                        estadoActualInput.value = selectedSol.estadoActual;
                    }

                    var cbNuevoEstado = document.getElementById('cbNuevoEstado');
                    if (cbNuevoEstado) {
                        // Para Técnico/Programador, inicializa el select con el estado actual
                        cbNuevoEstado.value = selectedSol.estadoActual;
                    }

                    // Pone el ticket seleccionado en un campo oculto para que el Servlet lo reciba
                    document.getElementById('txtTicketSeleccionado').value = ticketNum;
                }
            }

            function confirmarAccion(accion) {
                var ticket = document.getElementById('txtNumeroTicketDropdown').value; // Usar el valor del dropdown directamente

                if (!ticket) {
                    alert("Debe seleccionar un ticket del dropdown primero.");
                    return;
                }

                var confirmMsg;

                if (accion === 'CANCELAR') {
                    confirmMsg = "¿Está seguro que desea CANCELAR el ticket #" + ticket + "?";
                } else if (accion === 'ACTUALIZAR') {
                    var nuevoEstado = document.getElementById('cbNuevoEstado').value;
                    if (!nuevoEstado || nuevoEstado === '-- Seleccione Nuevo Estado') {
                        alert("Debe seleccionar un estado válido para actualizar.");
                        return;
                    }
                    confirmMsg = "¿Está seguro de ACTUALIZAR el estado del ticket #" + ticket + " a " + nuevoEstado + "?";
                } else {
                    return;
                }

                if (confirm(confirmMsg)) {
                    // Si se confirma, aseguramos que el número de ticket se envíe con el nombre correcto
                    // Y establecemos la acción
                    document.getElementById('txtTicketSeleccionado').name = 'numeroTicket';
                    document.getElementById('txtTicketSeleccionado').value = ticket;
                    document.getElementById('accionInput').value = accion;

                    document.getElementById('formAccion').submit();
                }
            }
        </script>
    </body>
</html>