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
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/CrearCuenta") 
public class CrearCuentaServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String nombre = request.getParameter("nombres");
        String apellido = request.getParameter("apellidos");
        String correo = request.getParameter("correo");
        String identificador = request.getParameter("identificador");
        String clave = request.getParameter("clave");
        
        String mensaje = "";
        String destino = "crearCuenta.jsp"; 
        Connection conn = null;

        try {
            Password password = new Password(clave, identificador);
            TipoUsuario tipoUsuario = new TipoUsuario("Cliente"); 
            
            Usuario nuevoUsuario = new Usuario(nombre, apellido, correo, tipoUsuario, password);
            
            conn = ConexionBD.conectar();
            if (conn != null) {
                CrearCuentaController ccc = new CrearCuentaController(); 
                boolean creado = ccc.registrarUsuario(nuevoUsuario, clave, identificador); 
                
                if (creado) {
                    mensaje = "La cuenta ha sido creada exitosamente. Inicie sesión.";
                    destino = "login.jsp"; 
                } else {
                    mensaje = "Error: El registro falló. Es posible que el identificador ya exista o la información sea inválida.";
                }
            } else {
                 mensaje = "Error: No se pudo establecer conexión con la Base de Datos.";
            }
        } catch (Exception e) {
            System.err.println("Error en la creación de cuenta: " + e.getMessage());
            mensaje = "Error interno del servidor: " + e.getMessage();
        } finally {
            try { 
                if (conn != null) conn.close(); 
            } catch (Exception e) {
                System.err.println("Error al cerrar conexión: " + e.getMessage());
            }
        }

        request.setAttribute("mensaje", mensaje);
        request.getRequestDispatcher(destino).forward(request, response);
    }
}