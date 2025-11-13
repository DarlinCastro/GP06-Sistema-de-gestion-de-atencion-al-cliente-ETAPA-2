/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package capa_controladora;

import base_datos.ConexionBD;
import capa_modelo.Solicitud;
import capa_modelo.Usuario;
import capa_modelo.Ticket;
import capa_modelo.EstadoTicket;
import capa_modelo.EstadoSolicitud;

import java.io.IOException;
import java.sql.Connection;
import java.util.List;
import java.util.Date;
import java.text.SimpleDateFormat;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/Asignacion")
public class AsignacionServlet extends HttpServlet {
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (request.getSession().getAttribute("usuarioActual") == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        Connection conn = null;
        try {
            AsignacionController ac = new AsignacionController(); 
            request.setAttribute("listaCargos", ac.obtenerCargos()); 
            request.setAttribute("listaPrioridades", ac.obtenerPrioridades());
            request.setAttribute("listaEstados", ac.obtenerEstadosSolicitud());

            List<Solicitud> solicitudes = ac.obtenerTodasSolicitudes(); 
            request.setAttribute("listaSolicitudes", solicitudes); 

            request.getRequestDispatcher("asignarSolicitud.jsp").forward(request, response);
            return;
            
        } catch (Exception e) {
            System.err.println("Error al cargar datos de Asignación: " + e.getMessage());
            request.setAttribute("error", "Error al cargar datos iniciales: " + e.getMessage());
            // Si falla, volvemos al menú
            request.getRequestDispatcher("MenuAdmin.jsp").forward(request, response); 
            return;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String numeroTicket = request.getParameter("numeroTicket");
        String prioridad = request.getParameter("nivelPrioridad");
        String estado = request.getParameter("estadoSolicitud");
        String tecnicoNombreCompleto = request.getParameter("tecnicoNombre");
        String fechaAsignacionStr = request.getParameter("fechaAsignacion"); 

        String mensaje = null;
        
        try {
            AsignacionController ac = new AsignacionController();
            if (numeroTicket == null || numeroTicket.isEmpty() || numeroTicket.equals("-- Seleccione Ticket --") ||
                prioridad == null || prioridad.isEmpty() ||
                estado == null || estado.isEmpty() ||
                tecnicoNombreCompleto == null || tecnicoNombreCompleto.isEmpty()) {
                
                throw new Exception("Faltan datos de selección: Ticket, Prioridad, Estado o Técnico.");
            }
            Usuario tecnicoAsignado = ac.buscarTecnicoPorNombre(tecnicoNombreCompleto);
            if (tecnicoAsignado == null) {
                throw new Exception("Error: Técnico asignado no encontrado. Recargue la página.");
            }
            EstadoTicket nivelPrioridad = new EstadoTicket(prioridad); 
            EstadoSolicitud nuevoEstado = new EstadoSolicitud(estado); 
            Date fechaAsignacion = dateFormat.parse(fechaAsignacionStr); 
            Solicitud solicitudAActualizar = new Solicitud();
            Ticket ticket = new Ticket(nivelPrioridad, fechaAsignacion, numeroTicket, tecnicoAsignado);
            solicitudAActualizar.setTicket(ticket);
            solicitudAActualizar.setEstadoSolicitud(nuevoEstado); 

            boolean exito = ac.ejecutarAsignacion(solicitudAActualizar); 
            
            if (exito) {
                mensaje = "Ticket " + numeroTicket + " asignado/actualizado correctamente.";
                request.setAttribute("mensajeExito", mensaje);
            } else {
                throw new Exception("Error desconocido al persistir la asignación."); 
            }
            
        } catch (Exception e) {
            System.err.println("Error de asignación: " + e.getMessage());
            request.setAttribute("error", "Error al procesar la asignación: " + e.getMessage());
        }

        response.sendRedirect("Asignacion");
    }
}