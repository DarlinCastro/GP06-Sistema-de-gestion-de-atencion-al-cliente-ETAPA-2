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

public class SeguimientoSolicitudController {

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy"); 
    private static final String ESTADO_CANCELADO_NOMBRE = "Cancelado";
    private static final String SELECCIONE_ITEM = "Seleccione "; 

    public SeguimientoSolicitudController() {
    }

    public boolean ejecutarActualizacion(String numeroTicket, String nuevoEstadoStr) throws Exception {
        
        Connection conn = null;
        try {
            conn = ConexionBD.conectar();
            if (conn == null) throw new SQLException("Fallo de conexión a la Base de Datos.");

            int nuevoEstadoId = obtenerIdEstadoPorNombre(nuevoEstadoStr);
            if (nuevoEstadoId == 0) {
                throw new Exception("No se pudo encontrar el ID del estado seleccionado: " + nuevoEstadoStr);
            }

            int idSolicitud = obtenerIdSolicitudPorTicket(numeroTicket);
            if (idSolicitud == 0) {
                throw new Exception("No se pudo encontrar el ID de la Solicitud para el ticket: " + numeroTicket);
            }

            return actualizarEstadoSolicitud(idSolicitud, nuevoEstadoId);

        } catch (SQLException ex) {
            throw new Exception("Error en la Base de Datos al actualizar solicitud: " + ex.getMessage(), ex);
        } finally {
             try { if (conn != null) conn.close(); } catch (SQLException e) {}
        }
    }

    public int obtenerIdUsuarioNumerico(String identificador) throws SQLException {
        int idUsuario = 0;
        String sql = "SELECT u.IDUSUARIO FROM USUARIO u JOIN PASWORD p ON u.IDPASWORD = p.IDPASWORD WHERE LOWER(TRIM(p.IDENTIFICADOR)) = LOWER(TRIM(?))";
        try (Connection conn = ConexionBD.conectar(); 
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, identificador);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    idUsuario = rs.getInt("IDUSUARIO");
                }
            }
        }
        return idUsuario;
    }

    public List<EstadoSolicitud> obtenerEstadosSolicitud() throws SQLException {
        List<EstadoSolicitud> estados = new ArrayList<>();
        String sql = "SELECT estadosolicitud FROM ESTADO_SOLICITUD";
        try (Connection conn = ConexionBD.conectar(); 
             PreparedStatement pstmt = conn.prepareStatement(sql); 
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                EstadoSolicitud es = new EstadoSolicitud();
                es.setEstadoSolicitud(rs.getString("estadosolicitud").trim());
                estados.add(es);
            }
        } catch (SQLException ex) {
            System.err.println("Error SQL al cargar Estados: " + ex.getMessage());
            throw ex; 
        }
        return estados;
    }

    public int obtenerIdEstadoPorNombre(String estadoNombre) throws SQLException {
        int idEstado = 0;
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

    public int obtenerIdSolicitudPorTicket(String numeroTicket) throws SQLException {
        int idSolicitud = 0;
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

    public List<Solicitud> obtenerSolicitudesPorUsuarioId(int idUsuario) throws SQLException {
        List<Solicitud> solicitudes = new ArrayList<>();
        String sql = "SELECT s.idsolicitud, s.idestadosolicitud, s.idtiposervicio, s.idticket, s.fechacreacion, s.descripcion, "
                + "t.numeroticket, t.idestadoticket, et.nivelprioridad, ts.nombreservicio, es.estadosolicitud "
                + "FROM SOLICITUD s "
                + "JOIN TICKET t ON s.idticket = t.idticket "
                + "JOIN ESTADO_TICKET et ON t.idestadoticket = et.idestadoticket "
                + "JOIN TIPO_SERVICIO ts ON s.idtiposervicio = ts.idtiposervicio "
                + "JOIN ESTADO_SOLICITUD es ON s.idestadosolicitud = es.idestadosolicitud "
                + "WHERE s.idusuario = ? AND es.estadosolicitud NOT IN ('Cancelado', 'Finalizado')"; 
        
        try (Connection conn = ConexionBD.conectar(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idUsuario);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    solicitudes.add(crearObjetoSolicitud(rs));
                }
            }
        }
        return solicitudes;
    }

    public List<Solicitud> obtenerSolicitudesAsignadasPorId(int idUsuario) throws SQLException {
        List<Solicitud> solicitudes = new ArrayList<>();
        String sql = "SELECT s.idsolicitud, s.idestadosolicitud, s.idtiposervicio, s.idticket, s.fechacreacion, s.descripcion, "
                + "t.numeroticket, t.idestadoticket, et.nivelprioridad, ts.nombreservicio, es.estadosolicitud "
                + "FROM SOLICITUD s "
                + "JOIN TICKET t ON s.idticket = t.idticket "
                + "JOIN ESTADO_TICKET et ON t.idestadoticket = et.idestadoticket "
                + "JOIN TIPO_SERVICIO ts ON s.idtiposervicio = ts.idtiposervicio "
                + "JOIN ESTADO_SOLICITUD es ON s.idestadosolicitud = es.idestadosolicitud "
                + "WHERE t.idusuario = ? AND es.estadosolicitud NOT IN ('Cancelado', 'Finalizado')";
        
        try (Connection conn = ConexionBD.conectar(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idUsuario);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    solicitudes.add(crearObjetoSolicitud(rs));
                }
            }
        }
        return solicitudes;
    }
    
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

    private Solicitud crearObjetoSolicitud(ResultSet rs) throws SQLException {
        Solicitud s = new Solicitud();

        s.setFechaCreacion(rs.getDate("fechacreacion"));
        s.setDescripcion(rs.getString("descripcion").trim());

        EstadoSolicitud es = new EstadoSolicitud();
        es.setEstadoSolicitud(rs.getString("estadosolicitud").trim());
        s.setEstadoSolicitud(es);

        TipoServicio ts = new TipoServicio();
        ts.setNombreServicio(rs.getString("nombreservicio").trim());
        s.setTipoServicio(ts);

        Ticket t = new Ticket();
        t.setNumeroTicket(rs.getString("numeroticket").trim());

        EstadoTicket et = new EstadoTicket();
        et.setNivelPrioridad(rs.getString("nivelprioridad").trim());
        t.setEstadoTicket(et);

        s.setTicket(t);

        return s;
    }

    public boolean actualizarEstadoSolicitud(int idSolicitud, int nuevoEstadoId) throws Exception {
        String sql = "UPDATE SOLICITUD SET IDESTADOSOLICITUD = ? WHERE IDSOLICITUD = ?";

        try (Connection conn = ConexionBD.conectar(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, nuevoEstadoId);
            pstmt.setInt(2, idSolicitud);

            int filasAfectadas = pstmt.executeUpdate();
            return filasAfectadas > 0;
        } catch (SQLException e) {
            System.err.println("Error de SQL al actualizar el estado de la solicitud: " + e.getMessage());
            throw new Exception("Error en la Base de Datos al actualizar solicitud. Detalles: " + e.getMessage(), e);
        }
    }
}