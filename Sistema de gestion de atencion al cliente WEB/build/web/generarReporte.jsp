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
            * {
                margin: 0;
                padding: 0;
                box-sizing: border-box;
            }

            body {
                font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                min-height: 100vh;
                padding: 20px;
            }

            .container {
                max-width: 1400px;
                margin: 0 auto;
                background: white;
                border-radius: 12px;
                box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
                overflow: hidden;
            }

            .header {
                background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                color: white;
                padding: 20px 30px;
                display: flex;
                justify-content: space-between;
                align-items: center;
            }

            .header h2 {
                font-size: 24px;
                font-weight: 600;
                font-family: Arial, sans-serif;
            }

            .btn-atras {
                background: #6a0dad;
                color: white;
                padding: 10px 20px;
                border: none;
                border-radius: 8px;
                text-decoration: none;
                font-weight: 600;
                transition: all 0.3s ease;
                cursor: pointer;
            }

            .btn-atras:hover {
                background: #580a94;
                transform: translateY(-2px);
                box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
            }

            .content {
                padding: 30px;
            }

            .filtro-section {
                display: flex;
                justify-content: space-between;
                align-items: center;
                margin-bottom: 20px;
                gap: 15px;
                flex-wrap: wrap;
            }

            .filtro-form {
                display: flex;
                align-items: center;
                gap: 10px;
            }

            .filtro-form label {
                font-weight: 600;
                color: #333;
            }

            .filtro-form select {
                padding: 10px 15px;
                border: 2px solid #e0e0e0;
                border-radius: 8px;
                font-size: 14px;
                cursor: pointer;
                transition: all 0.3s ease;
            }

            .filtro-form select:focus {
                outline: none;
                border-color: #667eea;
                box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
            }

            .btn-reporte-general {
                background: linear-gradient(135deg, #28a745 0%, #20c997 100%);
                color: white;
                padding: 12px 25px;
                border: none;
                border-radius: 8px;
                font-size: 15px;
                font-weight: 600;
                cursor: pointer;
                transition: all 0.3s ease;
                box-shadow: 0 4px 15px rgba(40, 167, 69, 0.4);
            }

            .btn-reporte-general:hover {
                transform: translateY(-2px);
                box-shadow: 0 6px 20px rgba(40, 167, 69, 0.6);
            }

            .btn-generar-ticket {
                background: linear-gradient(135deg, #dc3545 0%, #c82333 100%);
                color: white;
                padding: 12px 25px;
                border: none;
                border-radius: 8px;
                font-size: 15px;
                font-weight: 600;
                cursor: pointer;
                transition: all 0.3s ease;
                box-shadow: 0 4px 15px rgba(220, 53, 69, 0.4);
                display: none;
            }

            .btn-generar-ticket:hover {
                transform: translateY(-2px);
                box-shadow: 0 6px 20px rgba(220, 53, 69, 0.6);
            }

            .btn-generar-ticket.visible {
                display: inline-block;
            }

            .table-container {
                overflow-x: auto;
                overflow-y: auto;
                max-height: 500px;
                border-radius: 12px;
                box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
                margin-top: 20px;
            }

            table {
                width: 100%;
                border-collapse: collapse;
                font-size: 13px;
            }

            thead {
                background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                color: white;
                position: sticky;
                top: 0;
                z-index: 10;
            }

            th {
                padding: 12px 10px;
                text-align: left;
                font-weight: 600;
                font-size: 12px;
                text-transform: uppercase;
            }

            tbody tr {
                border-bottom: 1px solid #e0e0e0;
                transition: all 0.2s ease;
                cursor: pointer;
            }

            tbody tr:hover {
                background: #f1f3f5;
            }

            tbody tr.selected {
                background: #e3f2fd;
                border-left: 4px solid #667eea;
            }

            td {
                padding: 12px 10px;
                color: #555;
            }

            .mensaje-vacio {
                text-align: center;
                color: #888;
                margin-top: 30px;
                padding: 20px;
                background-color: #fff3cd;
                border: 2px solid #ffc107;
                border-radius: 8px;
                font-size: 16px;
            }

            /* ESTILOS DEL MODAL */
            .modal {
                display: none;
                position: fixed;
                z-index: 1000;
                left: 0;
                top: 0;
                width: 100%;
                height: 100%;
                overflow: auto;
                background-color: rgba(0, 0, 0, 0.6);
                animation: fadeIn 0.3s;
            }

            @keyframes fadeIn {
                from {
                    opacity: 0;
                }
                to {
                    opacity: 1;
                }
            }

            .modal-content {
                background-color: white;
                margin: 3% auto;
                padding: 0;
                border-radius: 12px;
                width: 90%;
                max-width: 1200px;
                box-shadow: 0 10px 40px rgba(0, 0, 0, 0.3);
                animation: slideDown 0.3s;
            }

            @keyframes slideDown {
                from {
                    transform: translateY(-50px);
                    opacity: 0;
                }
                to {
                    transform: translateY(0);
                    opacity: 1;
                }
            }

            .modal-header {
                background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                color: white;
                padding: 20px 30px;
                border-radius: 12px 12px 0 0;
                display: flex;
                justify-content: space-between;
                align-items: center;
            }

            .modal-header h3 {
                font-size: 22px;
                font-weight: 600;
            }

            .close {
                color: white;
                font-size: 32px;
                font-weight: bold;
                cursor: pointer;
                transition: all 0.3s ease;
            }

            .close:hover {
                transform: scale(1.2);
            }

            .modal-body {
                padding: 30px;
                max-height: 70vh;
                overflow-y: auto;
            }

            .modal-footer {
                padding: 20px 30px;
                border-top: 1px solid #e0e0e0;
                text-align: right;
            }

            .btn-imprimir {
                display: none;
            }

            @media print {
                display: none;
            }
            .ticket-container {
                border: 2px solid #667eea;
                border-radius: 12px;
                padding: 30px;
                background: linear-gradient(to bottom, #ffffff 0%, #f8f9fa 100%);
            }

            .ticket-header {
                text-align: center;
                margin-bottom: 30px;
                padding-bottom: 20px;
                border-bottom: 3px solid #667eea;
            }

            .ticket-header h2 {
                color: #667eea;
                font-size: 28px;
                margin-bottom: 5px;
            }

            .ticket-header .ticket-numero {
                font-size: 18px;
                color: #666;
                font-weight: 600;
            }

            .ticket-section {
                margin-bottom: 25px;
            }

            .ticket-section h3 {
                color: #667eea;
                font-size: 16px;
                margin-bottom: 15px;
                padding-bottom: 8px;
                border-bottom: 2px solid #e0e0e0;
            }

            .ticket-row {
                display: grid;
                grid-template-columns: 200px 1fr;
                padding: 10px 0;
                border-bottom: 1px solid #f0f0f0;
            }

            .ticket-label {
                font-weight: 600;
                color: #333;
            }

            .ticket-value {
                color: #555;
            }

            .ticket-descripcion {
                background: #f8f9fa;
                padding: 15px;
                border-radius: 8px;
                border-left: 4px solid #667eea;
                margin-top: 10px;
            }

            .footer {
                background: #001f3f;
                padding: 15px 30px;
                text-align: center;
                color: white;
                font-size: 15px;
                border-top: 1px solid #e0e0e0;
            }

            /* ESTILOS DEL TICKET */
        </style>
    </head>
    <body>
        <div class="container">
            <div class="header">
                <h2>REPORTE GENERAL DE TICKETS DE SOPORTE</h2>
                <a href="GenerarReporteController?accion=atras&origen=${requestScope.origen}" class="btn-atras">Atrás</a>
            </div>

            <div class="content">
                <div class="filtro-section">
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

                    <div style="display: flex; gap: 10px;">
                        <button class="btn-reporte-general" onclick="abrirModalReporteGeneral()">
                            📊 Reporte General
                        </button>
                        <button class="btn-generar-ticket" id="btnGenerarTicket" onclick="abrirModalTicket()">
                            🎫 Generar Ticket
                        </button>
                    </div>
                </div>

                <c:choose>
                    <c:when test="${not empty listaReportes}">
                        <div class="table-container">
                            <table id="tablaReportes">
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
                                        <tr onclick="seleccionarFila(this, {
                                                    numeroTicket: '${data.numeroTicket}',
                                                    fechaCreacion: '<fmt:formatDate value="${data.fechaCreacion}" pattern="dd/MM/yyyy"/>',
                                                    estadoTicket: '${data.estadoTicket}',
                                                    tipoServicio: '${data.tipoServicio}',
                                                    descripcion: '${data.descripcionServicio}',
                                                    nombreCliente: '${data.nombreCliente}',
                                                    fechaAsignacion: '<c:choose><c:when test="${not empty data.fechaAsignacion}"><fmt:formatDate value="${data.fechaAsignacion}" pattern="dd/MM/yyyy"/></c:when><c:otherwise>N/A</c:otherwise></c:choose>',
                                                                nombreEncargado: '${data.nombreEncargadoSoporte}',
                                                                cargoEncargado: '${data.cargoEncargado}'
                                                            })">
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
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="mensaje-vacio">
                            <p>⚠️ No se encontraron tickets para el filtro seleccionado.</p>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>

            <div class="footer">
                © 2025 KIA. Todos los derechos reservados
            </div>
        </div>

        <!-- MODAL 1: REPORTE GENERAL -->
        <div id="modalReporteGeneral" class="modal">
            <div class="modal-content">
                <div class="modal-header">
                    <h3>📊 REPORTE GENERAL DE TICKETS</h3>
                    <span class="close" onclick="cerrarModal('modalReporteGeneral')">&times;</span>
                </div>
                <div class="modal-body">
                    <table style="font-size: 12px;">
                        <thead>
                            <tr>
                                <th>N° Ticket</th>
                                <th>Fecha Creación</th>
                                <th>Estado</th>
                                <th>Tipo Servicio</th>
                                <th>Descripción</th>
                                <th>Cliente</th>
                                <th>Fec. Asignación</th>
                                <th>Encargado</th>
                                <th>Cargo</th>
                            </tr>
                        </thead>
                        <tbody id="tablaModalReporte">
                            <!-- Se llena dinámicamente -->
                        </tbody>
                    </table>
                </div>
                <div class="modal-footer" style="display: none;">
                </div>
            </div>
        </div>

        <!-- MODAL 2: TICKET INDIVIDUAL -->
        <div id="modalTicket" class="modal">
            <div class="modal-content" style="max-width: 800px;">
                <div class="modal-header">
                    <h3>🎫 TICKET DE SOPORTE</h3>
                    <span class="close" onclick="cerrarModal('modalTicket')">&times;</span>
                </div>
                <div class="modal-body">
                    <div class="ticket-container">
                        <div class="ticket-header">
                            <h2>TICKET DE SOPORTE TÉCNICO</h2>
                            <div class="ticket-numero" id="ticketNumero">N° TICKET: T0001</div>
                        </div>

                        <div class="ticket-section">
                            <h3>📋 Información del Ticket</h3>
                            <div class="ticket-row">
                                <div class="ticket-label">Número de Ticket:</div>
                                <div class="ticket-value" id="ticketNumeroVal">-</div>
                            </div>
                            <div class="ticket-row">
                                <div class="ticket-label">Fecha de Creación:</div>
                                <div class="ticket-value" id="ticketFechaCreacion">-</div>
                            </div>
                            <div class="ticket-row">
                                <div class="ticket-label">Estado:</div>
                                <div class="ticket-value" id="ticketEstado">-</div>
                            </div>
                            <div class="ticket-row">
                                <div class="ticket-label">Tipo de Servicio:</div>
                                <div class="ticket-value" id="ticketTipoServicio">-</div>
                            </div>
                        </div>

                        <div class="ticket-section">
                            <h3>👤 Información del Cliente</h3>
                            <div class="ticket-row">
                                <div class="ticket-label">Nombre del Cliente:</div>
                                <div class="ticket-value" id="ticketCliente">-</div>
                            </div>
                        </div>

                        <div class="ticket-section">
                            <h3>📝 Descripción del Problema</h3>
                            <div class="ticket-descripcion" id="ticketDescripcion">
                                -
                            </div>
                        </div>

                        <div class="ticket-section">
                            <h3>🔧 Información de Asignación</h3>
                            <div class="ticket-row">
                                <div class="ticket-label">Fecha de Asignación:</div>
                                <div class="ticket-value" id="ticketFechaAsignacion">-</div>
                            </div>
                            <div class="ticket-row">
                                <div class="ticket-label">Encargado de Soporte:</div>
                                <div class="ticket-value" id="ticketEncargado">-</div>
                            </div>
                            <div class="ticket-row">
                                <div class="ticket-label">Cargo:</div>
                                <div class="ticket-value" id="ticketCargo">-</div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <script>
            let filaSeleccionada = null;
            let datosFilaSeleccionada = null;

            // Seleccionar fila de la tabla
            function seleccionarFila(fila, datos) {
                // Remover selección anterior
                if (filaSeleccionada) {
                    filaSeleccionada.classList.remove('selected');
                }

                // Seleccionar nueva fila
                fila.classList.add('selected');
                filaSeleccionada = fila;
                datosFilaSeleccionada = datos;

                // Mostrar botón "Generar Ticket"
                document.getElementById('btnGenerarTicket').classList.add('visible');
            }

            // Abrir modal de reporte general
            function abrirModalReporteGeneral() {
                let tabla = document.getElementById('tablaReportes');
                let tbody = tabla.querySelector('tbody');
                let modalTbody = document.getElementById('tablaModalReporte');

                // Copiar contenido de la tabla
                modalTbody.innerHTML = tbody.innerHTML;

                // Quitar eventos onclick de las filas del modal
                let filasModal = modalTbody.querySelectorAll('tr');
                filasModal.forEach(fila => {
                    fila.onclick = null;
                    fila.style.cursor = 'default';
                });

                document.getElementById('modalReporteGeneral').style.display = 'block';
            }

            // Abrir modal de ticket individual
            function abrirModalTicket() {
                if (!datosFilaSeleccionada) {
                    alert('Por favor, seleccione una fila de la tabla primero.');
                    return;
                }

                // Llenar datos del ticket
                document.getElementById('ticketNumero').textContent = 'N° TICKET: ' + datosFilaSeleccionada.numeroTicket;
                document.getElementById('ticketNumeroVal').textContent = datosFilaSeleccionada.numeroTicket;
                document.getElementById('ticketFechaCreacion').textContent = datosFilaSeleccionada.fechaCreacion;
                document.getElementById('ticketEstado').textContent = datosFilaSeleccionada.estadoTicket;
                document.getElementById('ticketTipoServicio').textContent = datosFilaSeleccionada.tipoServicio;
                document.getElementById('ticketCliente').textContent = datosFilaSeleccionada.nombreCliente;
                document.getElementById('ticketDescripcion').textContent = datosFilaSeleccionada.descripcion;
                document.getElementById('ticketFechaAsignacion').textContent = datosFilaSeleccionada.fechaAsignacion;
                document.getElementById('ticketEncargado').textContent = datosFilaSeleccionada.nombreEncargado;
                document.getElementById('ticketCargo').textContent = datosFilaSeleccionada.cargoEncargado;

                document.getElementById('modalTicket').style.display = 'block';
            }

            // Cerrar modal
            function cerrarModal(idModal) {
                document.getElementById(idModal).style.display = 'none';
            }

            // Cerrar modal al hacer clic fuera
            window.onclick = function (event) {
                let modalReporte = document.getElementById('modalReporteGeneral');
                let modalTicket = document.getElementById('modalTicket');

                if (event.target == modalReporte) {
                    cerrarModal('modalReporteGeneral');
                }
                if (event.target == modalTicket) {
                    cerrarModal('modalTicket');
                }
            }

            // Cerrar modal con tecla ESC
            document.addEventListener('keydown', function (event) {
                if (event.key === 'Escape') {
                    cerrarModal('modalReporteGeneral');
                    cerrarModal('modalTicket');
                }
            });
        </script>
    </body>
</html>