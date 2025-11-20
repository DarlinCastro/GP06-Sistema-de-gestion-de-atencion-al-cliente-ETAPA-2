<%-- 
    Document   : asignarSolicitud
    Created on : 12 nov 2025, 00:37:10
    Author     : DELL
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>ASIGNAR SOLICITUDES - Sistema de Tickets</title>
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

            .mensaje-exito {
                background: #d4edda;
                color: #155724;
                border: 1px solid #c3e6cb;
                padding: 12px 20px;
                border-radius: 8px;
                margin-bottom: 20px;
                font-weight: 500;
            }

            .mensaje-error {
                background: #f8d7da;
                color: #721c24;
                border: 1px solid #f5c6cb;
                padding: 12px 20px;
                border-radius: 8px;
                margin-bottom: 20px;
                font-weight: 500;
            }

            .form-container {
                background: #f8f9fa;
                border: 2px solid #e0e0e0;
                border-radius: 12px;
                padding: 25px;
                margin-bottom: 30px;
            }

            .form-grid {
                display: grid;
                grid-template-columns: 1fr 1fr;
                gap: 20px;
                margin-bottom: 20px;
            }

            .form-group {
                display: flex;
                align-items: center;
                gap: 10px;
            }

            .form-group label {
                font-weight: 600;
                color: #333;
                font-size: 14px;
                min-width: 140px;
                text-align: right;
            }

            .form-group input,
            .form-group select,
            .form-group textarea {
                flex: 1;
                padding: 10px 12px;
                border: 2px solid #e0e0e0;
                border-radius: 6px;
                font-size: 14px;
                transition: all 0.3s ease;
                font-family: inherit;
            }

            .form-group input:focus,
            .form-group select:focus,
            .form-group textarea:focus {
                outline: none;
                border-color: #667eea;
                box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
            }

            .form-group input[readonly] {
                background: #e9ecef;
                cursor: not-allowed;
            }

            .form-group textarea {
                resize: vertical;
                min-height: 60px;
            }

            .form-group-full {
                grid-column: 1 / -1;
            }

            .btn-container {
                text-align: right;
                margin-top: 20px;
            }

            .btn-asignar {
                background: #6a0dad;
                color: white;
                padding: 12px 40px;
                border: none;
                border-radius: 8px;
                font-size: 15px;
                font-weight: 600;
                cursor: pointer;
                transition: all 0.3s ease;
                box-shadow: 0 4px 15px rgba(106, 13, 173, 0.4);
            }

            .btn-asignar:hover {
                background: #580a94;
                transform: translateY(-2px);
                box-shadow: 0 6px 20px rgba(106, 13, 173, 0.6);
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
                background: white;
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
                letter-spacing: 0.5px;
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

            .badge {
                display: inline-block;
                padding: 4px 10px;
                border-radius: 12px;
                font-size: 11px;
                font-weight: 600;
                text-transform: uppercase;
            }

            .badge-pendiente {
                background: #fff3cd;
                color: #856404;
            }

            .badge-en.proceso {
                background: #cfe2ff;
                color: #084298;
            }

            .badge-finalizado {
                background: #d4edda;
                color: #155724;
            }

            .badge-cancelado {
                background: #f8d7da;
                color: #721c24;
            }

            .badge-baja {
                background: #d1ecf1;
                color: #0c5460;
            }

            .badge-media {
                background: #fff3cd;
                color: #856404;
            }

            .badge-alta {
                background: #f8d7da;
                color: #721c24;
            }

            .badge-urgente {
                background: #dc3545;
                color: white;
            }

            .footer {
                background: #001f3f;
                padding: 15px 30px;
                text-align: center;
                color: white;
                font-size: 15px;
                border-top: 1px solid #e0e0e0;
            }

            @media (max-width: 768px) {
                .form-grid {
                    grid-template-columns: 1fr;
                }

                .form-group label {
                    min-width: 120px;
                }
            }
        </style>
    </head>
    <body>
        <div class="container">
            <div class="header">
                <h2>ASIGNAR SOLICITUDES</h2>
                <a href="MenuAdmin.jsp" class="btn-atras">Atrás</a>
            </div>

            <div class="content">
                <!-- Mensajes -->
                <c:if test="${not empty sessionScope.mensajeExito}">
                    <div class="mensaje-exito">
                        ✓ ${sessionScope.mensajeExito}
                    </div>
                    <c:remove var="mensajeExito" scope="session"/>
                </c:if>

                <c:if test="${not empty sessionScope.error}">
                    <div class="mensaje-error">
                        ✗ ${sessionScope.error}
                    </div>
                    <c:remove var="error" scope="session"/>
                </c:if>

                <!-- Formulario de Asignación -->
                <form method="POST" action="Asignacion" accept-charset="UTF-8">
                    <div class="form-container">
                        <div class="form-grid">
                            <!-- Columna Izquierda -->
                            <div style="display: flex; flex-direction: column; gap: 15px;">
                                <div class="form-group">
                                    <label for="cbNumeroTicket">Nº Ticket:</label>
                                    <select name="numeroTicket" id="cbNumeroTicket" onchange="cargarDatosTicketCombo()" required>
                                        <option value="">-- Seleccione Ticket --</option>
                                        <c:forEach var="ticket" items="${listaTickets}">
                                            <option value="${ticket}">${ticket}</option>
                                        </c:forEach>
                                    </select>
                                </div>

                                <div class="form-group">
                                    <label for="txtFechaCreacion">Fecha Creación:</label>
                                    <input type="text" id="txtFechaCreacion" readonly>
                                </div>

                                <div class="form-group">
                                    <label for="txtTipoServicio">Tipo de Servicio:</label>
                                    <input type="text" id="txtTipoServicio" readonly>
                                </div>

                                <div class="form-group">
                                    <label for="txtDescripcion">Descripción:</label>
                                    <textarea id="txtDescripcion" readonly></textarea>
                                </div>

                                <div class="form-group">
                                    <label for="cbEstadoSolicitud">Estado Solicitud:</label>
                                    <select name="estadoSolicitud" id="cbEstadoSolicitud" required>
                                        <option value="">-- Seleccione Estado --</option>
                                        <c:forEach var="estado" items="${listaEstados}">
                                            <option value="${estado}">${estado}</option>
                                        </c:forEach>
                                    </select>
                                </div>
                            </div>

                            <!-- Columna Derecha -->
                            <div style="display: flex; flex-direction: column; gap: 15px;">
                                <div class="form-group">
                                    <label for="txtFechaAsignacion">Fecha Asignación:</label>
                                    <input type="text" name="fechaAsignacion" id="txtFechaAsignacion" value="<fmt:formatDate value="<%= new java.util.Date()%>" pattern="yyyy-MM-dd"/>" readonly>
                                </div>

                                <div class="form-group">
                                    <label for="cbNivelPrioridad">Nivel de Prioridad:</label>
                                    <select name="nivelPrioridad" id="cbNivelPrioridad" required>
                                        <option value="">-- Seleccione Prioridad --</option>
                                        <c:forEach var="prioridad" items="${listaPrioridades}">
                                            <option value="${prioridad}">${prioridad}</option>
                                        </c:forEach>
                                    </select>
                                </div>

                                <div class="form-group">
                                    <label for="cbCargo">Cargo:</label>
                                    <select name="cargo" id="cbCargo" onchange="cargarTecnicos()" required>
                                        <option value="">-- Seleccione Cargo --</option>
                                        <c:forEach var="cargo" items="${listaCargos}">
                                            <option value="${cargo}">${cargo}</option>
                                        </c:forEach>
                                    </select>
                                </div>

                                <div class="form-group">
                                    <label for="cbNombre">Nombre:</label>
                                    <select name="tecnicoNombre" id="cbNombre" required>
                                        <option value="">-- Seleccione Técnico --</option>
                                    </select>
                                </div>
                            </div>
                        </div>

                        <div class="btn-container">
                            <button type="submit" class="btn-asignar">Asignar</button>
                        </div>
                    </div>
                </form>

                <!-- Tabla de Solicitudes -->
                <div class="table-container">
                    <table id="tablaSolicitudes">
                        <thead>
                            <tr>
                                <th>Ticket</th>
                                <th>Fec. Creación</th>
                                <th>Servicio</th>
                                <th>Descripción</th>
                                <th>Estado Sol.</th>
                                <th>Fec. Asign.</th>
                                <th>Prioridad</th>
                                <th>Cargo</th>
                                <th>Técnico Asignado</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="sol" items="${listaSolicitudes}">
                                <tr onclick="cargarDatosTicketTabla('${sol.ticket.numeroTicket}')"> 
                                    <td><strong>${sol.ticket.numeroTicket}</strong></td>
                                    <td><fmt:formatDate value="${sol.fechaCreacion}" pattern="yyyy-MM-dd"/></td>
                                    <td>${sol.tipoServicio.nombreServicio}</td>
                                    <td>${sol.descripcion}</td>
                                    <td>
                                        <span class="badge badge-${fn:toLowerCase(fn:replace(sol.estadoSolicitud.estadoSolicitud, ' ', '.'))}">
                                            ${sol.estadoSolicitud.estadoSolicitud}
                                        </span>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${sol.ticket.fechaAsignacion != null}">
                                                <fmt:formatDate value="${sol.ticket.fechaAsignacion}" pattern="yyyy-MM-dd"/>
                                            </c:when>
                                            <c:otherwise>-</c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${sol.ticket.estadoTicket != null}">
                                                <span class="badge badge-${fn:toLowerCase(sol.ticket.estadoTicket.nivelPrioridad)}">
                                                    ${sol.ticket.estadoTicket.nivelPrioridad}
                                                </span>
                                            </c:when>
                                            <c:otherwise>-</c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${sol.ticket.tecnicoAsignado != null}">
                                                ${sol.ticket.tecnicoAsignado.tipoUsuario.cargo}
                                            </c:when>
                                            <c:otherwise>-</c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${sol.ticket.tecnicoAsignado != null}">
                                                ${sol.ticket.tecnicoAsignado.nombres} ${sol.ticket.tecnicoAsignado.apellidos}
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge badge-pendiente">PENDIENTE</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>

            <div class="footer">
                © 2025 KIA. Todos los derechos reservados
            </div>
        </div>

        <script>
            // Variables globales
            let solicitudesData = [];
            let tecnicosPorCargo = {};

            // Cargar datos al iniciar
            window.onload = function () {
                // Cargar solicitudes
            <c:forEach var="sol" items="${listaSolicitudes}">
                solicitudesData.push({
                    ticket: '${sol.ticket.numeroTicket}',
                    fechaCreacion: '<fmt:formatDate value="${sol.fechaCreacion}" pattern="yyyy-MM-dd"/>',
                    tipoServicio: '${sol.tipoServicio.nombreServicio}',
                    descripcion: '${sol.descripcion}',
                    estadoSolicitud: '${sol.estadoSolicitud.estadoSolicitud}',
                    prioridad: '${sol.ticket.estadoTicket != null ? sol.ticket.estadoTicket.nivelPrioridad : ""}',
                    cargo: '${sol.ticket.tecnicoAsignado != null ? sol.ticket.tecnicoAsignado.tipoUsuario.cargo : ""}',
                    tecnico: '<c:choose><c:when test="${sol.ticket.tecnicoAsignado != null}">${sol.ticket.tecnicoAsignado.nombres} ${sol.ticket.tecnicoAsignado.apellidos}</c:when><c:otherwise></c:otherwise></c:choose>'
                            });
            </c:forEach>

                            // Cargar técnicos por cargo
            <c:forEach var="entry" items="${tecnicosPorCargo}">
                            tecnicosPorCargo['${entry.key}'] = [
                <c:forEach var="tecnico" items="${entry.value}" varStatus="status">
                            '${tecnico}'<c:if test="${!status.last}">,</c:if>
                </c:forEach>
                            ];
            </c:forEach>

                            console.log('Solicitudes cargadas:', solicitudesData.length);
                            console.log('Técnicos por cargo:', tecnicosPorCargo);
                        };

                        // Función para cargar datos cuando se selecciona del combo de tickets
                        function cargarDatosTicketCombo() {
                            let ticketSeleccionado = document.getElementById('cbNumeroTicket').value;

                            if (!ticketSeleccionado) {
                                limpiarFormulario();
                                return;
                            }

                            let solicitud = solicitudesData.find(s => s.ticket === ticketSeleccionado);

                            if (solicitud) {
                                rellenarFormulario(solicitud);
                                marcarFilaTabla(ticketSeleccionado);
                            }
                        }

                        // Función para cargar datos cuando se hace clic en la tabla
                        function cargarDatosTicketTabla(numeroTicket) {
                            // Seleccionar el ticket en el combo
                            document.getElementById('cbNumeroTicket').value = numeroTicket;

                            let solicitud = solicitudesData.find(s => s.ticket === numeroTicket);

                            if (solicitud) {
                                rellenarFormulario(solicitud);
                                marcarFilaTabla(numeroTicket);
                            }
                        }

                        // Función para rellenar el formulario con los datos
                        function rellenarFormulario(solicitud) {
                            document.getElementById('txtFechaCreacion').value = solicitud.fechaCreacion;
                            document.getElementById('txtTipoServicio').value = solicitud.tipoServicio;
                            document.getElementById('txtDescripcion').value = solicitud.descripcion;
                            document.getElementById('cbEstadoSolicitud').value = solicitud.estadoSolicitud;
                            document.getElementById('cbNivelPrioridad').value = solicitud.prioridad;

                            // Limpiar combo de técnicos primero
                            document.getElementById('cbNombre').innerHTML = '<option value="">-- Seleccione Técnico --</option>';

                            // Si hay cargo y técnico asignado, cargarlos
                            if (solicitud.cargo && solicitud.cargo.trim() !== '') {
                                document.getElementById('cbCargo').value = solicitud.cargo.trim();

                                // Cargar técnicos del cargo
                                cargarTecnicos().then(() => {
                                    let tecnicoCompleto = solicitud.tecnico.trim();
                                    // Verificar que el técnico no esté vacío
                                    if (tecnicoCompleto && tecnicoCompleto !== ' ' && tecnicoCompleto !== 'null null') {
                                        // Intentar seleccionar el técnico en el combo
                                        let selectTecnico = document.getElementById('cbNombre');
                                        let encontrado = false;

                                        // Buscar el técnico en las opciones
                                        for (let i = 0; i < selectTecnico.options.length; i++) {
                                            if (selectTecnico.options[i].value.trim() === tecnicoCompleto) {
                                                selectTecnico.selectedIndex = i;
                                                encontrado = true;
                                                break;
                                            }
                                        }

                                        if (!encontrado) {
                                            console.log('Técnico no encontrado en lista: "' + tecnicoCompleto + '"');
                                        }
                                    }
                                });
                            } else {
                                // Si no hay cargo asignado, limpiar los combos
                                document.getElementById('cbCargo').value = '';
                                document.getElementById('cbNombre').innerHTML = '<option value="">-- Seleccione Técnico --</option>';
                            }
                        }

                        // Función para marcar la fila seleccionada en la tabla
                        function marcarFilaTabla(numeroTicket) {
                            let filas = document.querySelectorAll('#tablaSolicitudes tbody tr');
                            filas.forEach(fila => {
                                fila.classList.remove('selected');
                                if (fila.cells[0].textContent.trim() === numeroTicket) {
                                    fila.classList.add('selected');
                                }
                            });
                        }

                        // Función para limpiar el formulario
                        function limpiarFormulario() {
                            document.getElementById('txtFechaCreacion').value = '';
                            document.getElementById('txtTipoServicio').value = '';
                            document.getElementById('txtDescripcion').value = '';
                            document.getElementById('cbEstadoSolicitud').value = '';
                            document.getElementById('cbNivelPrioridad').value = '';
                            document.getElementById('cbCargo').value = '';
                            document.getElementById('cbNombre').innerHTML = '<option value="">-- Seleccione Técnico --</option>';

                            let filas = document.querySelectorAll('#tablaSolicitudes tbody tr');
                            filas.forEach(fila => fila.classList.remove('selected'));
                        }

                        // Función para cargar técnicos por cargo (SIN AJAX - filtrado local)
                        function cargarTecnicos() {
                            return new Promise((resolve) => {
                                let cargo = document.getElementById('cbCargo').value;
                                let cbNombre = document.getElementById('cbNombre');

                                cbNombre.innerHTML = '<option value="">-- Seleccione Técnico --</option>';

                                if (!cargo) {
                                    resolve();
                                    return;
                                }

                                // Obtener técnicos del cargo desde la variable global
                                let tecnicos = tecnicosPorCargo[cargo] || [];

                                if (tecnicos.length === 0) {
                                    cbNombre.innerHTML += '<option value="" disabled>No hay técnicos disponibles</option>';
                                } else {
                                    tecnicos.forEach(tecnico => {
                                        cbNombre.innerHTML += '<option value="' + tecnico + '">' + tecnico + '</option>';
                                    });
                                }
                                resolve();
                            });
                        }
        </script>
    </body>
</html>