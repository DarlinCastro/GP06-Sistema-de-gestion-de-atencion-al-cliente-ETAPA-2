package capa_controladora;

import base_datos.ConexionBD;

import capa_modelo.EstadoSolicitud;
import capa_modelo.Usuario;
import capa_modelo.Ticket;
import capa_modelo.TipoServicio;
import capa_modelo.Solicitud;
import capa_modelo.EstadoTicket;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ArrayList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Clase controladora para la lógica de Seguimiento de Solicitudes. Contiene
 * métodos para consultar y actualizar el estado de las solicitudes basándose en
 * el rol del usuario (Cliente o Técnico/Programador).
 */
public class SeguimientoSolicitudController {

    // Formato para manejar fechas, aunque no se usa en la lógica SQL de este archivo.
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    private static final String ESTADO_CANCELADO_NOMBRE = "Cancelado";
    // Constante para un posible texto de selección, aunque no se usa en este código.
    private static final String SELECCIONE_ITEM = "Seleccione ";

    public SeguimientoSolicitudController() {
    }

    /**
     * Método principal para ejecutar una actualización de estado de solicitud.
     * Encapsula la lógica de obtención de IDs y la ejecución de la
     * actualización de la DB.
     *
     * @param numeroTicket El identificador del ticket.
     * @param nuevoEstadoStr El nombre del nuevo estado de la solicitud.
     * @return true si la actualización fue exitosa, false en caso contrario.
     * @throws Exception Si ocurre un error de lógica (IDs no encontrados) o
     * SQL.
     */
    public boolean ejecutarActualizacion(String numeroTicket, String nuevoEstadoStr) throws Exception {

        Connection conn = null;
        try {
            conn = ConexionBD.conectar();
            if (conn == null) {
                throw new SQLException("Fallo de conexión a la Base de Datos.");
            }

            // 1. Obtener el ID numérico del nuevo estado a partir de su nombre.
            int nuevoEstadoId = obtenerIdEstadoPorNombre(nuevoEstadoStr);
            if (nuevoEstadoId == 0) {
                throw new Exception("No se pudo encontrar el ID del estado seleccionado: " + nuevoEstadoStr);
            }

            // 2. Obtener el ID de la Solicitud a partir del número de Ticket.
            int idSolicitud = obtenerIdSolicitudPorTicket(numeroTicket);
            if (idSolicitud == 0) {
                throw new Exception("No se pudo encontrar el ID de la Solicitud para el ticket: " + numeroTicket);
            }

            // 3. Ejecutar la actualización en la DB.
            return actualizarEstadoSolicitud(idSolicitud, nuevoEstadoId);

        } catch (SQLException ex) {
            // Se lanza una nueva excepción con detalles para la capa superior (Servlet).
            throw new Exception("Error en la Base de Datos al actualizar solicitud: " + ex.getMessage(), ex);
        } finally {
            // Asegura el cierre de la conexión, incluso si hay excepciones.
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
            }
        }
    }

    /**
     * Obtiene el ID numérico interno de un usuario a partir de su identificador
     * (usado en el login). Requiere un JOIN con la tabla PASWORD (que se asume
     * contiene el campo IDENTIFICADOR).
     *
     * @param identificador El identificador del usuario.
     * @return El ID numérico del usuario, o 0 si no se encuentra.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public int obtenerIdUsuarioNumerico(String identificador) throws SQLException {
        int idUsuario = 0;
        // La consulta compara el identificador ignorando mayúsculas/minúsculas y espacios (LOWER y TRIM).
        String sql = "SELECT u.IDUSUARIO FROM USUARIO u JOIN PASWORD p ON u.IDPASWORD = p.IDPASWORD WHERE LOWER(TRIM(p.IDENTIFICADOR)) = LOWER(TRIM(?))";

        try (Connection conn = ConexionBD.conectar(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, identificador);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    idUsuario = rs.getInt("IDUSUARIO");
                }
            }
        }
        return idUsuario;
    }

    /**
     * Obtiene todos los posibles estados de solicitud disponibles en la base de
     * datos. Se usa para poblar los dropdowns/selects de cambio de estado.
     *
     * @return Una lista de objetos EstadoSolicitud.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public List<EstadoSolicitud> obtenerEstadosSolicitud() throws SQLException {
        List<EstadoSolicitud> estados = new ArrayList<>();
        String sql = "SELECT estadosolicitud FROM ESTADO_SOLICITUD";

        try (Connection conn = ConexionBD.conectar(); PreparedStatement pstmt = conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                EstadoSolicitud es = new EstadoSolicitud();
                // Limpia el nombre del estado (ej. si es de tipo CHAR(n))
                es.setEstadoSolicitud(rs.getString("estadosolicitud").trim());
                estados.add(es);
            }
        } catch (SQLException ex) {
            System.err.println("Error SQL al cargar Estados: " + ex.getMessage());
            throw ex; // Relanza la excepción para que la maneje el Servlet
        }
        return estados;
    }

    /**
     * Obtiene el ID numérico de un estado de solicitud a partir de su nombre.
     *
     * @param estadoNombre El nombre del estado (ej. "En Progreso").
     * @return El ID numérico del estado, o 0 si no se encuentra.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public int obtenerIdEstadoPorNombre(String estadoNombre) throws SQLException {
        int idEstado = 0;
        // Búsqueda insensible a mayúsculas/minúsculas y espacios (LOWER y TRIM).
        String sql = "SELECT idestadosolicitud FROM ESTADO_SOLICITUD WHERE LOWER(TRIM(estadosolicitud)) = LOWER(TRIM(?))";

        try (Connection conn = ConexionBD.conectar(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, estadoNombre);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    idEstado = rs.getInt("idestadosolicitud");
                }
            }
        }
        return idEstado;
    }

    /**
     * Obtiene el ID numérico de la solicitud a partir del número de ticket (el
     * identificador visible). Requiere un JOIN entre SOLICITUD y TICKET.
     *
     * @param numeroTicket El número de ticket (ej. TCK-0001).
     * @return El ID numérico de la solicitud, o 0 si no se encuentra.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public int obtenerIdSolicitudPorTicket(String numeroTicket) throws SQLException {
        int idSolicitud = 0;
        // Búsqueda insensible a mayúsculas/minúsculas y espacios (LOWER y TRIM).
        String sql = "SELECT s.idsolicitud FROM SOLICITUD s JOIN TICKET t ON s.idticket = t.idticket WHERE LOWER(TRIM(t.numeroticket)) = LOWER(TRIM(?))";

        try (Connection conn = ConexionBD.conectar(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, numeroTicket);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    idSolicitud = rs.getInt("idsolicitud");
                }
            }
        }
        return idSolicitud;
    }

    /**
     * Obtiene la lista de solicitudes creadas por un usuario (típicamente usado
     * para el rol Cliente). Excluye las solicitudes 'Cancelado' y 'Finalizado'
     * del listado.
     *
     * @param idUsuario El ID numérico del usuario cliente.
     * @return Lista de objetos Solicitud.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public List<Solicitud> obtenerSolicitudesPorUsuarioId(int idUsuario) throws SQLException {
        List<Solicitud> solicitudes = new ArrayList<>();
        // Consulta compleja que une Solicitud, Ticket, EstadoTicket, TipoServicio y EstadoSolicitud.
        String sql = "SELECT s.idsolicitud, s.idestadosolicitud, s.idtiposervicio, s.idticket, s.fechacreacion, s.descripcion, "
                + "t.numeroticket, t.idestadoticket, et.nivelprioridad, ts.nombreservicio, es.estadosolicitud "
                + "FROM SOLICITUD s "
                + "JOIN TICKET t ON s.idticket = t.idticket "
                + "JOIN ESTADO_TICKET et ON t.idestadoticket = et.idestadoticket "
                + "JOIN TIPO_SERVICIO ts ON s.idtiposervicio = ts.idtiposervicio "
                + "JOIN ESTADO_SOLICITUD es ON s.idestadosolicitud = es.idestadosolicitud "
                + "WHERE s.idusuario = ? AND es.estadosolicitud NOT IN ('Cancelado', 'Finalizado')";

        try (Connection conn = ConexionBD.conectar(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idUsuario); // Asigna el ID del usuario
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // Utiliza el método auxiliar para crear el objeto Solicitud con todos sus DTOs anidados.
                    solicitudes.add(crearObjetoSolicitud(rs));
                }
            }
        }
        return solicitudes;
    }

    /**
     * Obtiene la lista de solicitudes asignadas a un técnico/programador (usado
     * para roles de Soporte). Excluye las solicitudes 'Cancelado' y
     * 'Finalizado'.
     *
     * @param idUsuario El ID numérico del técnico asignado al ticket.
     * @return Lista de objetos Solicitud.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public List<Solicitud> obtenerSolicitudesAsignadasPorId(int idUsuario) throws SQLException {
        List<Solicitud> solicitudes = new ArrayList<>();
        // Consulta similar a la anterior, pero filtra por el ID de usuario asignado en la tabla TICKET (t.idusuario).
        String sql = "SELECT s.idsolicitud, s.idestadosolicitud, s.idtiposervicio, s.idticket, s.fechacreacion, s.descripcion, "
                + "t.numeroticket, t.idestadoticket, et.nivelprioridad, ts.nombreservicio, es.estadosolicitud "
                + "FROM SOLICITUD s "
                + "JOIN TICKET t ON s.idticket = t.idticket "
                + "JOIN ESTADO_TICKET et ON t.idestadoticket = et.idestadoticket "
                + "JOIN TIPO_SERVICIO ts ON s.idtiposervicio = ts.idtiposervicio "
                + "JOIN ESTADO_SOLICITUD es ON s.idestadosolicitud = es.idestadosolicitud "
                + "WHERE t.idusuario = ? AND es.estadosolicitud NOT IN ('Cancelado', 'Finalizado')";

        try (Connection conn = ConexionBD.conectar(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idUsuario); // Asigna el ID del técnico
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    solicitudes.add(crearObjetoSolicitud(rs));
                }
            }
        }
        return solicitudes;
    }

    /**
     * Obtiene una única solicitud completa a partir de su número de ticket.
     *
     * @param numeroTicket El número de ticket a buscar.
     * @return El objeto Solicitud completo, o null si no se encuentra.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public Solicitud obtenerSolicitudPorTicket(String numeroTicket) throws SQLException {
        Solicitud solicitud = null;
        String sql = "SELECT s.idsolicitud, s.idestadosolicitud, s.idtiposervicio, s.idticket, s.fechacreacion, s.descripcion, "
                + "t.numeroticket, t.idestadoticket, et.nivelprioridad, ts.nombreservicio, es.estadosolicitud "
                + "FROM SOLICITUD s "
                + "JOIN TICKET t ON s.idticket = t.idticket "
                + "JOIN ESTADO_TICKET et ON t.idestadoticket = et.idestadoticket "
                + "JOIN TIPO_SERVICIO ts ON s.idtiposervicio = ts.idtiposervicio "
                + "JOIN ESTADO_SOLICITUD es ON s.idestadosolicitud = es.idestadosolicitud "
                + "WHERE LOWER(TRIM(t.numeroticket)) = LOWER(TRIM(?))";

        try (Connection conn = ConexionBD.conectar(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, numeroTicket);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    solicitud = crearObjetoSolicitud(rs);
                }
            }
        }
        return solicitud;
    }

    /**
     * Método auxiliar privado para mapear un ResultSet a un objeto Solicitud
     * con todos sus DTOs internos (EstadoSolicitud, TipoServicio, Ticket,
     * EstadoTicket). Evita la duplicación de código en los métodos de obtención
     * de solicitudes.
     *
     * @param rs El ResultSet de la consulta.
     * @return Un objeto Solicitud completamente poblado.
     * @throws SQLException Si ocurre un error al leer los datos del ResultSet.
     */
    private Solicitud crearObjetoSolicitud(ResultSet rs) throws SQLException {
        Solicitud s = new Solicitud();

        s.setFechaCreacion(rs.getDate("fechacreacion"));
        s.setDescripcion(rs.getString("descripcion").trim());

        // Mapeo del Estado de Solicitud
        EstadoSolicitud es = new EstadoSolicitud();
        es.setEstadoSolicitud(rs.getString("estadosolicitud").trim());
        s.setEstadoSolicitud(es);

        // Mapeo del Tipo de Servicio
        TipoServicio ts = new TipoServicio();
        ts.setNombreServicio(rs.getString("nombreservicio").trim());
        s.setTipoServicio(ts);

        // Mapeo del Ticket
        Ticket t = new Ticket();
        t.setNumeroTicket(rs.getString("numeroticket").trim());

        // Mapeo del Estado del Ticket (Prioridad)
        EstadoTicket et = new EstadoTicket();
        et.setNivelPrioridad(rs.getString("nivelprioridad").trim());
        t.setEstadoTicket(et);

        s.setTicket(t);

        return s;
    }

    /**
     * Ejecuta la actualización del ID de estado en la tabla SOLICITUD. Este es
     * el método final que realiza el cambio de estado en la base de datos.
     *
     * @param idSolicitud El ID numérico de la solicitud a modificar.
     * @param nuevoEstadoId El ID numérico del nuevo estado.
     * @return true si se afectó al menos una fila, false en caso contrario.
     * @throws Exception Si ocurre un error de base de datos durante la
     * ejecución.
     */
    public boolean actualizarEstadoSolicitud(int idSolicitud, int nuevoEstadoId) throws Exception {
        String sql = "UPDATE SOLICITUD SET IDESTADOSOLICITUD = ? WHERE IDSOLICITUD = ?";

        try (Connection conn = ConexionBD.conectar(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, nuevoEstadoId);
            pstmt.setInt(2, idSolicitud);

            int filasAfectadas = pstmt.executeUpdate();
            return filasAfectadas > 0;
        } catch (SQLException e) {
            System.err.println("Error de SQL al actualizar el estado de la solicitud: " + e.getMessage());
            // Se lanza una excepción más descriptiva para la capa de presentación.
            throw new Exception("Error en la Base de Datos al actualizar solicitud. Detalles: " + e.getMessage(), e);
        }
    }
}
