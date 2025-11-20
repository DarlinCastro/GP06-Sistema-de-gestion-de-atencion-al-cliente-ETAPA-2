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
import java.util.Map;
import java.util.HashMap;
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

        // Configurar codificación UTF-8
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        if (request.getSession().getAttribute("usuarioActual") == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        try {
            AsignacionController ac = new AsignacionController();

            // Cargar todos los datos necesarios
            List<String> cargos = ac.obtenerCargos();
            request.setAttribute("listaCargos", cargos);
            request.setAttribute("listaPrioridades", ac.obtenerPrioridades());
            request.setAttribute("listaEstados", ac.obtenerEstadosSolicitud());
            request.setAttribute("listaTickets", ac.obtenerTodosNumerosTicket());

            // Cargar técnicos agrupados por cargo
            Map<String, List<String>> tecnicosPorCargo = new HashMap<>();
            for (String cargo : cargos) {
                List<String> tecnicos = ac.obtenerNombresTecnicosPorCargo(cargo);
                tecnicosPorCargo.put(cargo, tecnicos);
            }
            request.setAttribute("tecnicosPorCargo", tecnicosPorCargo);

            List<Solicitud> solicitudes = ac.obtenerTodasSolicitudes();
            request.setAttribute("listaSolicitudes", solicitudes);

            request.getRequestDispatcher("asignarSolicitud.jsp").forward(request, response);
            return;

        } catch (Exception e) {
            System.err.println("Error al cargar datos de Asignación: " + e.getMessage());
            request.setAttribute("error", "Error al cargar datos iniciales: " + e.getMessage());
            request.getRequestDispatcher("MenuAdmin.jsp").forward(request, response);
            return;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // IMPORTANTE: Configurar codificación UTF-8
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        String numeroTicket = request.getParameter("numeroTicket");
        String prioridad = request.getParameter("nivelPrioridad");
        String estado = request.getParameter("estadoSolicitud");
        String tecnicoNombreCompleto = request.getParameter("tecnicoNombre");
        String fechaAsignacionStr = request.getParameter("fechaAsignacion");

        System.out.println("DEBUG POST: Técnico recibido = '" + tecnicoNombreCompleto + "'");

        String mensaje = null;

        try {
            AsignacionController ac = new AsignacionController();

            // Validaciones
            if (numeroTicket == null || numeroTicket.isEmpty()
                    || prioridad == null || prioridad.isEmpty()
                    || estado == null || estado.isEmpty()
                    || tecnicoNombreCompleto == null || tecnicoNombreCompleto.isEmpty()) {

                throw new Exception("Faltan datos de selección: Ticket, Prioridad, Estado o Técnico.");
            }

            // Cargar todas las solicitudes y TODOS los técnicos para buscar
            ac.obtenerTodasSolicitudes();

            // Cargar técnicos de todos los cargos para poder buscar
            List<String> cargos = ac.obtenerCargos();
            for (String cargo : cargos) {
                ac.obtenerNombresTecnicosPorCargo(cargo);
            }

            Usuario tecnicoAsignado = ac.buscarTecnicoPorNombre(tecnicoNombreCompleto);
            if (tecnicoAsignado == null) {
                throw new Exception("Error: Técnico '" + tecnicoNombreCompleto + "' no encontrado. Recargue la página.");
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
                request.getSession().setAttribute("mensajeExito", mensaje);
            } else {
                throw new Exception("Error desconocido al persistir la asignación.");
            }

        } catch (Exception e) {
            System.err.println("Error de asignación: " + e.getMessage());
            e.printStackTrace();
            request.getSession().setAttribute("error", "Error al procesar la asignación: " + e.getMessage());
        }

        response.sendRedirect("Asignacion");
    }
}
