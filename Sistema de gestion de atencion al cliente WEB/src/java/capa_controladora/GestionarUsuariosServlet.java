/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package capa_controladora;

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

@WebServlet("/GestionarUsuarios")
public class GestionarUsuariosServlet extends HttpServlet {

    // Método para validar formato de correo electrónico
    private boolean esCorreoValido(String correo) {
        if (correo == null || correo.trim().isEmpty()) {
            return false;
        }
        // Expresión regular para validar correo
        String regex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        return correo.matches(regex);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (request.getSession().getAttribute("usuarioActual") == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        System.out.println("🔹 GET GestionarUsuariosServlet - INICIO");

        String mensajeExito = (String) request.getSession().getAttribute("mensajeExito");
        String mensajeError = (String) request.getSession().getAttribute("error");

        if (mensajeExito != null) {
            request.setAttribute("mensajeExito", mensajeExito);
            request.getSession().removeAttribute("mensajeExito");
        }
        if (mensajeError != null) {
            request.setAttribute("error", mensajeError);
            request.getSession().removeAttribute("error");
        }

        try {
            GestionarUsuariosController guc = new GestionarUsuariosController();

            // Obtener lista de usuarios
            List<Usuario> listaUsuarios = guc.obtenerUsuarios();
            System.out.println("📋 Usuarios obtenidos: " + listaUsuarios.size());
            request.setAttribute("listaUsuarios", listaUsuarios);

            // Obtener lista de cargos
            List<String> listaCargos = guc.obtenerTiposUsuarioParaCargos();
            System.out.println("📋 Cargos obtenidos: " + listaCargos.size());
            System.out.println("📋 Cargos: " + listaCargos);

            // CRÍTICO: Asignar al request
            request.setAttribute("listaCargos", listaCargos);

            System.out.println("🔹 Redirigiendo a gestionarUsuarios.jsp");

            // Enviar a JSP
            request.getRequestDispatcher("gestionarUsuarios.jsp").forward(request, response);

        } catch (Exception e) {
            System.err.println("❌ Error al cargar la gestión de usuarios: " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("error", "Error al cargar datos de gestión: " + e.getMessage());
            request.getRequestDispatcher("MenuAdmin.jsp").forward(request, response);
        }
    }

    /*
        try {
            GestionarUsuariosController guc = new GestionarUsuariosController();
            List<Usuario> listaUsuarios = guc.obtenerUsuarios();
            request.setAttribute("listaUsuarios", listaUsuarios);

            List<String> listaCargos = guc.obtenerTiposUsuarioParaCargos();
            request.setAttribute("listaCargos", listaCargos);

            request.getRequestDispatcher("gestionarUsuarios.jsp").forward(request, response);

        } catch (Exception e) {
            System.err.println("Error al cargar la gestión de usuarios: " + e.getMessage());
            request.setAttribute("error", "Error al cargar datos de gestión: " + e.getMessage());
            request.getRequestDispatcher("MenuAdmin.jsp").forward(request, response);
        }
    }
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String accion = request.getParameter("accion");
        String mensaje = null;

        String nombres = request.getParameter("nombres");
        String apellidos = request.getParameter("apellidos");
        String correo = request.getParameter("correo");
        String identificador = request.getParameter("identificador");
        String clave = request.getParameter("clave");
        String cargo = request.getParameter("cargo");

        try {
            GestionarUsuariosController guc = new GestionarUsuariosController();

            if ("ELIMINAR".equalsIgnoreCase(accion)) {

                if (identificador == null || identificador.isEmpty()) {
                    throw new Exception("Identificador es requerido para la eliminación.");
                }
                guc.eliminarUsuario(identificador);
                mensaje = "Usuario con identificador " + identificador + " eliminado con éxito.";

            } else {

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
                if (correo.length() > 25) {
                    throw new Exception("El correo electrónico es demasiado largo. Máximo 100 caracteres.");
                }

                Password pass = new Password(clave, identificador);
                TipoUsuario tipo = new TipoUsuario(cargo);
                Usuario usuario = new Usuario(nombres, apellidos, correo, tipo, pass);

                if ("REGISTRAR".equalsIgnoreCase(accion)) {
                    //Validar identificador único
                    if (guc.existeIdentificador(identificador)) {
                        throw new Exception("El identificador '" + identificador + "' ya existe. Elija otro.");
                    }
                    guc.agregarUsuario(usuario);
                    mensaje = "Usuario registrado exitosamente.";

                } else if ("ACTUALIZAR".equalsIgnoreCase(accion)) {
                    guc.actualizarUsuario(usuario);
                    mensaje = "Usuario actualizado exitosamente.";

                } else {
                    throw new Exception("Acción de formulario no válida.");
                }
            }
            request.getSession().setAttribute("mensajeExito", mensaje);

        } catch (Exception e) {
            System.err.println("Error en la gestión de usuarios (" + accion + "): " + e.getMessage());
            request.getSession().setAttribute("error", "Error en la gestión (" + accion + "): " + e.getMessage());
        }

        response.sendRedirect("GestionarUsuarios");
    }
}
