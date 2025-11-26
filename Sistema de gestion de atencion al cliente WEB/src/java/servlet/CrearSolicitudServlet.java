/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package servlet;

import base_datos.ConexionBD;

import capa_controladora.CrearSolicitudController;

import capa_modelo.Usuario;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Servlet que maneja la creación de solicitudes de servicio Tiene dos funciones
 * principales: - doGet: Carga el formulario con la lista de servicios
 * disponibles - doPost: Procesa el formulario y crea la solicitud en la base de
 * datos
 */
@WebServlet("/CrearSolicitudServlet")
public class CrearSolicitudServlet extends HttpServlet {

    /**
     * Método GET: Carga la lista de servicios y muestra el formulario Este
     * método se ejecuta cuando el usuario accede a la URL del servlet
     * directamente o cuando se hace una redirección GET desde otra parte de la
     * aplicación
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Log de inicio del proceso
        System.out.println("=== SERVLET doGet: Iniciando carga de formulario ===");

        try {
            // 1. Verificar que el usuario esté logueado (seguridad básica)
            HttpSession session = request.getSession(false); // false = no crear nueva sesión
            if (session == null || session.getAttribute("usuarioActual") == null) {
                // Si no hay sesión válida, redirigir al login
                System.out.println("Sesión no válida, redirigiendo a login");
                response.sendRedirect("login.jsp?mensaje=Debe iniciar sesión primero.");
                return;
            }

            // 2. Obtener la lista de servicios desde la BD usando el método estático
            // Este método abre y cierra su propia conexión
            Map<Integer, String> servicios = CrearSolicitudController.listarTipoServiciosStatic();
            System.out.println("Servicios cargados: " + servicios.size());

            // 3. Verificar que se cargaron servicios (validación de datos)
            if (servicios.isEmpty()) {
                System.out.println("ADVERTENCIA: No se encontraron servicios en la BD");
                request.setAttribute("mensaje", "Advertencia: No hay servicios disponibles en el sistema.");
            }

            // 4. Pasar la lista al JSP como atributo de request
            // El JSP podrá acceder a este mapa con ${listaServiciosMap}
            request.setAttribute("listaServiciosMap", servicios);

            // 5. Forward al JSP para mostrar el formulario
            String jspPath = "/CrearSolicitud.jsp";
            System.out.println("Forwarding a: " + jspPath);
            request.getRequestDispatcher(jspPath).forward(request, response);

        } catch (Exception e) {
            // Capturar cualquier error inesperado durante la carga del formulario
            System.err.println("ERROR en doGet: " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("mensaje", "Error al cargar el formulario: " + e.getMessage());
            request.getRequestDispatcher("/CrearSolicitud.jsp").forward(request, response);
        }
    }

    /**
     * Método POST: Procesa el formulario y crea la solicitud Este método se
     * ejecuta cuando el usuario envía el formulario de creación de solicitud
     * Maneja toda la lógica de validación, creación de ticket y solicitud
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Log de inicio del proceso
        System.out.println("=== SERVLET doPost: Procesando solicitud ===");
        Connection conn = null; // Conexión que se pasará al controlador

        try {
            // 1. Verificar sesión activa (seguridad)
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("usuarioActual") == null) {
                System.out.println("Sesión expirada");
                response.sendRedirect("login.jsp?mensaje=Sesión expirada. Por favor inicie sesión.");
                return;
            }

            // 2. Obtener el usuario logueado desde la sesión
            Usuario usuarioActual = (Usuario) session.getAttribute("usuarioActual");
            String correoUsuario = usuarioActual.getCorreoElectronico();
            System.out.println("Usuario: " + correoUsuario);

            // 3. Obtener parámetros del formulario enviados por POST
            String tipoServicioParam = request.getParameter("tipoServicio"); // ID del servicio seleccionado
            String descripcion = request.getParameter("descripcion"); // Descripción del problema

            // Log de los parámetros recibidos para debugging
            System.out.println("Parámetros recibidos:");
            System.out.println("   - Tipo Servicio: " + tipoServicioParam);
            System.out.println("   - Descripción: " + (descripcion != null ? descripcion.substring(0, Math.min(50, descripcion.length())) + "..." : "null"));

            // 4. Validar que se seleccionó un tipo de servicio
            if (tipoServicioParam == null || tipoServicioParam.trim().isEmpty()) {
                System.out.println("⚠️ Error: Tipo de servicio no seleccionado");
                response.sendRedirect(request.getContextPath() + "/CrearSolicitudServlet?mensaje=Error: Debe seleccionar un tipo de servicio.");
                return;
            }

            // 5. Validar que se ingresó una descripción
            if (descripcion == null || descripcion.trim().isEmpty()) {
                System.out.println("Error: Descripción vacía");
                response.sendRedirect(request.getContextPath() + "/CrearSolicitudServlet?mensaje=Error: La descripción es obligatoria.");
                return;
            }

            // 6. Convertir ID de servicio de String a Integer
            int idTipoServicio;
            try {
                idTipoServicio = Integer.parseInt(tipoServicioParam);
            } catch (NumberFormatException e) {
                // Si el ID no es un número válido, mostrar error
                System.out.println("Error: ID de servicio inválido - " + tipoServicioParam);
                response.sendRedirect(request.getContextPath() + "/CrearSolicitudServlet?mensaje=Error: Tipo de servicio inválido.");
                return;
            }

            // 7. Abrir conexión a la base de datos y crear controlador
            conn = ConexionBD.conectar();
            if (conn == null) {
                throw new SQLException("No se pudo establecer conexión con la base de datos");
            }
            System.out.println("Conexión a BD establecida");

            // Crear controlador pasándole la conexión (patrón de inyección de dependencia)
            CrearSolicitudController controller = new CrearSolicitudController(conn);

            // 8. Crear la solicitud (esto también crea el ticket asociado)
            System.out.println("Iniciando creación de solicitud...");
            String numeroTicket = controller.crearSolicitudConTicketWeb(correoUsuario, idTipoServicio, descripcion);

            // 9. Verificar resultado y redirigir según el resultado
            if (numeroTicket != null) {
                // Éxito: redirigir al menú con mensaje de éxito
                System.out.println("ÉXITO: Solicitud creada con ticket: " + numeroTicket);
                response.sendRedirect("MenuCliente.jsp?mensaje=✅ Solicitud creada exitosamente. Número de Ticket: " + numeroTicket);
            } else {
                // Fallo: redirigir al formulario con mensaje de error
                System.out.println("Error: No se pudo crear la solicitud");
                response.sendRedirect(request.getContextPath() + "/CrearSolicitudServlet?mensaje=Error: No se pudo crear la solicitud. Intente nuevamente.");
            }

        } catch (NumberFormatException e) {
            // Error al convertir el ID de servicio a número
            System.err.println("Error de formato: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/CrearSolicitudServlet?mensaje=Error: Tipo de servicio inválido.");

        } catch (SQLException e) {
            // Error de base de datos (conexión, consulta, etc.)
            System.err.println("Error de base de datos: " + e.getMessage());
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/CrearSolicitudServlet?mensaje=Error de base de datos: " + e.getMessage());

        } catch (Exception e) {
            // Cualquier otro error inesperado
            System.err.println("Error general en doPost: " + e.getMessage());
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/CrearSolicitudServlet?mensaje=Error inesperado: " + e.getMessage());

        } finally {
            // 10. Cerrar conexión (SIEMPRE, haya o no error)
            if (conn != null) {
                try {
                    conn.close();
                    System.out.println("Conexión cerrada");
                } catch (SQLException e) {
                    System.err.println("Error al cerrar conexión: " + e.getMessage());
                }
            }
        }
    }
}
