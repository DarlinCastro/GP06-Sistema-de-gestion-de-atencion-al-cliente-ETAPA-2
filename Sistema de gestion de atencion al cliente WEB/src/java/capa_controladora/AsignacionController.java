/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package capa_controladora;

import capa_modelo.EstadoSolicitud;
import capa_modelo.Usuario;
import capa_modelo.Ticket;
import capa_modelo.TipoUsuario;
import capa_modelo.EstadoTicket;
import capa_modelo.TipoServicio;
import capa_modelo.Solicitud;
import base_datos.ConexionBD;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AsignacionController {
    private Solicitud solicitudSeleccionada;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private List<Solicitud> listaTodasSolicitudes; 
    private List<String> listaTodosCargos;
    private List<Usuario> listaTodosTecnicos; 

    private static final String SQL_OBTENER_CARGOS = "SELECT DISTINCT cargo FROM TIPO_USUARIO";
    private static final String SQL_OBTENER_TECNICOS_POR_CARGO = "SELECT u.nombres, u.apellidos, tu.cargo FROM USUARIO u JOIN TIPO_USUARIO tu ON u.idtipousuario = tu.idtipousuario WHERE tu.cargo = ?";
    private static final String SQL_OBTENER_ID_TECNICO_BY_NAME = 
        "SELECT idusuario FROM USUARIO WHERE nombres = ? AND apellidos = ?";
    private static final String SQL_OBTENER_SOLICITUDES = 
        "SELECT s.idsolicitud, s.fechacreacion, s.descripcion, t.numeroticket, t.fechaasignacion, " + 
        "   u.idusuario AS idtecnico, u.nombres AS tecnico_nombres, u.apellidos AS tecnico_apellidos, " +
        "   tu.cargo AS tecnico_cargo, t.idestadoticket, es.estadosolicitud, et.nivelprioridad, ts.nombreservicio " + 
        "FROM SOLICITUD s " +
        "JOIN TICKET t ON s.idticket = t.idticket " +
        "JOIN ESTADO_SOLICITUD es ON s.idestadosolicitud = es.idestadosolicitud " +
        "JOIN TIPO_SERVICIO ts ON s.idtiposervicio = ts.idtiposervicio " +
        "LEFT JOIN ESTADO_TICKET et ON t.idestadoticket = et.idestadoticket " + 
        "LEFT JOIN USUARIO u ON t.idusuario = u.idusuario " + 
        "LEFT JOIN TIPO_USUARIO tu ON u.idtipousuario = tu.idtipousuario " + 
        "ORDER BY s.idsolicitud DESC";
    private static final String SQL_ID_PRIORIDAD = "SELECT idestadoticket FROM ESTADO_TICKET WHERE TRIM(nivelprioridad) = ?";
    private static final String SQL_ID_ESTADO_SOLICITUD = "SELECT idestadosolicitud FROM ESTADO_SOLICITUD WHERE TRIM(estadosolicitud) = ?";
    private static final String SQL_UPDATE_TICKET = "UPDATE TICKET SET idestadoticket = ?, idusuario = ?, fechaasignacion = ? WHERE numeroticket = ?";
    private static final String SQL_UPDATE_SOLICITUD = "UPDATE SOLICITUD SET idestadosolicitud = ? WHERE idticket = (SELECT idticket FROM TICKET WHERE numeroticket = ?)";
    private static final String SQL_OBTENER_PRIORIDADES = "SELECT NIVELPRIORIDAD FROM ESTADO_TICKET";
    private static final String SQL_OBTENER_ESTADOS_SOLICITUD = "SELECT ESTADOSOLICITUD FROM ESTADO_SOLICITUD";

    public AsignacionController() {
        this.solicitudSeleccionada = null;
        this.listaTodosTecnicos = new ArrayList<>();
    }

    private int obtenerId(Connection conn, String sql, String valor) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, valor.trim()); 
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Error: No se encontró ID para el valor: " + valor);
    }
    
    private int obtenerIdTecnico(Connection conn, String nombres, String apellidos) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SQL_OBTENER_ID_TECNICO_BY_NAME)) {
            ps.setString(1, nombres.trim()); 
            ps.setString(2, apellidos.trim()); 
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("idusuario");
                }
            }
        }
        throw new SQLException("Error: No se encontró ID para el técnico: " + nombres + " " + apellidos);
    }
    
    public Solicitud buscarSolicitudPorTicket(String numeroTicket) {
        if (numeroTicket == null || listaTodasSolicitudes == null) return null;
        
        for (Solicitud s : listaTodasSolicitudes) {
            if (s.getTicket().getNumeroTicket().equals(numeroTicket)) {
                return s;
            }
        }
        return null;
    }

    public Usuario buscarTecnicoPorNombre(String nombreCompleto) {
        if (nombreCompleto == null || listaTodosTecnicos == null) return null;
        
        String[] partes = nombreCompleto.split(" ", 2);
        if (partes.length < 2) return null;

        String nombres = partes[0].trim();
        String apellidos = partes[1].trim();
        
        for (Usuario u : listaTodosTecnicos) {
            if (u.getNombres().trim().equals(nombres) && u.getApellidos().trim().equals(apellidos)) {
                return u;
            }
        }
        return null;
    }
    
    public List<String> obtenerCargos() {
         List<String> cargos = new ArrayList<>();
         try (Connection conn = ConexionBD.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL_OBTENER_CARGOS)) {

             if (conn == null) return cargos;

             while (rs.next()) {
                 cargos.add(rs.getString("cargo").trim());
             }
         } catch (SQLException e) {
             System.err.println("Error al obtener cargos: " + e.getMessage());
         }
         return cargos;
    }
    
    public List<String> obtenerPrioridades() {
        List<String> prioridades = new ArrayList<>();
        try (Connection conn = ConexionBD.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL_OBTENER_PRIORIDADES)) {

             if (conn == null) return prioridades;

             while (rs.next()) {
                 prioridades.add(rs.getString("NIVELPRIORIDAD").trim());
             }
        } catch (SQLException e) {
            System.err.println("Error al obtener prioridades: " + e.getMessage());
        }
        return prioridades;
    }

    public List<String> obtenerEstadosSolicitud() {
         List<String> estados = new ArrayList<>();
         try (Connection conn = ConexionBD.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL_OBTENER_ESTADOS_SOLICITUD)) {

             if (conn == null) return estados;

             while (rs.next()) {
                 estados.add(rs.getString("ESTADOSOLICITUD").trim());
             }
         } catch (SQLException e) {
             System.err.println("Error al obtener estados de solicitud: " + e.getMessage());
         }
         return estados;
    }

    public List<String> obtenerNombresTecnicosPorCargo(String cargo) {
        List<String> nombresTecnicos = new ArrayList<>();
        this.listaTodosTecnicos.clear(); 
        String cargoPadded = String.format("%-10s", cargo); 

        try (Connection conn = ConexionBD.conectar();
             PreparedStatement ps = conn.prepareStatement(SQL_OBTENER_TECNICOS_POR_CARGO)) {

            if (conn == null) return nombresTecnicos;

            ps.setString(1, cargoPadded);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TipoUsuario tipo = new TipoUsuario(rs.getString("cargo").trim()); 
                    Usuario tecnico = new Usuario();
                    tecnico.setNombres(rs.getString("nombres").trim());
                    tecnico.setApellidos(rs.getString("apellidos").trim());
                    tecnico.setTipoUsuario(tipo);
                    
                    this.listaTodosTecnicos.add(tecnico); 
                    
                    nombresTecnicos.add(tecnico.getNombres().trim() + " " + tecnico.getApellidos().trim());
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener técnicos por cargo: " + e.getMessage());
        }
        return nombresTecnicos;
    }

    public List<Solicitud> obtenerTodasSolicitudes() {
        List<Solicitud> solicitudes = new ArrayList<>();
        try (Connection conn = ConexionBD.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL_OBTENER_SOLICITUDES)) {

            if (conn == null) return solicitudes;

            while (rs.next()) {
                EstadoSolicitud estado = new EstadoSolicitud(rs.getString("estadosolicitud").trim());
                TipoServicio servicio = new TipoServicio(rs.getString("nombreservicio").trim());
                
                Usuario tecnicoAsignado = null;
                Date fechaAsignacion = rs.getDate("fechaasignacion");
                EstadoTicket nivelPrioridad = null;

                if (rs.getString("nivelprioridad") != null) {
                    nivelPrioridad = new EstadoTicket(rs.getString("nivelprioridad").trim());
                }
                
                if (rs.getInt("idtecnico") != 0) {
                    TipoUsuario tipoTecnico = new TipoUsuario(rs.getString("tecnico_cargo").trim());
                    tecnicoAsignado = new Usuario();
                    tecnicoAsignado.setNombres(rs.getString("tecnico_nombres").trim());
                    tecnicoAsignado.setApellidos(rs.getString("tecnico_apellidos").trim());
                    tecnicoAsignado.setTipoUsuario(tipoTecnico);
                }

                Ticket ticket = new Ticket(nivelPrioridad, fechaAsignacion, rs.getString("numeroticket").trim(), tecnicoAsignado);
                
                Solicitud s = new Solicitud(null, servicio, estado, ticket, rs.getDate("fechacreacion"), rs.getString("descripcion").trim());
                solicitudes.add(s);
            }
            this.listaTodasSolicitudes = solicitudes;
        } catch (SQLException e) {
            System.err.println("Error al obtener solicitudes: " + e.getMessage());
        }
        return solicitudes;
    }
    public boolean ejecutarAsignacion(Solicitud solicitud) throws Exception {
        Connection conn = ConexionBD.conectar();
        if (conn == null) return false;

        boolean exito = false;

        try {
            conn.setAutoCommit(false); 
            
            String prioridad = solicitud.getTicket().getEstadoTicket().getNivelPrioridad();
            String estado = solicitud.getEstadoSolicitud().getEstadoSolicitud();
            Usuario tecnicoAsignado = solicitud.getTicket().getTecnicoAsignado();
            
            int idPrioridad = obtenerId(conn, SQL_ID_PRIORIDAD, prioridad);
            int idNuevoEstado = obtenerId(conn, SQL_ID_ESTADO_SOLICITUD, estado);
            
            int idTecnico = obtenerIdTecnico(conn, tecnicoAsignado.getNombres(), tecnicoAsignado.getApellidos());

            try (PreparedStatement psTicket = conn.prepareStatement(SQL_UPDATE_TICKET)) {
                psTicket.setInt(1, idPrioridad);
                psTicket.setInt(2, idTecnico); 
                psTicket.setDate(3, new java.sql.Date(solicitud.getTicket().getFechaAsignacion().getTime())); 
                psTicket.setString(4, solicitud.getTicket().getNumeroTicket());
                psTicket.executeUpdate();
            }

            try (PreparedStatement psSolicitud = conn.prepareStatement(SQL_UPDATE_SOLICITUD)) {
                psSolicitud.setInt(1, idNuevoEstado);
                psSolicitud.setString(2, solicitud.getTicket().getNumeroTicket());
                psSolicitud.executeUpdate();
            }

            conn.commit(); 
            exito = true;

        } catch (SQLException e) {
            System.err.println("Error en la transacción de asignación: " + e.getMessage());
            try {
                conn.rollback(); 
            } catch (SQLException ex) {
                System.err.println("Error al hacer rollback: " + ex.getMessage());
            }
            throw new Exception("Fallo en la asignación: " + e.getMessage(), e); 
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException e) {}
        }
        return exito;
    }

}