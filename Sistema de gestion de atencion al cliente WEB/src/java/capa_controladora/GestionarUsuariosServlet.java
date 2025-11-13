/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package capa_controladora;

import base_datos.ConexionBD;
import capa_modelo.Usuario;
import capa_modelo.Password;
import capa_modelo.TipoUsuario;
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

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (request.getSession().getAttribute("usuarioActual") == null) {
            response.sendRedirect("login.jsp");
            return;
        }

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
                
                if (identificador == null || identificador.isEmpty()) throw new Exception("Identificador es requerido para la eliminación.");
                guc.eliminarUsuario(identificador); 
                mensaje = "Usuario con identificador " + identificador + " eliminado con éxito.";

            } else {
                
                if (nombres == null || nombres.isEmpty() || apellidos == null || apellidos.isEmpty() || correo == null || correo.isEmpty() || identificador == null || identificador.isEmpty() || clave == null || clave.isEmpty() || cargo == null || cargo.isEmpty()) {
                    throw new Exception("Todos los campos (Nombres, Apellidos, Correo, Identificador, Clave, Cargo) son requeridos para la acción.");
                }

                Password pass = new Password(clave, identificador);
                TipoUsuario tipo = new TipoUsuario(cargo);
                Usuario usuario = new Usuario(nombres, apellidos, correo, tipo, pass);

                if ("REGISTRAR".equalsIgnoreCase(accion)) {
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