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
public class SeguimientoSolicitudServlet extends HttpServlet {

    private final String ESTADO_CANCELADO_NOMBRE = "Cancelado";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Usuario usuarioLogeado = (Usuario) request.getSession().getAttribute("usuarioActual");
        if (usuarioLogeado == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        // --- INICIO DE CÓDIGO MODIFICADO PARA EL BOTÓN ATRÁS ---
        // 1. Capturar el parámetro 'origen' (solo se enviará la primera vez desde el menú)
        String origenMenu = request.getParameter("origen");

        // 2. Si se recibió el parámetro 'origen', se guarda en la sesión.
        // Esto permite que el dato persista incluso después de una redirección POST -> GET.
        if (origenMenu != null && !origenMenu.isEmpty()) {
            request.getSession().setAttribute("urlMenuOrigen", origenMenu);
        }

        // 3. Pasar la URL de la sesión al request para que el JSP la use.
        String urlAtras = (String) request.getSession().getAttribute("urlMenuOrigen");
        if (urlAtras != null) {
            request.setAttribute("urlAtras", urlAtras);
        }

        // --- FIN DE CÓDIGO MODIFICADO PARA EL BOTÓN ATRÁS ---
        String cargo = usuarioLogeado.getTipoUsuario().getCargo().trim();
        Connection conn = null;
        List<Solicitud> solicitudes = null;

        try {
            conn = ConexionBD.conectar();
            if (conn != null) {
                SeguimientoSolicitudController ssc = new SeguimientoSolicitudController();

                // Nota: se asume que 'getPassword().getIdentificador()' devuelve el dato correcto para buscar el ID de usuario.
                int idUsuario = ssc.obtenerIdUsuarioNumerico(usuarioLogeado.getPassword().getIdentificador());

                if (idUsuario == 0) {
                    throw new Exception("ID de usuario no encontrado.");
                }

                if ("Programador".equalsIgnoreCase(cargo) || "Tecnico".equalsIgnoreCase(cargo) || "Técnico".equalsIgnoreCase(cargo)) {
                    solicitudes = ssc.obtenerSolicitudesAsignadasPorId(idUsuario);
                } else if ("Cliente".equalsIgnoreCase(cargo)) {
                    solicitudes = ssc.obtenerSolicitudesPorUsuarioId(idUsuario);
                }

                // Solo cargar la lista de estados para Programador/Técnico
                if (!"Cliente".equalsIgnoreCase(cargo)) {
                    request.setAttribute("listaTodosLosEstados", ssc.obtenerEstadosSolicitud());
                }

                // Pasar la lista de solicitudes al JSP
                request.setAttribute("listaSolicitudes", solicitudes);
                request.getRequestDispatcher("seguimientoSolicitud.jsp").forward(request, response);
                return;
            }
        } catch (Exception e) {
            System.err.println("Error al cargar seguimiento de solicitudes: " + e.getMessage());
            request.setAttribute("error", "Error al cargar la lista de solicitudes: " + e.getMessage());

            // Si hay un error, redirigir al menú correspondiente.
            String menuDestino = "Menu" + cargo + ".jsp";
            response.sendRedirect(menuDestino);
            return;
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception e) {
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String numeroTicket = request.getParameter("numeroTicket");
        String nuevoEstadoStr = request.getParameter("nuevoEstado");
        String accion = request.getParameter("accion");

        Usuario usuarioLogeado = (Usuario) request.getSession().getAttribute("usuarioActual");

        // Verificación de logeo (aunque debería estar en sesión si llegó aquí)
        if (usuarioLogeado == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        String cargo = usuarioLogeado.getTipoUsuario().getCargo().trim();

        String mensaje = null;
        String estadoAUsar = null;

        try {
            // Se utiliza un nuevo controlador ya que no se tiene conexión activa aquí
            SeguimientoSolicitudController ssc = new SeguimientoSolicitudController();

            if ("CANCELAR".equalsIgnoreCase(accion)) {
                if (!"Cliente".equalsIgnoreCase(cargo)) {
                    throw new Exception("Permiso Denegado: Solo el cliente puede cancelar.");
                }
                estadoAUsar = ESTADO_CANCELADO_NOMBRE;

            } else if ("ACTUALIZAR".equalsIgnoreCase(accion)) {
                if ("Cliente".equalsIgnoreCase(cargo)) {
                    throw new Exception("Permiso Denegado: Los clientes solo pueden cancelar.");
                }
                if (nuevoEstadoStr == null || nuevoEstadoStr.isEmpty() || nuevoEstadoStr.equals("Seleccione Estado")) {
                    throw new Exception("Debe seleccionar un nuevo estado.");
                }
                estadoAUsar = nuevoEstadoStr;
            } else {
                throw new Exception("Acción no válida.");
            }

            // Lógica de actualización
            int idSolicitud = ssc.obtenerIdSolicitudPorTicket(numeroTicket);
            int idNuevoEstado = ssc.obtenerIdEstadoPorNombre(estadoAUsar);

            boolean exito = ssc.actualizarEstadoSolicitud(idSolicitud, idNuevoEstado);

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

        // Redirigir al doGet para recargar la lista y mostrar mensajes
        response.sendRedirect("Seguimiento");
    }
}
