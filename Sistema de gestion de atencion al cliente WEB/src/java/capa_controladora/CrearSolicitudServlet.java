/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package capa_controladora;

import base_datos.ConexionBD;
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

@WebServlet("/CrearSolicitudServlet")
public class CrearSolicitudServlet extends HttpServlet {

    /**
     * Método GET: Carga la lista de servicios y muestra el formulario
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        System.out.println("=== SERVLET doGet: Iniciando carga de formulario ===");

        try {
            // 1. Verificar que el usuario esté logueado
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("usuarioActual") == null) {
                System.out.println("❌ Sesión no válida, redirigiendo a login");
                response.sendRedirect("login.jsp?mensaje=Debe iniciar sesión primero.");
                return;
            }

            // 2. Obtener la lista de servicios desde la BD
            Map<Integer, String> servicios = CrearSolicitudController.listarTipoServiciosStatic();
            System.out.println("✅ Servicios cargados: " + servicios.size());

            // 3. Verificar que se cargaron servicios
            if (servicios.isEmpty()) {
                System.out.println("⚠️ ADVERTENCIA: No se encontraron servicios en la BD");
                request.setAttribute("mensaje", "Advertencia: No hay servicios disponibles en el sistema.");
            }

            // 4. Pasar la lista al JSP
            request.setAttribute("listaServiciosMap", servicios);

            // 5. Forward al JSP
            String jspPath = "/CrearSolicitud.jsp";
            System.out.println("➡️ Forwarding a: " + jspPath);
            request.getRequestDispatcher(jspPath).forward(request, response);

        } catch (Exception e) {
            System.err.println("❌ ERROR en doGet: " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("mensaje", "Error al cargar el formulario: " + e.getMessage());
            request.getRequestDispatcher("/CrearSolicitud.jsp").forward(request, response);
        }
    }

    /**
     * Método POST: Procesa el formulario y crea la solicitud
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        System.out.println("=== SERVLET doPost: Procesando solicitud ===");
        Connection conn = null;

        try {
            // 1. Verificar sesión activa
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("usuarioActual") == null) {
                System.out.println("❌ Sesión expirada");
                response.sendRedirect("login.jsp?mensaje=Sesión expirada. Por favor inicie sesión.");
                return;
            }

            // 2. Obtener el usuario logueado
            Usuario usuarioActual = (Usuario) session.getAttribute("usuarioActual");
            String correoUsuario = usuarioActual.getCorreoElectronico();
            System.out.println("✅ Usuario: " + correoUsuario);

            // 3. Obtener parámetros del formulario
            String tipoServicioParam = request.getParameter("tipoServicio");
            String descripcion = request.getParameter("descripcion");

            System.out.println("📝 Parámetros recibidos:");
            System.out.println("   - Tipo Servicio: " + tipoServicioParam);
            System.out.println("   - Descripción: " + (descripcion != null ? descripcion.substring(0, Math.min(50, descripcion.length())) + "..." : "null"));

            // 4. Validar parámetros
            if (tipoServicioParam == null || tipoServicioParam.trim().isEmpty()) {
                System.out.println("⚠️ Error: Tipo de servicio no seleccionado");
                response.sendRedirect(request.getContextPath() + "/CrearSolicitudServlet?mensaje=Error: Debe seleccionar un tipo de servicio.");
                return;
            }

            if (descripcion == null || descripcion.trim().isEmpty()) {
                System.out.println("⚠️ Error: Descripción vacía");
                response.sendRedirect(request.getContextPath() + "/CrearSolicitudServlet?mensaje=Error: La descripción es obligatoria.");
                return;
            }

            // 5. Convertir ID de servicio
            int idTipoServicio;
            try {
                idTipoServicio = Integer.parseInt(tipoServicioParam);
            } catch (NumberFormatException e) {
                System.out.println("⚠️ Error: ID de servicio inválido - " + tipoServicioParam);
                response.sendRedirect(request.getContextPath() + "/CrearSolicitudServlet?mensaje=Error: Tipo de servicio inválido.");
                return;
            }

            // 6. Abrir conexión y crear controlador
            conn = ConexionBD.conectar();
            if (conn == null) {
                throw new SQLException("No se pudo establecer conexión con la base de datos");
            }
            System.out.println("✅ Conexión a BD establecida");

            CrearSolicitudController controller = new CrearSolicitudController(conn);

            // 7. Crear la solicitud (esto también crea el ticket)
            System.out.println("🔄 Iniciando creación de solicitud...");
            String numeroTicket = controller.crearSolicitudConTicketWeb(correoUsuario, idTipoServicio, descripcion);

            // 8. Verificar resultado y redirigir
            if (numeroTicket != null) {
                System.out.println("✅ ÉXITO: Solicitud creada con ticket: " + numeroTicket);
                response.sendRedirect("MenuCliente.jsp?mensaje=✅ Solicitud creada exitosamente. Número de Ticket: " + numeroTicket);
            } else {
                System.out.println("❌ Error: No se pudo crear la solicitud");
                response.sendRedirect(request.getContextPath() + "/CrearSolicitudServlet?mensaje=Error: No se pudo crear la solicitud. Intente nuevamente.");
            }

        } catch (NumberFormatException e) {
            System.err.println("❌ Error de formato: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/CrearSolicitudServlet?mensaje=Error: Tipo de servicio inválido.");

        } catch (SQLException e) {
            System.err.println("❌ Error de base de datos: " + e.getMessage());
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/CrearSolicitudServlet?mensaje=Error de base de datos: " + e.getMessage());

        } catch (Exception e) {
            System.err.println("❌ Error general en doPost: " + e.getMessage());
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/CrearSolicitudServlet?mensaje=Error inesperado: " + e.getMessage());

        } finally {
            // 9. Cerrar conexión
            if (conn != null) {
                try {
                    conn.close();
                    System.out.println("✅ Conexión cerrada");
                } catch (SQLException e) {
                    System.err.println("⚠️ Error al cerrar conexión: " + e.getMessage());
                }
            }
        }
    }
}
