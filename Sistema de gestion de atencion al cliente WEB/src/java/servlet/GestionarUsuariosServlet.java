/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package servlet;

import base_datos.ConexionBD;

import capa_modelo.Usuario;
import capa_modelo.Password;
import capa_modelo.TipoUsuario;

import capa_controladora.GestionarUsuariosController;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Servlet (Controlador) que maneja las solicitudes relacionadas con la
 * administración (CRUD) de usuarios. Mapea la URL "/GestionarUsuarios" para
 * gestionar tanto la visualización (GET) como las acciones de gestión (POST).
 */
@WebServlet("/GestionarUsuarios")
public class GestionarUsuariosServlet extends HttpServlet {

    /**
     * Método auxiliar privado para validar el formato básico de un correo
     * electrónico mediante una Expresión Regular (Regex).
     *
     * @param correo La cadena a validar.
     * @return true si el formato es válido, false en caso contrario.
     */
    private boolean esCorreoValido(String correo) {
        if (correo == null || correo.trim().isEmpty()) {
            return false;
        }
        // Expresión regular que verifica el formato: texto@texto.dominio (mínimo 2 letras en el dominio)
        String regex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        return correo.matches(regex);
    }

    /**
     * Maneja las peticiones HTTP GET. Se utiliza para: 1. Cargar la vista
     * inicial de gestión de usuarios. 2. Obtener y mostrar la lista actual de
     * todos los usuarios. 3. Obtener la lista de cargos (TipoUsuario) para los
     * formularios. 4. Mostrar mensajes de éxito o error resultantes de una
     * operación POST previa.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // *** CONFIGURACIÓN DE ENCODING UTF-8 ***
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");

        // --- 1. Control de Sesión ---
        // Verifica si el usuario está autenticado antes de permitir el acceso.
        if (request.getSession().getAttribute("usuarioActual") == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        System.out.println("🔹 GET GestionarUsuariosServlet - INICIO");

        // --- 2. Manejo de Mensajes de Sesión ---
        // Recupera mensajes de éxito o error que fueron guardados en la sesión
        // por un POST anterior (patrón Post-Redirect-Get).
        String mensajeExito = (String) request.getSession().getAttribute("mensajeExito");
        String mensajeError = (String) request.getSession().getAttribute("error");

        if (mensajeExito != null) {
            request.setAttribute("mensajeExito", mensajeExito);
            request.getSession().removeAttribute("mensajeExito"); // Limpia la sesión
        }
        if (mensajeError != null) {
            request.setAttribute("error", mensajeError);
            request.getSession().removeAttribute("error"); // Limpia la sesión
        }

        // --- 3. Carga de Datos ---
        try {
            GestionarUsuariosController guc = new GestionarUsuariosController();

            // Obtener lista de usuarios
            List<Usuario> listaUsuarios = guc.obtenerUsuarios();
            System.out.println("Usuarios obtenidos: " + listaUsuarios.size());
            request.setAttribute("listaUsuarios", listaUsuarios);

            // Obtener lista de cargos (Tipos de Usuario) para el dropdown/select del formulario
            List<String> listaCargos = guc.obtenerTiposUsuarioParaCargos();
            System.out.println("Cargos obtenidos: " + listaCargos.size());
            System.out.println("Cargos: " + listaCargos);

            // CRÍTICO: Asignar al request para que el JSP pueda iterar sobre ellos.
            request.setAttribute("listaCargos", listaCargos);

            System.out.println("Redirigiendo a gestionarUsuarios.jsp");

            // Enviar a JSP (forward)
            request.getRequestDispatcher("gestionarUsuarios.jsp").forward(request, response);

        } catch (Exception e) {
            // --- 4. Manejo de Errores Críticos ---
            // Si falla la carga inicial de datos (ej. problema de conexión a DB),
            // se registra el error y se redirige al menú de administrador.
            System.err.println("Error al cargar la gestión de usuarios: " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("error", "Error al cargar datos de gestión: " + e.getMessage());
            request.getRequestDispatcher("MenuAdmin.jsp").forward(request, response);
        }
    }

    /**
     * Maneja las peticiones HTTP POST. Se utiliza para ejecutar las acciones de
     * gestión de usuarios (Registrar, Actualizar, Eliminar).
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // *** CONFIGURACIÓN DE ENCODING UTF-8 (CRÍTICO: ANTES de leer parámetros) ***
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        // Obtener el tipo de acción a realizar desde el formulario.
        String accion = request.getParameter("accion");
        String mensaje = null; // Mensaje de éxito/error a mostrar tras la redirección.

        // --- 1. Captura de Parámetros del Formulario ---
        String nombres = request.getParameter("nombres");
        String apellidos = request.getParameter("apellidos");
        String correo = request.getParameter("correo");
        String identificador = request.getParameter("identificador");
        String clave = request.getParameter("clave");
        String cargo = request.getParameter("cargo");

        try {
            GestionarUsuariosController guc = new GestionarUsuariosController();

            // --- 2. Lógica de Eliminación ---
            if ("ELIMINAR".equalsIgnoreCase(accion)) {

                if (identificador == null || identificador.isEmpty()) {
                    throw new Exception("Identificador es requerido para la eliminación.");
                }
                // Delegar la eliminación al controlador de negocio
                guc.eliminarUsuario(identificador);
                mensaje = "Usuario con identificador " + identificador + " eliminado con éxito.";

                // --- 3. Lógica de Registro y Actualización ---
            } else {

                // Validación de campos obligatorios para AGREGAR/ACTUALIZAR
                if (nombres == null || nombres.isEmpty()
                        || apellidos == null || apellidos.isEmpty()
                        || correo == null || correo.isEmpty()
                        || identificador == null || identificador.isEmpty()
                        || clave == null || clave.isEmpty()
                        || cargo == null || cargo.isEmpty()) {
                    throw new Exception("Todos los campos (Nombres, Apellidos, Correo, Identificador, Clave, Cargo) son requeridos para la acción.");
                }

                // VALIDACIÓN DEL FORMATO DE CORREO
                if (!esCorreoValido(correo)) {
                    throw new Exception("El correo electrónico no tiene un formato válido. Debe incluir '@' y un dominio (ejemplo: usuario@dominio.com)");
                }
                // VALIDACIÓN DE LONGITUD DE CORREO
                if (correo.length() > 100) {
                    throw new Exception("El correo electrónico es demasiado largo. Máximo 100 caracteres.");
                }

                // Creación de objetos del modelo (DTOs)
                Password pass = new Password(clave, identificador);
                TipoUsuario tipo = new TipoUsuario(cargo);
                // El objeto Usuario encapsula todos los datos
                Usuario usuario = new Usuario(nombres, apellidos, correo, tipo, pass);

                if ("REGISTRAR".equalsIgnoreCase(accion)) {
                    // Validar identificador único antes de registrar
                    if (guc.existeIdentificador(identificador)) {
                        throw new Exception("El identificador '" + identificador + "' ya existe. Elija otro.");
                    }
                    // Delegar el registro al controlador de negocio
                    guc.agregarUsuario(usuario);
                    mensaje = "Usuario registrado exitosamente.";

                } else if ("ACTUALIZAR".equalsIgnoreCase(accion)) {
                    // Delegar la actualización al controlador de negocio
                    guc.actualizarUsuario(usuario);
                    mensaje = "Usuario actualizado exitosamente.";

                } else {
                    // Si la acción no es reconocida
                    throw new Exception("Acción de formulario no válida.");
                }
            }
            // --- 4. Post-Redirect-Get (Éxito) ---
            // Si la operación fue exitosa, se guarda el mensaje en la sesión
            // y se redirige al mismo servlet (a su doGet) para evitar reenvío de formulario.
            request.getSession().setAttribute("mensajeExito", mensaje);

        } catch (Exception e) {
            // --- 5. Post-Redirect-Get (Error) ---
            // Si ocurre una excepción (validación fallida o error de DB/lógica),
            // se guarda el error en la sesión y se redirige al doGet.
            System.err.println("Error en la gestión de usuarios (" + accion + "): " + e.getMessage());
            request.getSession().setAttribute("error", "Error en la gestión (" + accion + "): " + e.getMessage());
        }

        // Redirección final al doGet del mismo servlet para recargar la lista de usuarios.
        response.sendRedirect("GestionarUsuarios");
    }
}
