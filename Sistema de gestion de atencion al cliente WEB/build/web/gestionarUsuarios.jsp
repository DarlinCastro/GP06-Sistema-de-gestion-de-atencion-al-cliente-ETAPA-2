<%-- 
    Document   : gestionarUsuarios
    Created on : 12 nov 2025, 01:28:06
    Author     : DELL
--%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>GESTIONAR USUARIOS - Web</title>
</head>
<body>
    
    <h2>GESTIONAR USUARIOS</h2>
    <p><a href="MenuAdmin.jsp">Atrás</a></p>

    <p style="color: green; font-weight: bold;">${requestScope.mensajeExito}</p>
    <p style="color: red; font-weight: bold;">${requestScope.error}</p>

    <form method="POST" action="GestionarUsuarios" id="crudForm">
        <fieldset>
            <legend>Crear / Actualizar Usuario</legend>
            
            <label>Nombres:</label> <input type="text" name="nombres" id="txtNombres" required><br>
            <label>Apellidos:</label> <input type="text" name="apellidos" id="txtApellidos" required><br>
            <label>Correo Electrónico:</label> <input type="email" name="correo" id="txtCorreo" required><br>
            <label>Cargo:</label>
            <select name="cargo" id="cbCargo" required>
                <option value="">-- Seleccione Cargo --</option>
                <c:forEach var="cargo" items="${requestScope.listaCargos}">
                    <option value="${cargo}">${cargo}</option>
                </c:forEach>
            </select><br>

            <label>Identificador:</label> <input type="text" name="identificador" id="txtIdentificador" required><br>
            <label>Contraseña:</label> <input type="password" name="clave" id="txtClave" required><br><br>
            
            <input type="hidden" name="accion" id="accionInput">

            <button type="button" onclick="ejecutarAccion('REGISTRAR')">Registrar</button>
            <button type="button" onclick="ejecutarAccion('ACTUALIZAR')" id="btnActualizar">Actualizar</button>
            <button type="button" onclick="ejecutarAccion('ELIMINAR')" id="btnEliminar">Eliminar</button>
            <button type="reset" onclick="limpiarFormulario()">Limpiar</button>
        </fieldset>
    </form>
    
    <hr>

    <h3>Lista de Usuarios</h3>
    <table border="1" id="tablaUsuarios">
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
                <tr onclick="cargarUsuarioParaEdicion(
                                 '${u.nombres}', 
                                 '${u.apellidos}', 
                                 '${u.correoElectronico}', 
                                 '${u.tipoUsuario.cargo}', 
                                 '${u.password.identificador}', 
                                 '${u.password.claveAcceso}')"> 
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
    
    <script>
        function cargarUsuarioParaEdicion(nombres, apellidos, correo, cargo, identificador, clave) {
            document.getElementById('txtNombres').value = nombres;
            document.getElementById('txtApellidos').value = apellidos;
            document.getElementById('txtCorreo').value = correo;
            document.getElementById('cbCargo').value = cargo;
            document.getElementById('txtIdentificador').value = identificador;
            document.getElementById('txtClave').value = clave;
            
            // Deshabilitar el campo identificador para las acciones de Actualizar/Eliminar
            document.getElementById('txtIdentificador').readOnly = true;
        }

        function limpiarFormulario() {
            document.getElementById('crudForm').reset();
            document.getElementById('txtIdentificador').readOnly = false;
        }

        function ejecutarAccion(accion) {
            var identificador = document.getElementById('txtIdentificador').value;
            
            if (!identificador) {
                alert("Debe llenar el campo Identificador y seleccionar una fila o llenar los datos para la acción.");
                return;
            }

            if (accion === 'ELIMINAR') {
                if (!confirm("¿Está seguro que desea ELIMINAR el usuario " + identificador + "? Esta acción es irreversible.")) {
                    return;
                }
            } else if (accion === 'ACTUALIZAR') {
                if (!confirm("¿Está seguro que desea ACTUALIZAR el usuario " + identificador + "?")) {
                    return;
                }
            }
            
            // Poner la acción en el campo oculto y enviar el formulario
            document.getElementById('accionInput').value = accion;
            document.getElementById('crudForm').submit();
        }
    </script>
</body>
</html>