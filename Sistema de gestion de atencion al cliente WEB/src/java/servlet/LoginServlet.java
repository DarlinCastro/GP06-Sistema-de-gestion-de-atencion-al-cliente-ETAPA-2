/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package servlet;

import base_datos.ConexionBD;

import capa_controladora.UsuarioController;

import capa_modelo.Usuario;

import java.io.IOException;
import java.sql.Connection;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Servlet (Controlador) encargado de procesar la autenticación de usuarios.
 * Recibe las credenciales (Identificador y Clave) del formulario de login y, si
 * son válidas, crea una sesión y redirige al menú correspondiente al rol.
 */
// @WebServlet define la URL de acceso: http://localhost:8080/TuApp/Login
@WebServlet("/Login")
public class LoginServlet extends HttpServlet {

    /**
     * Maneja las peticiones HTTP POST, que son las que se envían al intentar
     * loguearse.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Obtener parámetros del formulario de login.
        String identificador = request.getParameter("identificador");
        String clave = request.getParameter("clave");

        Usuario usuarioLogeado = null;
        Connection conn = null;

        // 2. Intentar la autenticación (Delegar la lógica de negocio y DB).
        try {
            // Establecer conexión con la Base de Datos.
            conn = ConexionBD.conectar();

            if (conn != null) {
                // Instanciar el Controlador de negocio, pasándole la conexión activa.
                UsuarioController uc = new UsuarioController(conn);
                // Intentar obtener el usuario validando las credenciales.
                usuarioLogeado = uc.obtenerUsuarioPorCredenciales(identificador, clave);
            }
        } catch (Exception e) {
            // Manejo de errores críticos de conexión o DB.
            System.err.println("Error de conexión o DB durante el login: " + e.getMessage());
            request.setAttribute("error", "Error interno al procesar la solicitud.");
            // Redirigir de vuelta a la página de login con un mensaje de error.
            request.getRequestDispatcher("login.jsp").forward(request, response);
            return;
        } finally {
            // 3. Asegurar el cierre de la conexión, sin importar el resultado.
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception e) {
            }
        }

        // 4. Lógica de Resultado de Autenticación.
        if (usuarioLogeado != null) {
            // --- Credenciales Válidas ---

            // Crear o recuperar la sesión HTTP del usuario.
            HttpSession session = request.getSession();
            // Almacenar el objeto Usuario en la sesión para mantener el estado de login.
            session.setAttribute("usuarioActual", usuarioLogeado);

            // Obtener el cargo (rol) del usuario logueado, eliminando posibles espacios.
            String cargo = usuarioLogeado.getTipoUsuario().getCargo().trim();

            // 5. Redirección basada en el Rol (Autorización).
            if ("Admin".equalsIgnoreCase(cargo)) {
                response.sendRedirect("MenuAdmin.jsp");
            } else if ("Cliente".equalsIgnoreCase(cargo)) {
                response.sendRedirect("MenuCliente.jsp");
            } // Técnico y Programador comparten el mismo menú.
            else if ("Técnico".equalsIgnoreCase(cargo) || "Programador".equalsIgnoreCase(cargo)) {
                response.sendRedirect("MenuTecnico.jsp");
            } else {
                // Si el rol es desconocido o no está mapeado.
                request.setAttribute("error", "Error: Rol de usuario no reconocido.");
                request.getRequestDispatcher("login.jsp").forward(request, response);
            }

        } else {
            // --- Credenciales Inválidas ---

            // Establecer mensaje de error.
            request.setAttribute("error", "Identificador o Contraseña incorrectos.");
            // Reenviar al formulario de login para mostrar el error.
            request.getRequestDispatcher("login.jsp").forward(request, response);
        }
    }
}
