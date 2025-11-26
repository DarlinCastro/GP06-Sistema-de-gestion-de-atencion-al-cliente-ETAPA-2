/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package servlet;

import base_datos.ConexionBD;

import capa_controladora.SeguimientoSolicitudController;

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

/**
 * Servlet (Controlador) para la funcionalidad de Seguimiento de
 * Solicitudes/Tickets. Maneja la carga inicial de solicitudes (GET) y las
 * acciones de actualización/cancelación (POST).
 */
@WebServlet("/Seguimiento")
public class SeguimientoSolicitudServlet extends HttpServlet {

    // Constante que almacena el nombre del estado "Cancelado" para su uso en la lógica.
    private final String ESTADO_CANCELADO_NOMBRE = "Cancelado";

    /**
     * Maneja las peticiones HTTP GET. Se utiliza para: 1. Verificar la
     * autenticación del usuario. 2. Gestionar la URL de regreso ('Atrás'). 3.
     * Obtener las solicitudes que corresponden al usuario logueado (cliente o
     * técnico). 4. Cargar la lista de posibles estados (solo para técnicos).
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Usuario usuarioLogeado = (Usuario) request.getSession().getAttribute("usuarioActual");
        // Verifica si el usuario está logueado. Si no lo está, redirige a la página de login.
        if (usuarioLogeado == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        // Este bloque implementa la lógica para que el botón 'Atrás' en el JSP
        // redirija correctamente al menú desde donde se accedió a la pantalla de seguimiento.
        // 1. Capturar el parámetro 'origen' (solo se enviará la primera vez desde el menú)
        String origenMenu = request.getParameter("origen");

        // 2. Si se recibió el parámetro 'origen', se guarda en la sesión.
        // Esto permite que el dato persista incluso después de una redirección POST -> GET (patrón PRG).
        if (origenMenu != null && !origenMenu.isEmpty()) {
            request.getSession().setAttribute("urlMenuOrigen", origenMenu);
        }

        // 3. Pasar la URL de la sesión al request para que el JSP la use.
        String urlAtras = (String) request.getSession().getAttribute("urlMenuOrigen");
        if (urlAtras != null) {
            request.setAttribute("urlAtras", urlAtras);
        }

        // Obtiene el cargo del usuario logueado, limpiando posibles espacios.
        String cargo = usuarioLogeado.getTipoUsuario().getCargo().trim();
        Connection conn = null;
        List<Solicitud> solicitudes = null;

        try {
            // Intenta conectar a la base de datos
            conn = ConexionBD.conectar();
            if (conn != null) {
                SeguimientoSolicitudController ssc = new SeguimientoSolicitudController();

                // Nota: se asume que 'getPassword().getIdentificador()' devuelve el dato correcto 
                // para buscar el ID de usuario numérico en la base de datos.
                int idUsuario = ssc.obtenerIdUsuarioNumerico(usuarioLogeado.getPassword().getIdentificador());

                if (idUsuario == 0) {
                    throw new Exception("ID de usuario no encontrado.");
                }

                // Lógica de carga de solicitudes basada en el cargo del usuario
                if ("Programador".equalsIgnoreCase(cargo) || "Tecnico".equalsIgnoreCase(cargo) || "Técnico".equalsIgnoreCase(cargo)) {
                    // Si es técnico/programador, obtiene las solicitudes que le han sido asignadas.
                    solicitudes = ssc.obtenerSolicitudesAsignadasPorId(idUsuario);
                } else if ("Cliente".equalsIgnoreCase(cargo)) {
                    // Si es cliente, obtiene las solicitudes que él mismo ha creado.
                    solicitudes = ssc.obtenerSolicitudesPorUsuarioId(idUsuario);
                }

                // Solo cargar la lista de estados disponibles para la actualización si el usuario no es Cliente.
                if (!"Cliente".equalsIgnoreCase(cargo)) {
                    request.setAttribute("listaTodosLosEstados", ssc.obtenerEstadosSolicitud());
                }

                // Pasar la lista de solicitudes al JSP y reenviar la solicitud (forward) a la vista.
                request.setAttribute("listaSolicitudes", solicitudes);
                request.getRequestDispatcher("seguimientoSolicitud.jsp").forward(request, response);
                return;
            }
        } catch (Exception e) {
            // Manejo de errores durante la carga de datos
            System.err.println("Error al cargar seguimiento de solicitudes: " + e.getMessage());
            request.setAttribute("error", "Error al cargar la lista de solicitudes: " + e.getMessage());

            // Si hay un error, redirigir al menú correspondiente al cargo del usuario.
            String menuDestino = "Menu" + cargo + ".jsp";
            response.sendRedirect(menuDestino);
            return;
        } finally {
            // Bloque finally para asegurar el cierre de la conexión a la base de datos, si está abierta.
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception e) {
                // Se ignora cualquier error al intentar cerrar la conexión.
            }
        }
    }

    /**
     * Maneja las peticiones HTTP POST. Se utiliza para: 1. Cancelar una
     * solicitud (por parte del cliente). 2. Actualizar el estado de una
     * solicitud (por parte del técnico/programador).
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Obtiene los parámetros del formulario
        String numeroTicket = request.getParameter("numeroTicket");
        String nuevoEstadoStr = request.getParameter("nuevoEstado");
        String accion = request.getParameter("accion");

        Usuario usuarioLogeado = (Usuario) request.getSession().getAttribute("usuarioActual");

        // Re-verifica el logeo (medida de seguridad)
        if (usuarioLogeado == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        String cargo = usuarioLogeado.getTipoUsuario().getCargo().trim();

        String mensaje = null;
        String estadoAUsar = null;

        try {
            // Se utiliza un nuevo controlador ya que no se tiene una conexión persistente
            SeguimientoSolicitudController ssc = new SeguimientoSolicitudController();

            // --- Lógica de CANCELAR ---
            if ("CANCELAR".equalsIgnoreCase(accion)) {
                // Restricción: Solo el Cliente puede cancelar.
                if (!"Cliente".equalsIgnoreCase(cargo)) {
                    throw new Exception("Permiso Denegado: Solo el cliente puede cancelar.");
                }
                // Si es Cliente y cancela, se fija el estado a la constante predefinida.
                estadoAUsar = ESTADO_CANCELADO_NOMBRE;

                // --- Lógica de ACTUALIZAR ESTADO ---
            } else if ("ACTUALIZAR".equalsIgnoreCase(accion)) {
                // Restricción: Los clientes no pueden usar la función de actualizar estado.
                if ("Cliente".equalsIgnoreCase(cargo)) {
                    throw new Exception("Permiso Denegado: Los clientes solo pueden cancelar.");
                }
                // Validación: Se debe haber seleccionado un estado válido.
                if (nuevoEstadoStr == null || nuevoEstadoStr.isEmpty() || nuevoEstadoStr.equals("Seleccione Estado")) {
                    throw new Exception("Debe seleccionar un nuevo estado.");
                }
                estadoAUsar = nuevoEstadoStr;
            } else {
                // Si la acción no es reconocida
                throw new Exception("Acción no válida.");
            }

            // --- Ejecución de la Actualización ---
            // 1. Obtiene los ID numéricos a partir de los nombres/números de la interfaz.
            int idSolicitud = ssc.obtenerIdSolicitudPorTicket(numeroTicket);
            int idNuevoEstado = ssc.obtenerIdEstadoPorNombre(estadoAUsar);

            // 2. Ejecuta la actualización en la base de datos.
            boolean exito = ssc.actualizarEstadoSolicitud(idSolicitud, idNuevoEstado);

            if (exito) {
                // Si tiene éxito, guarda el mensaje de éxito en la sesión (Patrón PRG).
                mensaje = "El estado del Ticket #" + numeroTicket + " ha sido actualizado a: " + estadoAUsar;
                request.getSession().setAttribute("mensajeExito", mensaje);
            } else {
                throw new Exception("Fallo al actualizar el estado en la base de datos.");
            }

        } catch (Exception e) {
            // Si hay un error, lo registra y guarda el mensaje de error en la sesión (Patrón PRG).
            System.err.println("Error al procesar acción: " + e.getMessage());
            request.getSession().setAttribute("error", "Error al procesar la acción: " + e.getMessage());
        }

        // Redirigir al doGet para recargar la lista con los nuevos datos y mostrar mensajes (Patrón PRG).
        response.sendRedirect("Seguimiento");
    }
}
