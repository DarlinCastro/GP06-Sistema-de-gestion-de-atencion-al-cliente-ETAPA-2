/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package capa_controladora;

import base_datos.ConexionBD;
import capa_modelo.Usuario;
import java.io.IOException;
import java.sql.Connection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

// @WebServlet define la URL de acceso: http://localhost:8080/TuApp/Login
@WebServlet("/Login") 
public class LoginServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String identificador = request.getParameter("identificador");
        String clave = request.getParameter("clave");
        
        Usuario usuarioLogeado = null;
        Connection conn = null;

        try {
            conn = ConexionBD.conectar();
            if (conn != null) {
                UsuarioController uc = new UsuarioController(conn);
                usuarioLogeado = uc.obtenerUsuarioPorCredenciales(identificador, clave);
            }
        } catch (Exception e) {
            request.setAttribute("error", "Error interno al procesar la solicitud.");
            request.getRequestDispatcher("login.jsp").forward(request, response);
            return;
        } finally {
            try { if (conn != null) conn.close(); } catch (Exception e) {}
        }

        if (usuarioLogeado != null) {
            HttpSession session = request.getSession();
            session.setAttribute("usuarioActual", usuarioLogeado); 

            String cargo = usuarioLogeado.getTipoUsuario().getCargo().trim();
            
            if ("Admin".equalsIgnoreCase(cargo)) {
                response.sendRedirect("MenuAdmin.jsp");
            } else if ("Cliente".equalsIgnoreCase(cargo)) {
                response.sendRedirect("MenuCliente.jsp");
            } else if ("Técnico".equalsIgnoreCase(cargo)) {
                response.sendRedirect("MenuTecnico.jsp");
            } else if ("Programador".equalsIgnoreCase(cargo)) {
                response.sendRedirect("MenuTecnico.jsp");
            } 
            else {
                request.setAttribute("error", "Error: Rol de usuario no reconocido.");
                request.getRequestDispatcher("login.jsp").forward(request, response);
            }
            
        } else {
            request.setAttribute("error", "Identificador o Contraseña incorrectos.");
            request.getRequestDispatcher("login.jsp").forward(request, response); 
        }
    }
}