/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package capa_controladora;

import capa_modelo.Solicitud;
import capa_modelo.Usuario;
import capa_modelo.TipoServicio;
import capa_modelo.Ticket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.Date;
import base_datos.ConexionBD; //me decia que lo agregara pero no estoy segura si va

public class CrearSolicitudController {

    public CrearSolicitudController() {
    }

    private int obtenerIdTipoServicioPorNombre(Connection conn, String nombreServicio) throws SQLException {
        String sql = "SELECT idtiposervicio FROM tipo_servicio WHERE LOWER(TRIM(nombreservicio)) = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nombreServicio.trim().toLowerCase());
            try(ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt("idtiposervicio");
            }
        } 
        return -1;
    }

    private int obtenerIdEstadoSolicitudPorNombre(Connection conn, String nombreEstado) throws SQLException {
        String sql = "SELECT idestadosolicitud FROM estado_solicitud WHERE LOWER(TRIM(estadosolicitud)) = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nombreEstado.trim().toLowerCase());
            try(ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt("idestadosolicitud");
            }
        } 
        return -1;
    }
    
    private int obtenerIdUsuarioPorCorreo(Connection conn, String correo) throws SQLException {
        String sql = "SELECT idusuario FROM usuario WHERE LOWER(TRIM(correoelectronico)) = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, correo.trim().toLowerCase());
            try(ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt("idusuario");
            }
        } 
        return -1;
    }

    private int obtenerCantidadTickets(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) AS total FROM ticket";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getInt("total");
        }
        return -1;
    }

    private int crearTicket(Connection conn, int idUsuario, String numeroTicket) throws SQLException {
        String sql = "INSERT INTO ticket (idestadoticket, idusuario, fechaasignacion, numeroticket) VALUES (?, ?, ?, ?) RETURNING idticket";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, 1); 
            stmt.setInt(2, idUsuario); 
            stmt.setDate(3, new java.sql.Date(new Date().getTime()));
            stmt.setString(4, numeroTicket);
            
            try(ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt("idticket");
            }
        } 
        return -1;
    }

    private boolean guardarSolicitud(Connection conn, Solicitud solicitud, int idUsuario, int idTipoServicio, int idEstadoSolicitud, int idTicket) throws SQLException {
        String sql = "INSERT INTO solicitud (idusuario, idtiposervicio, idestadosolicitud, idticket, fechacreacion, descripcion) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUsuario);
            stmt.setInt(2, idTipoServicio);
            stmt.setInt(3, idEstadoSolicitud);
            stmt.setInt(4, idTicket);
            
            stmt.setDate(5, new java.sql.Date(solicitud.getFechaCreacion().getTime()));

            String descripcion = solicitud.getDescripcion() != null ? solicitud.getDescripcion().trim() : "";
            if (descripcion.length() > 300) { 
                descripcion = descripcion.substring(0, 300);
            }
            stmt.setString(6, descripcion);

            return stmt.executeUpdate() > 0;
        }
    }


    private String generarNuevoNumeroTicket(Connection conn) throws SQLException {
        int cantidadTickets = obtenerCantidadTickets(conn);
        int nuevoId = cantidadTickets + 1;
        
        String idFormateado = String.format("%04d", nuevoId);
        return "T" + idFormateado;
    }

    public String crearSolicitudConTicket(Connection conn, Usuario usuario, TipoServicio tipoServicio, String descripcion) throws SQLException {

        int idUsuario = obtenerIdUsuarioPorCorreo(conn, usuario.getCorreoElectronico());
        int idTipoServicio = obtenerIdTipoServicioPorNombre(conn, tipoServicio.getNombreServicio());
        int idEstadoSolicitud = obtenerIdEstadoSolicitudPorNombre(conn, "Pendiente"); 

        if (idUsuario == -1 || idTipoServicio == -1 || idEstadoSolicitud == -1) {
            System.err.println("Error: Datos de entrada no válidos (Usuario, Tipo de Servicio o Estado no encontrado).");
            throw new SQLException("Error de FK: Datos no encontrados en BD.");
        }

        //Generar Ticket y obtener ID del BD
        String numeroTicket = generarNuevoNumeroTicket(conn);
        int idTicket = crearTicket(conn, idUsuario, numeroTicket);
        if (idTicket == -1) {
            throw new SQLException("Error: Fallo al crear el registro del Ticket.");
        }

        Solicitud solicitud = new Solicitud();
        solicitud.setFechaCreacion(new Date());

        String descGuardar = descripcion.length() > 300 ? descripcion.substring(0, 300) : descripcion;
        solicitud.setDescripcion(descGuardar);
        
        Ticket ticketCreado = new Ticket();
        ticketCreado.setNumeroTicket(numeroTicket); 
        solicitud.setTicket(ticketCreado); 

        boolean solicitudGuardada = guardarSolicitud(conn, solicitud, idUsuario, idTipoServicio, idEstadoSolicitud, idTicket);
        
        if (!solicitudGuardada) {
            throw new SQLException("Error: Fallo al guardar el registro de la Solicitud.");
        }
        
        return numeroTicket;
    }
}