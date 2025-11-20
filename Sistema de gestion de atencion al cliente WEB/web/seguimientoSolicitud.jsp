<%--
    Document   : seguimientoSolicitud
    Created on : 15 nov 2025, 23:29:55
    Autor      : Erick :)
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>SEGUIMIENTO DE SOLICITUD</title>

    <style>
        body {
            margin: 0;
            padding: 0;
            font-family: Arial, sans-serif;
            background: linear-gradient(135deg, #4e73df, #8e44ad);
            height: 100vh;
            overflow-x: hidden;
        }

        /* Barra superior */
        .top-bar {
            background-color: #0a2240;
            padding: 15px 25px;
            display: flex;
            justify-content: flex-end;
        }

        .btn-atras {
            background-color: #3a7afe;
            padding: 8px 18px;
            color: white;
            text-decoration: none;
            font-size: 15px;
            border-radius: 6px;
        }

        /* Tarjeta central */
        .contenedor {
            width: 520px;
            margin: 40px auto;
            background: white;
            padding: 35px;
            border-radius: 16px;
            box-shadow: 0px 8px 25px rgba(0, 0, 0, 0.25);
            text-align: left;
        }

        h2 {
            text-align: center;
            margin-top: 0;
            margin-bottom: 25px;
            color: #0a2240;
        }

        .field-row {
            margin-bottom: 14px;
        }

        .field-row label {
            font-weight: bold;
            display: block;
            margin-bottom: 6px;
        }

        input, select, textarea {
            width: 100%;
            padding: 10px;
            border-radius: 8px;
            border: 1px solid #cfcfcf;
            box-sizing: border-box;
            font-size: 14px;
        }

        .readonly-field {
            background-color: #f2f2f2;
        }

        textarea {
            resize: vertical;
            height: 120px;
        }

        .acciones {
            text-align: center;
            margin-top: 25px;
        }

        .btn-accion {
            padding: 12px 28px;
            border: none;
            font-size: 15px;
            border-radius: 8px;
            cursor: pointer;
            margin: 0 10px;
        }

        .btn-update {
            background-color: #3498db;
            color: white;
        }

        .btn-cancel {
            background-color: #8e44ad;
            color: white;
        }

        .msg-exito {
            color: #1e8449;
            text-align: center;
            font-weight: bold;
        }

        .msg-error {
            color: #c0392b;
            text-align: center;
            font-weight: bold;
        }

        /* FOOTER FIJO */
        .footer {
          position: fixed;
          bottom: 0;
          left: 0;
          width: 100%;
          background-color: #1a237e;
          color: white;
          text-align: center;
          padding: 15px 0;
          font-size: 14px;
          z-index: 1;
        }

    </style>
</head>

<body>

    <!-- BARRA SUPERIOR -->
    <div class="top-bar">
        <c:set var="urlDestino" value="${requestScope.urlAtras}" />
        <c:if test="${empty urlDestino}">
            <c:set var="urlDestino" value="Menu${sessionScope.usuarioActual.tipoUsuario.cargo.trim()}.jsp" />
        </c:if>

        <a href="${urlDestino}" class="btn-atras">Atrás</a>
    </div>

    <!-- TARJETA CENTRAL -->
    <div class="contenedor">

        <h2>Seguimiento de Solicitud</h2>

        <p class="msg-exito">${sessionScope.mensajeExito}</p>
        <p class="msg-error">${sessionScope.error}</p>
        <c:remove var="mensajeExito" scope="session"/>
        <c:remove var="error" scope="session"/>

        <c:set var="esCliente" value="${sessionScope.usuarioActual.tipoUsuario.cargo.trim() == 'Cliente'}" />

        <form method="POST" action="Seguimiento" id="formAccion">
            <input type="hidden" name="accion" id="accionInput">

            <script> let solicitudesData = []; </script>

            <c:forEach var="sol" items="${requestScope.listaSolicitudes}">
                <script>
                    solicitudesData.push({
                        numeroTicket: '${sol.ticket.numeroTicket.trim()}',
                        fechaCreacion: '<fmt:formatDate value="${sol.fechaCreacion}" pattern="dd/MM/yyyy"/>',
                        tipoServicio: '${sol.tipoServicio.nombreServicio.trim()}',
                        descripcion: '${sol.descripcion}',
                        estadoActual: '${sol.estadoSolicitud.estadoSolicitud.trim()}',
                        prioridad: '${sol.ticket.estadoTicket.nivelPrioridad.trim()}'
                    });
                </script>
            </c:forEach>

            <!-- DATOS FIJOS -->
            <div class="field-row">
                <label>Cargo</label>
                <input type="text" class="readonly-field" readonly
                       value="${sessionScope.usuarioActual.tipoUsuario.cargo.trim()}">
            </div>

            <div class="field-row">
                <label>Nombre</label>
                <input type="text" class="readonly-field" readonly
                       value="${sessionScope.usuarioActual.nombres.trim()} ${sessionScope.usuarioActual.apellidos.trim()}">
            </div>

            <hr>

            <!-- TICKET -->
            <div class="field-row">
                <label>N° Ticket</label>
                <select id="txtNumeroTicketDropdown" name="numeroTicket" onchange="cargarDatosTicketSeleccionado()" required>
                    <option value="">-- Seleccione un Ticket --</option>

                    <c:forEach var="sol" items="${requestScope.listaSolicitudes}">
                        <option value="${sol.ticket.numeroTicket.trim()}">${sol.ticket.numeroTicket.trim()}</option>
                    </c:forEach>
                </select>
                <input type="hidden" id="txtTicketSeleccionado" name="numeroTicketHidden">
            </div>

            <!-- CAMPOS AUTO-RELLENADOS -->
            <div class="field-row">
                <label>Fecha Creación</label>
                <input type="text" id="txtFechaCreacion" class="readonly-field" readonly>
            </div>

            <div class="field-row">
                <label>Tipo Servicio</label>
                <input type="text" id="txtTipoServicio" class="readonly-field" readonly>
            </div>

            <div class="field-row">
                <label>Descripción</label>
                <textarea id="txtDescripcion" class="readonly-field" readonly></textarea>
            </div>

            <c:if test="${!esCliente}">
                <div class="field-row">
                    <label>Nivel de Prioridad</label>
                    <input type="text" id="txtNivelPrioridad" class="readonly-field" readonly>
                </div>
            </c:if>

            <!-- ESTADO -->
            <div class="field-row">
                <label>Estado de Solicitud</label>

                <c:choose>
                    <c:when test="${esCliente}">
                        <input type="text" id="txtEstadoActual" name="nuevoEstado"
                               class="readonly-field" readonly>
                    </c:when>

                    <c:otherwise>
                        <select id="cbNuevoEstado" name="nuevoEstado" required>
                            <option value="">-- Seleccione Nuevo Estado --</option>
                            <c:forEach var="estado" items="${requestScope.listaTodosLosEstados}">
                                <option value="${estado.estadoSolicitud}">${estado.estadoSolicitud}</option>
                            </c:forEach>
                        </select>
                        <input type="hidden" id="txtEstadoActual">
                    </c:otherwise>
                </c:choose>
            </div>

            <!-- BOTONES -->
            <div class="acciones">
                <c:if test="${!esCliente}">
                    <button type="button" class="btn-accion btn-update"
                            onclick="confirmarAccion('ACTUALIZAR')">
                        Actualizar Solicitud
                    </button>
                </c:if>

                <c:if test="${esCliente}">
                    <button type="button" class="btn-accion btn-cancel"
                            onclick="confirmarAccion('CANCELAR')">
                        Cancelar Solicitud
                    </button>
                </c:if>
            </div>

        </form>
    </div>

    <!-- FOOTER -->
    <div class="footer">
        © 2025 KIA. Todos los derechos reservados.
    </div>

    <!-- SCRIPTS -->
    <script>
        function limpiarCampos() {
            document.getElementById('txtFechaCreacion').value = '';
            document.getElementById('txtTipoServicio').value = '';
            document.getElementById('txtDescripcion').value = '';
            let p = document.getElementById('txtNivelPrioridad'); if (p) p.value = '';
            let e = document.getElementById('txtEstadoActual'); if (e) e.value = '';
            let cb = document.getElementById('cbNuevoEstado'); if (cb) cb.value = '';
            document.getElementById('txtTicketSeleccionado').value = '';
        }

        function cargarDatosTicketSeleccionado() {
            const ticketNum = document.getElementById("txtNumeroTicketDropdown").value;
            if (!ticketNum) return limpiarCampos();

            const s = solicitudesData.find(sol => sol.numeroTicket === ticketNum);
            if (!s) return;

            document.getElementById("txtFechaCreacion").value = s.fechaCreacion;
            document.getElementById("txtTipoServicio").value = s.tipoServicio;
            document.getElementById("txtDescripcion").value = s.descripcion;
            const p = document.getElementById("txtNivelPrioridad"); if (p) p.value = s.prioridad;
            document.getElementById("txtEstadoActual").value = s.estadoActual;

            const cb = document.getElementById("cbNuevoEstado");
            if (cb) cb.value = s.estadoActual;

            document.getElementById("txtTicketSeleccionado").value = ticketNum;
        }

        function confirmarAccion(accion) {
            const ticket = document.getElementById("txtNumeroTicketDropdown").value;
            if (!ticket) return alert("Debe seleccionar un ticket.");

            let msg = "";
            if (accion === "CANCELAR") {
                msg = `¿Cancelar ticket #${ticket}?`;
            } else {
                const nuevo = document.getElementById("cbNuevoEstado").value;
                if (!nuevo) return alert("Seleccione un estado válido.");
                msg = `¿Actualizar ticket #${ticket} a "${nuevo}"?`;
            }

            if (confirm(msg)) {
                document.getElementById("txtTicketSeleccionado").name = "numeroTicket";
                document.getElementById("accionInput").value = accion;
                document.getElementById("formAccion").submit();
            }
        }
    </script>

</body>
</html>



