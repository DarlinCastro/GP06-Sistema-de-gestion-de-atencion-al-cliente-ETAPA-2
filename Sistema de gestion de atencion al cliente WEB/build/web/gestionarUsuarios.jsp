<%-- 
    Document   : gestionarUsuarios
    Created on : 12 nov 2025, 01:28:06
    Author     : DELL
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%
    // Si no hay datos, redirigir al servlet
    if (request.getAttribute("listaUsuarios") == null) {
        response.sendRedirect("GestionarUsuarios");
        return;
    }
%>

<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>GESTIONAR USUARIOS</title>
        <style>
            * {
                box-sizing: border-box;
                margin: 0;
                padding: 0;
            }

            body {
                font-family: Arial, sans-serif;
                background-color: #f5f5f5;
            }

            /* ✅ HEADER CON DEGRADADO */
            .header-container {
                background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                padding: 20px;
                color: white;
                box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
                display: flex;
                justify-content: space-between;
                align-items: center;
            }

            .header-container h2 {
                margin: 0;
                font-size: 28px;
                font-weight: 600;
            }

            .header-container .btn-atras-container {
                margin-left: auto;
            }

            .header-container button {
                padding: 10px 20px;
                background: #6a0dad;
                color: white;
                border: none;
                border-radius: 6px;
                cursor: pointer;
                font-weight: 600;
                transition: all 0.3s;
            }

            .header-container button:hover {
                background: #580a94;
                transform: translateY(-2px);
                box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
            }

            .content-wrapper {
                padding: 20px;
                background: #c0c0c0;
            }

            /* ✅ CONTENEDOR PRINCIPAL EN 2 COLUMNAS */
            .contenedor-principal {
                display: flex;
                gap: 20px;
                max-width: 1400px;
                margin: 0 auto;
                align-items: flex-start;
                background: #c0c0c0;
            }

            /* ✅ COLUMNA IZQUIERDA - FORMULARIO */
            .columna-formulario {
                flex: 0 0 400px;
                background: white;
                padding: 20px;
                border-radius: 8px;
                box-shadow: 0 2px 8px rgba(0,0,0,0.1);
            }

            /* ✅ COLUMNA DERECHA - TABLA */
            .columna-tabla {
                flex: 1;
                background: white;
                padding: 20px;
                border-radius: 8px;
                box-shadow: 0 2px 8px rgba(0,0,0,0.1);
            }

            .mensaje-exito {
                color: green;
                font-weight: bold;
                padding: 10px;
                background-color: #d4edda;
                border: 1px solid #c3e6cb;
                border-radius: 4px;
                margin: 10px auto;
                max-width: 1400px;
            }

            .mensaje-error {
                color: red;
                font-weight: bold;
                padding: 10px;
                background-color: #f8d7da;
                border: 1px solid #f5c6cb;
                border-radius: 4px;
                margin: 10px auto;
                max-width: 1400px;
            }

            fieldset {
                border: 2px solid #667eea;
                border-radius: 8px;
                padding: 15px;
            }

            legend {
                padding: 5px 10px;
                font-weight: bold;
                color: #667eea;
            }

            label {
                display: block;
                margin-top: 10px;
                font-weight: 500;
            }

            input, select {
                width: 100%;
                margin: 5px 0;
                padding: 8px;
                border: 2px solid #e0e0e0;
                border-radius: 4px;
            }

            input:focus, select:focus {
                outline: none;
                border-color: #667eea;
            }

            .botones-container {
                margin-top: 15px;
                display: flex;
                flex-wrap: wrap;
                gap: 5px;
            }

            button {
                flex: 1;
                min-width: 80px;
                padding: 10px;
                border: none;
                border-radius: 6px;
                cursor: pointer;
                font-weight: 600;
                transition: all 0.3s;
            }

            button[onclick*="REGISTRAR"] {
                background: #667eea;
                color: white;
            }

            button[onclick*="ACTUALIZAR"] {
                background: #f5576c;
                color: white;
            }

            button[onclick*="ELIMINAR"] {
                background: #fa709a;
                color: white;
            }

            button[type="reset"] {
                background: #a8edea;
                color: #333;
            }

            button:hover {
                transform: translateY(-2px);
                box-shadow: 0 4px 12px rgba(0,0,0,0.15);
            }

            /* TABLA CON SCROLL */
            .tabla-scroll {
                max-height: 500px;
                overflow-y: auto;
                border: 1px solid #ddd;
                margin-top: 10px;
                border-radius: 4px;
            }

            table {
                width: 100%;
                border-collapse: collapse;
            }

            th, td {
                border: 1px solid #e0e0e0;
                padding: 10px;
                text-align: left;
            }

            th {
                background-color: #667eea;
                color: white;
                font-weight: 600;
                position: sticky;
                top: 0;
                z-index: 10;
            }

            tbody tr:hover {
                background-color: #c8e1ff !important;
                cursor: pointer;
            }

            tbody tr:nth-child(even) {
                background-color: #f9f9f9;
            }

            /* Fila seleccionada */
            tr.seleccionada {
                background-color: #a3c9ff !important;
                border-left: 4px solid #667eea;
                font-weight: 600;
            }

            h3 {
                margin-bottom: 10px;
                color: #333;
            }

            /* Barra de scroll personalizada */
            .tabla-scroll::-webkit-scrollbar {
                width: 10px;
            }

            .tabla-scroll::-webkit-scrollbar-track {
                background: #f1f1f1;
                border-radius: 10px;
            }

            .tabla-scroll::-webkit-scrollbar-thumb {
                background: #667eea;
                border-radius: 10px;
            }

            .tabla-scroll::-webkit-scrollbar-thumb:hover {
                background: #5568d3;
            }

            /* ✅ RESPONSIVE: En pantallas pequeñas, columnas una debajo de otra */
            @media (max-width: 1024px) {
                .contenedor-principal {
                    flex-direction: column;
                }

                .columna-formulario {
                    flex: 1;
                    width: 100%;
                }

                .header-container {
                    flex-direction: column;
                    gap: 15px;
                    text-align: center;
                }
            }
        </style>
    </head>
    <body>
        <!-- ✅ HEADER CON DEGRADADO -->
        <div class="header-container">
            <h2>GESTIONAR USUARIOS</h2>
            <div class="btn-atras-container">
                <button type="button" onclick="window.location.href = 'MenuAdmin.jsp'">
                    Atrás
                </button>
            </div>
        </div>

        <div class="content-wrapper">
            <c:if test="${not empty requestScope.mensajeExito}">
                <div class="mensaje-exito">✅ ${requestScope.mensajeExito}</div>
            </c:if>

            <c:if test="${not empty requestScope.error}">
                <div class="mensaje-error">❌ ${requestScope.error}</div>
            </c:if>

            <!-- ✅ CONTENEDOR PRINCIPAL EN 2 COLUMNAS -->
            <div class="contenedor-principal">

                <!-- ✅ COLUMNA IZQUIERDA: FORMULARIO -->
                <div class="columna-formulario">
                    <form method="POST" action="GestionarUsuarios" id="crudForm">
                        <fieldset>
                            <legend>📝 Crear / Actualizar Usuario</legend>

                            <label>Nombres:</label> 
                            <input type="text" name="nombres" id="txtNombres" required>

                            <label>Apellidos:</label> 
                            <input type="text" name="apellidos" id="txtApellidos" required>

                            <label>Correo Electrónico:</label> 
                            <input type="email" name="correo" id="txtCorreo" 
                                   pattern="[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}"
                                   title="Ingrese un correo válido (ejemplo: usuario@dominio.com)"
                                   placeholder="ejemplo@correo.com"
                                   maxlength="100"
                                   required>

                            <label>Cargo:</label>
                            <select name="cargo" id="cbCargo" required>
                                <option value="">-- Seleccione Cargo --</option>
                                <c:forEach var="cargo" items="${requestScope.listaCargos}">
                                    <option value="${cargo}">${cargo}</option>
                                </c:forEach>
                            </select>

                            <label>Identificador:</label> 
                            <input type="text" name="identificador" id="txtIdentificador" required>

                            <label>Contraseña:</label> 
                            <input type="password" name="clave" id="txtClave" 
                                   minlength="6"
                                   maxlength="10"
                                   title="La contraseña debe tener entre 6 y 10 caracteres"
                                   required>

                            <input type="hidden" name="accion" id="accionInput">

                            <div class="botones-container">
                                <button type="button" onclick="ejecutarAccion('REGISTRAR')">➕ Registrar</button>
                                <button type="button" onclick="ejecutarAccion('ACTUALIZAR')">✏️ Actualizar</button>
                                <button type="button" onclick="ejecutarAccion('ELIMINAR')">🗑️ Eliminar</button>
                                <button type="reset" onclick="limpiarFormulario()">🔄 Limpiar</button>
                            </div>
                        </fieldset>
                    </form>
                </div>

                <!-- ✅ COLUMNA DERECHA: TABLA -->
                <div class="columna-tabla">
                    <h3>📋 Lista de Usuarios (Total: ${requestScope.listaUsuarios.size()})</h3>

                    <div style="margin-bottom: 15px; display: flex; gap: 10px; align-items: center;">
                        <input type="text" 
                               id="txtBuscar" 
                               placeholder="🔍 Buscar por nombre, apellido, correo, cargo o ID..." 
                               style="flex: 1; padding: 10px ; border: 2px solid #667eea; border-radius: 6px; font-size: 14px;">
                        <button type="button" 
                                onclick="buscarEnTabla()" 
                                style="padding: 10px 10px; background: #667eea; color: white; border: none; border-radius: 6px; cursor: pointer; font-weight: 600; transition: all 0.3s;">
                            🔍 Buscar
                        </button>
                        <button type="button" 
                                onclick="limpiarBusqueda()" 
                                style="padding: 10px 10px; background: #a8edea; color: #333; border: none; border-radius: 6px; cursor: pointer; font-weight: 600; transition: all 0.3s;">
                            🔄 Limpiar
                        </button>
                    </div>
                    <small style="color: #666; display: block; margin-bottom: 10px;">
                        Mostrando: <strong id="contadorResultados">${requestScope.listaUsuarios.size()}</strong> de ${requestScope.listaUsuarios.size()} usuarios
                    </small>


                    <c:choose>
                        <c:when test="${empty requestScope.listaUsuarios}">
                            <p style="color: orange; font-weight: bold; text-align: center; padding: 20px;">
                                ⚠️ No hay usuarios registrados en el sistema.
                            </p>
                        </c:when>
                        <c:otherwise>
                            <div class="tabla-scroll">
                                <table id="tablaUsuarios">
                                    <thead>
                                        <tr>
                                            <th>Nombres</th>
                                            <th>Apellidos</th>
                                            <th>Correo Electrónico</th>
                                            <th>Cargo</th>
                                            <th>Identificador</th>
                                            <th>Contraseña</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="u" items="${requestScope.listaUsuarios}">
                                            <tr onclick="cargarUsuario('${u.nombres}', '${u.apellidos}', '${u.correoElectronico}', '${u.tipoUsuario.cargo}', '${u.password.identificador}', '${u.password.claveAcceso}')"> 
                                                <td>${u.nombres}</td>
                                                <td>${u.apellidos}</td>
                                                <td>${u.correoElectronico}</td>
                                                <td>${u.tipoUsuario.cargo}</td>
                                                <td>${u.password.identificador}</td>
                                                <td>${u.password.claveAcceso}</td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>

            </div>
        </div>

        <script>
            function validarCorreo(correo) {
                var regex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
                return regex.test(correo);
            }

            function cargarUsuario(nombres, apellidos, correo, cargo, identificador, clave) {
                document.getElementById('txtNombres').value = nombres;
                document.getElementById('txtApellidos').value = apellidos;
                document.getElementById('txtCorreo').value = correo;
                document.getElementById('cbCargo').value = cargo;
                document.getElementById('txtIdentificador').value = identificador;
                document.getElementById('txtClave').value = clave;
                document.getElementById('txtIdentificador').readOnly = true;

                // ✅ QUITAR SELECCIÓN ANTERIOR
                var filas = document.querySelectorAll('#tablaUsuarios tbody tr');
                filas.forEach(function (fila) {
                    fila.classList.remove('seleccionada');
                });

                // ✅ MARCAR LA FILA ACTUAL COMO SELECCIONADA
                event.currentTarget.classList.add('seleccionada');

                // Scroll suave hacia el formulario
                document.querySelector('.columna-formulario').scrollIntoView({behavior: 'smooth', block: 'start'});
            }

            function limpiarFormulario() {
                document.getElementById('crudForm').reset();
                document.getElementById('txtIdentificador').readOnly = false;

                // ✅ QUITAR SELECCIÓN AL LIMPIAR
                var filas = document.querySelectorAll('#tablaUsuarios tbody tr');
                filas.forEach(function (fila) {
                    fila.classList.remove('seleccionada');
                });
            }

            function ejecutarAccion(accion) {
                var identificador = document.getElementById('txtIdentificador').value.trim();

                if (!identificador) {
                    alert("⚠️ Debe llenar el campo Identificador.");
                    document.getElementById('txtIdentificador').focus();
                    return;
                }

                if (accion === 'ELIMINAR') {
                    if (!confirm("⚠️ ¿Está seguro que desea ELIMINAR el usuario '" + identificador + "'?\n\n❌ Esta acción es IRREVERSIBLE.")) {
                        return;
                    }
                } else if (accion === 'ACTUALIZAR' || accion === 'REGISTRAR') {

                    var campos = [
                        {id: 'txtNombres', nombre: 'Nombres'},
                        {id: 'txtApellidos', nombre: 'Apellidos'},
                        {id: 'txtCorreo', nombre: 'Correo Electrónico'},
                        {id: 'cbCargo', nombre: 'Cargo'},
                        {id: 'txtClave', nombre: 'Contraseña'}
                    ];

                    for (var i = 0; i < campos.length; i++) {
                        var valor = document.getElementById(campos[i].id).value.trim();
                        if (!valor) {
                            alert("⚠️ El campo '" + campos[i].nombre + "' es obligatorio.");
                            document.getElementById(campos[i].id).focus();
                            return;
                        }
                    }

                    var correo = document.getElementById('txtCorreo').value.trim();
                    if (!validarCorreo(correo)) {
                        alert("❌ El correo electrónico no es válido.\n\nDebe tener el formato: usuario@dominio.com");
                        document.getElementById('txtCorreo').focus();
                        document.getElementById('txtCorreo').select();
                        return;
                    }

                    if (accion === 'ACTUALIZAR') {
                        if (!confirm("¿Está seguro que desea ACTUALIZAR el usuario '" + identificador + "'?")) {
                            return;
                        }
                    }
                }
                document.getElementById('accionInput').value = accion;
                document.getElementById('crudForm').submit();
            }

            // ✅ BUSCAR CON BOTÓN
            function buscarEnTabla() {
                var textoBuscar = document.getElementById('txtBuscar').value.toLowerCase().trim();
                var filas = document.querySelectorAll('#tablaUsuarios tbody tr');
                var contador = 0;

                filas.forEach(function (fila) {
                    var textoFila = fila.textContent.toLowerCase();

                    if (textoFila.includes(textoBuscar) || textoBuscar === '') {
                        fila.style.display = '';
                        contador++;
                    } else {
                        fila.style.display = 'none';
                    }
                });

                document.getElementById('contadorResultados').textContent = contador;

                if (contador === 0 && textoBuscar !== '') {
                    alert('⚠️ No se encontraron resultados para: "' + textoBuscar + '"');
                }
            }

            function limpiarBusqueda() {
                document.getElementById('txtBuscar').value = '';
                var filas = document.querySelectorAll('#tablaUsuarios tbody tr');
                var total = filas.length;

                filas.forEach(function (fila) {
                    fila.style.display = '';
                });

                document.getElementById('contadorResultados').textContent = total;
            }
// ✅ Buscar al presionar Enter
            document.addEventListener('DOMContentLoaded', function () {
                var inputBuscar = document.getElementById('txtBuscar');
                if (inputBuscar) {
                    inputBuscar.addEventListener('keypress', function (e) {
                        if (e.key === 'Enter') {
                            buscarEnTabla();
                        }
                    });
                }
            });
        </script>
    </body>
</html>