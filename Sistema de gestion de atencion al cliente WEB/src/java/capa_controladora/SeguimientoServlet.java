/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package capa_controladora;

import base_datos.ConexionBD;
import capa_modelo.Usuario;
import capa_modelo.Solicitud;
import java.io.IOException;
import java.sql.Connection;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/Seguimiento")
public class SeguimientoServlet extends HttpServlet {

    private final String ESTADO_CANCELADO_NOMBRE = "Cancelado";
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Usuario usuarioLogeado = (Usuario) request.getSession().getAttribute("usuarioActual");
        if (usuarioLogeado == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        String cargo = usuarioLogeado.getTipoUsuario().getCargo().trim();
        Connection conn = null;
        List<Solicitud> solicitudes = null;
        
        try {
            conn = ConexionBD.conectar();
            if (conn != null) {
                SeguimientoSolicitudController ssc = new SeguimientoSolicitudController();
                
                int idUsuario = ssc.obtenerIdUsuarioNumerico(usuarioLogeado.getPassword().getIdentificador());
                
                if (idUsuario == 0) throw new Exception("ID de usuario no encontrado.");

                if ("Programador".equalsIgnoreCase(cargo) || "Tecnico".equalsIgnoreCase(cargo) || "Técnico".equalsIgnoreCase(cargo)) {
                    solicitudes = ssc.obtenerSolicitudesAsignadasPorId(idUsuario);
                } else if ("Cliente".equalsIgnoreCase(cargo)) {
                    solicitudes = ssc.obtenerSolicitudesPorUsuarioId(idUsuario);
                }
                
                if (!"Cliente".equalsIgnoreCase(cargo)) {
                     request.setAttribute("listaTodosLosEstados", ssc.obtenerEstadosSolicitud());
                }
                
                request.setAttribute("listaSolicitudes", solicitudes); 
                request.getRequestDispatcher("seguimientoSolicitud.jsp").forward(request, response);
                return;
            }
        } catch (Exception e) {
            System.err.println("Error al cargar seguimiento de solicitudes: " + e.getMessage());
            request.setAttribute("error", "Error al cargar la lista de solicitudes: " + e.getMessage());
            
            String menuDestino = "Menu" + cargo + ".jsp";
            response.sendRedirect(menuDestino); 
            return;
        } finally {
            try { if (conn != null) conn.close(); } catch (Exception e) {}
        }
    }
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String numeroTicket = request.getParameter("numeroTicket");
        String nuevoEstadoStr = request.getParameter("nuevoEstado"); 
        String accion = request.getParameter("accion"); 
        
        Usuario usuarioLogeado = (Usuario) request.getSession().getAttribute("usuarioActual");
        String cargo = usuarioLogeado.getTipoUsuario().getCargo().trim();
        
        String mensaje = null;
        String estadoAUsar = null;

        try {
            SeguimientoSolicitudController ssc = new SeguimientoSolicitudController();
            
            if ("CANCELAR".equalsIgnoreCase(accion)) {
                 if (!"Cliente".equalsIgnoreCase(cargo)) throw new Exception("Permiso Denegado: Solo el cliente puede cancelar.");
                 estadoAUsar = ESTADO_CANCELADO_NOMBRE;
            
            } else if ("ACTUALIZAR".equalsIgnoreCase(accion)) {
                 if ("Cliente".equalsIgnoreCase(cargo)) throw new Exception("Permiso Denegado: Los clientes solo pueden cancelar.");
                 if (nuevoEstadoStr == null || nuevoEstadoStr.isEmpty() || nuevoEstadoStr.equals("Seleccione Estado")) throw new Exception("Debe seleccionar un nuevo estado.");
                 estadoAUsar = nuevoEstadoStr;
            } else {
                 throw new Exception("Acción no válida.");
            }
            
            int idSolicitud = ssc.obtenerIdSolicitudPorTicket(numeroTicket); 
            
            boolean exito = ssc.actualizarEstadoSolicitud(idSolicitud, ssc.obtenerIdEstadoPorNombre(estadoAUsar)); 
            
            if (exito) {
                mensaje = "El estado del Ticket #" + numeroTicket + " ha sido actualizado a: " + estadoAUsar;
                request.getSession().setAttribute("mensajeExito", mensaje);
            } else {
                throw new Exception("Fallo al actualizar el estado en la base de datos.");
            }

        } catch (Exception e) {
            System.err.println("Error al procesar acción: " + e.getMessage());
            request.getSession().setAttribute("error", "Error al procesar la acción: " + e.getMessage());
        }

        response.sendRedirect("Seguimiento");
    }
}