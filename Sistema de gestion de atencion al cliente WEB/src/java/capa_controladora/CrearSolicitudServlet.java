/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package capa_controladora;

import base_datos.ConexionBD;
import capa_modelo.Usuario;
import capa_modelo.TipoServicio;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/CrearSolicitud")
public class CrearSolicitudServlet extends HttpServlet {

    // GET se mantiene para cargar los tipos de servicio
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        if (request.getSession().getAttribute("usuarioActual") == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        try {
            GenerarReporteController grc = new GenerarReporteController(); 
            List<TipoServicio> listaServicios = grc.cargarTiposServicio();
            
            // Manejo de mensajes de error/éxito de un POST previo (PRG)
            String errorMsg = (String) request.getAttribute("error");
            if (errorMsg == null) {
                // Si el error no viene del POST, revisamos la sesión por si viene de una redirección
                errorMsg = (String) request.getSession().getAttribute("error");
                if (errorMsg != null) {
                    request.getSession().removeAttribute("error");
                }
            }
            if (errorMsg != null) {
                request.setAttribute("error", errorMsg);
            }
            
            request.setAttribute("listaServicios", listaServicios); 
            request.getRequestDispatcher("crearSolicitud.jsp").forward(request, response);
            return;
        } catch (Exception e) {
            System.err.println("Error al cargar Tipos de Servicio: " + e.getMessage());
            request.setAttribute("error", "Error al cargar tipos de servicio: " + e.getMessage());
            request.getRequestDispatcher("MenuCliente.jsp").forward(request, response);
            return;
        }
    }
    
    // POST maneja la transacción
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String tipoServicioStr = request.getParameter("tipoServicio");
        String descripcion = request.getParameter("descripcion");
        
        Usuario usuarioActual = (Usuario) request.getSession().getAttribute("usuarioActual");

        String mensaje = null;
        Connection conn = null;
        
        if (usuarioActual == null) { response.sendRedirect("login.jsp"); return; }
        
        if (tipoServicioStr == null || tipoServicioStr.isEmpty() || tipoServicioStr.equals("-- Seleccione Tipo de Servicio --") || descripcion.isEmpty()) {
            request.setAttribute("error", "Error: Faltan datos (Tipo de Servicio o Descripción).");
            doGet(request, response); 
            return;
        }

        try {
            conn = ConexionBD.conectar();
            if (conn != null) {
                conn.setAutoCommit(false); 
                
                TipoServicio tipoServicio = new TipoServicio(tipoServicioStr);
                CrearSolicitudController csc = new CrearSolicitudController();
                
                String numeroTicket = csc.crearSolicitudConTicket(conn, usuarioActual, tipoServicio, descripcion);
                
                if (numeroTicket != null) {
                    conn.commit(); 
                    mensaje = "¡Solicitud creada! Su número de ticket es: " + numeroTicket;
                    request.getSession().setAttribute("mensajeExito", mensaje); 
                    response.sendRedirect("MenuCliente.jsp"); 
                    return;
                } else {
                    // Si retorna null sin lanzar excepción (lo cual ya corregimos)
                    throw new Exception("Error inesperado al obtener el número de ticket.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error de SQL al crear solicitud: " + e.getMessage());
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) { /* ignorar */ }
            request.getSession().setAttribute("error", "Error de base de datos al crear solicitud: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error al crear solicitud: " + e.getMessage());
            request.getSession().setAttribute("error", "Error al crear solicitud: " + e.getMessage());
        } finally {
            try { 
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close(); 
                }
            } catch (SQLException e) {
                System.err.println("Error al cerrar la conexión: " + e.getMessage());
            }
        }
        // Redirigir al GET para recargar la página a través del PRG
        response.sendRedirect("CrearSolicitud");
    }
}