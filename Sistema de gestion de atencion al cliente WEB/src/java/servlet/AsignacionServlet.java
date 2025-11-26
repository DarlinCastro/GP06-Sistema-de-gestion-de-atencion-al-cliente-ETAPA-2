/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package servlet;

import base_datos.ConexionBD;

import capa_controladora.AsignacionController;

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

// Servlet mapeado a la URL "/Asignacion" para gestionar la asignación de tickets a técnicos
@WebServlet("/Asignacion")
public class AsignacionServlet extends HttpServlet {

    // Formato de fecha utilizado para parsear y formatear fechas en formato año-mes-día
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * Método doGet: Maneja las peticiones GET al servlet Se encarga de cargar
     * todos los datos necesarios para mostrar la página de asignación
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Configurar codificación UTF-8 para manejar correctamente caracteres especiales
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        // Verificar si el usuario está autenticado, si no lo está redirigir al login
        if (request.getSession().getAttribute("usuarioActual") == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        try {
            // Instanciar el controlador que maneja la lógica de negocio de asignaciones
            AsignacionController ac = new AsignacionController();

            // Cargar todos los datos necesarios para el formulario de asignación
            // Obtener la lista de cargos disponibles (ej: Técnico Junior, Senior, etc.)
            List<String> cargos = ac.obtenerCargos();
            request.setAttribute("listaCargos", cargos);

            // Obtener las prioridades disponibles para los tickets
            request.setAttribute("listaPrioridades", ac.obtenerPrioridades());

            // Obtener los estados posibles de una solicitud
            request.setAttribute("listaEstados", ac.obtenerEstadosSolicitud());

            // Obtener todos los números de ticket existentes
            request.setAttribute("listaTickets", ac.obtenerTodosNumerosTicket());

            // Crear un mapa que agrupa los técnicos por su cargo
            // Esto permite mostrar los técnicos organizados según su cargo en el frontend
            Map<String, List<String>> tecnicosPorCargo = new HashMap<>();
            for (String cargo : cargos) {
                // Para cada cargo, obtener la lista de nombres de técnicos que tienen ese cargo
                List<String> tecnicos = ac.obtenerNombresTecnicosPorCargo(cargo);
                tecnicosPorCargo.put(cargo, tecnicos);
            }
            request.setAttribute("tecnicosPorCargo", tecnicosPorCargo);

            // Obtener todas las solicitudes existentes en el sistema
            List<Solicitud> solicitudes = ac.obtenerTodasSolicitudes();
            request.setAttribute("listaSolicitudes", solicitudes);

            // Redirigir a la página JSP de asignación con todos los datos cargados
            request.getRequestDispatcher("asignarSolicitud.jsp").forward(request, response);
            return;

        } catch (Exception e) {
            // En caso de error al cargar datos, mostrar el error y redirigir al menú admin
            System.err.println("Error al cargar datos de Asignación: " + e.getMessage());
            request.setAttribute("error", "Error al cargar datos iniciales: " + e.getMessage());
            request.getRequestDispatcher("MenuAdmin.jsp").forward(request, response);
            return;
        }
    }

    /**
     * Método doPost: Maneja las peticiones POST al servlet Se encarga de
     * procesar el formulario de asignación de un ticket a un técnico
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // IMPORTANTE: Configurar codificación UTF-8 para manejar correctamente caracteres especiales
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        // Obtener todos los parámetros enviados desde el formulario
        String numeroTicket = request.getParameter("numeroTicket");
        String prioridad = request.getParameter("nivelPrioridad");
        String estado = request.getParameter("estadoSolicitud");
        String tecnicoNombreCompleto = request.getParameter("tecnicoNombre");
        String fechaAsignacionStr = request.getParameter("fechaAsignacion");

        // Log de depuración para verificar qué técnico se recibió desde el formulario
        System.out.println("DEBUG POST: Técnico recibido = '" + tecnicoNombreCompleto + "'");

        String mensaje = null;

        try {
            // Instanciar el controlador de asignaciones
            AsignacionController ac = new AsignacionController();

            // Validar que todos los campos obligatorios estén presentes
            if (numeroTicket == null || numeroTicket.isEmpty()
                    || prioridad == null || prioridad.isEmpty()
                    || estado == null || estado.isEmpty()
                    || tecnicoNombreCompleto == null || tecnicoNombreCompleto.isEmpty()) {

                throw new Exception("Faltan datos de selección: Ticket, Prioridad, Estado o Técnico.");
            }

            // Cargar todas las solicitudes existentes (necesario para el contexto del controlador)
            ac.obtenerTodasSolicitudes();

            // Cargar todos los técnicos de todos los cargos para poder buscar el técnico seleccionado
            List<String> cargos = ac.obtenerCargos();
            for (String cargo : cargos) {
                ac.obtenerNombresTecnicosPorCargo(cargo);
            }

            // Buscar el técnico por su nombre completo
            Usuario tecnicoAsignado = ac.buscarTecnicoPorNombre(tecnicoNombreCompleto);
            if (tecnicoAsignado == null) {
                // Si no se encuentra el técnico, lanzar excepción
                throw new Exception("Error: Técnico '" + tecnicoNombreCompleto + "' no encontrado. Recargue la página.");
            }

            // Crear objetos de modelo con los datos recibidos
            EstadoTicket nivelPrioridad = new EstadoTicket(prioridad);
            EstadoSolicitud nuevoEstado = new EstadoSolicitud(estado);
            Date fechaAsignacion = dateFormat.parse(fechaAsignacionStr); // Convertir string a Date

            // Construir el objeto Solicitud con todos los datos para la asignación
            Solicitud solicitudAActualizar = new Solicitud();
            Ticket ticket = new Ticket(nivelPrioridad, fechaAsignacion, numeroTicket, tecnicoAsignado);
            solicitudAActualizar.setTicket(ticket);
            solicitudAActualizar.setEstadoSolicitud(nuevoEstado);

            // Ejecutar la asignación en la base de datos
            boolean exito = ac.ejecutarAsignacion(solicitudAActualizar);

            if (exito) {
                // Si la asignación fue exitosa, guardar mensaje de éxito en la sesión
                mensaje = "Ticket " + numeroTicket + " asignado/actualizado correctamente.";
                request.getSession().setAttribute("mensajeExito", mensaje);
            } else {
                // Si no fue exitosa pero tampoco lanzó excepción, lanzar error genérico
                throw new Exception("Error desconocido al persistir la asignación.");
            }

        } catch (Exception e) {
            // Capturar cualquier error durante el proceso de asignación
            System.err.println("Error de asignación: " + e.getMessage());
            e.printStackTrace(); // Imprimir traza completa del error para debugging
            request.getSession().setAttribute("error", "Error al procesar la asignación: " + e.getMessage());
        }

        // Redirigir de vuelta a la página de asignación (patrón Post-Redirect-Get)
        // Esto evita que se reenvíe el formulario si el usuario recarga la página
        response.sendRedirect("Asignacion");
    }
}
