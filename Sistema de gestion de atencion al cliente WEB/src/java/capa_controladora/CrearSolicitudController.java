/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package capa_controladora;

// Asegúrate de que estas clases de modelo existan en tu proyecto
import base_datos.ConexionBD; 
import capa_modelo.Solicitud; 
import capa_modelo.TipoServicio; 
import capa_modelo.Usuario; 
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Clase que gestiona la lógica de negocio para la creación de una Solicitud.
 * Contiene un método estático para la lectura (combobox) y métodos de instancia
 * para la transacción (escritura).
 */
public class CrearSolicitudController { 
    
    // Conexión para los métodos de instancia (Escritura/Transacción)
    private final Connection conn;

    /**
     * Constructor usado por el Servlet para el doPost (Transacciones).
     */
    public CrearSolicitudController(Connection conn) {
        this.conn = conn;
    }
    
    // =========================================================================
    // --- LÓGICA ESTÁTICA PARA LECTURA (USADA POR doGet del Servlet) ---
    // =========================================================================

    /**
     * ✅ MÉTODO ESTÁTICO: Obtiene todos los IDs y Nombres de servicio para llenar el JSP.
     * Abre y cierra su propia conexión, imitando el patrón exitoso de GenerarReporteController.
     * @return Un mapa de ID del servicio (Integer) a Nombre del servicio (String).
     */
    public static Map<Integer, String> listarTipoServiciosStatic() { 
        Map<Integer, String> servicios = new HashMap<>();
        System.out.println("DEBUG: Iniciando listarTipoServiciosStatic");
        
        // Usamos TRIM(nombreservicio) para evitar los espacios de CHAR(80)
        String sql = "SELECT idtiposervicio, TRIM(nombreservicio) AS nombre_limpio FROM tipo_servicio ORDER BY nombreservicio";
        
        // La conexión se abre y se cierra dentro de este bloque try-with-resources
        try (Connection connEstatica = ConexionBD.conectar(); 
             PreparedStatement stmt = connEstatica.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            System.out.println("DEBUG: Conexión y query OK. Procesando resultados...");
            int count = 0;
            while (rs.next()) {
                int id = rs.getInt("idtiposervicio"); 
                String nombre = rs.getString("nombre_limpio"); 
                System.out.println("DEBUG: Servicio #" + (++count) + ": ID=" + id + ", Nombre='" + nombre + "' (longitud: " + nombre.length() + ")");
                servicios.put(id, nombre.trim()); // .trim() extra por precaución
            }
            System.out.println("DEBUG: Total servicios cargados: " + servicios.size());
        } catch (SQLException e) {
            System.err.println("❌ ERROR SQL al listarTipoServiciosStatic: " + e.getMessage());
            e.printStackTrace(); // Detalles completos del error
        }
        return servicios;
    }
    
    // =========================================================================
    // --- MÉTODOS PRIVADOS DAO (USAN this.conn para la Transacción) ---
    // =========================================================================

    /**
     * Busca el ID del Usuario por correo electrónico. (CORREOELECTRONICO es CHAR(25))
     */
    private int obtenerIdUsuarioPorCorreo(String correo) throws SQLException {
        // Usamos TRIM y LOWER para manejo de CHAR y case-insensitivity
        String sql = "SELECT idusuario FROM usuario WHERE LOWER(TRIM(correoelectronico)) = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, correo.trim().toLowerCase());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt("idusuario");
            }
        }
        return -1;
    }

    /**
     * Busca el ID del estado de solicitud por nombre. (ESTADOSOLICITUD es CHAR(10))
     */
    private int obtenerIdEstadoSolicitudPorNombre(String nombreEstado) throws SQLException {
        // Usamos TRIM y LOWER para manejo de CHAR
        String sql = "SELECT idestadosolicitud FROM estado_solicitud WHERE LOWER(TRIM(estadosolicitud)) = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nombreEstado.trim().toLowerCase());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt("idestadosolicitud");
            }
        }
        return -1;
    }
    
    /**
     * Obtiene el total de tickets para generar el nuevo número secuencial.
     */
    private int obtenerCantidadTickets() throws SQLException {
        String sql = "SELECT COUNT(*) AS total FROM ticket";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getInt("total");
        }
        return 0; // Si no hay, devuelve 0
    }
    
    /**
     * Genera el número de ticket en formato TXXXX.
     */
    private String generarNuevoNumeroTicket() throws SQLException {
        int cantidadTickets = obtenerCantidadTickets();
        int nuevoId = cantidadTickets + 1;
        String idFormateado = String.format("%04d", nuevoId);
        return "T" + idFormateado; // CHAR(5)
    }
    
    /**
     * Crea un Ticket y retorna su ID generado.
     */
    private int crearTicket(int idUsuario, String numeroTicket) throws SQLException {
        // ID_ESTADOTICKET = 1 es el estado inicial (Ej: Abierto/Baja)
        String sql = "INSERT INTO ticket (idestadoticket, idusuario, fechaasignacion, numeroticket) VALUES (?, ?, ?, ?) RETURNING idticket";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, 1); 
            stmt.setInt(2, idUsuario);
            stmt.setDate(3, new java.sql.Date(new Date().getTime()));
            stmt.setString(4, numeroTicket);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt("idticket");
            }
        }
        return -1;
    }
    
    /**
     * Guarda la Solicitud referenciando el Ticket. (DESCRIPCION es CHAR(300))
     */
    private boolean guardarSolicitud(Solicitud solicitud, int idUsuario, int idTipoServicio, int idEstadoSolicitud, int idTicket) throws SQLException {
        String sql = "INSERT INTO solicitud (idusuario, idtiposervicio, idestadosolicitud, idticket, fechacreacion, descripcion) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUsuario);
            stmt.setInt(2, idTipoServicio);
            stmt.setInt(3, idEstadoSolicitud); 
            stmt.setInt(4, idTicket);
            stmt.setDate(5, new java.sql.Date(solicitud.getFechaCreacion().getTime()));

            String descripcion = solicitud.getDescripcion() != null ? solicitud.getDescripcion().trim() : "";
            // Recorte para asegurar que no exceda CHAR(300)
            if (descripcion.length() > 300) {
                descripcion = descripcion.substring(0, 300);
            }
            stmt.setString(6, descripcion);

            return stmt.executeUpdate() > 0;
        }
    }
    
    // =========================================================================
    // --- LÓGICA PRINCIPAL TRANSACCIONAL (USADA POR doPost del Servlet) ---
    // =========================================================================

    /**
     * ✅ MÉTODO ADAPTADO PARA LA WEB: Crea una Solicitud y su Ticket asociado.
     * Utiliza el ID de Servicio directo del formulario.
     */
    public String crearSolicitudConTicketWeb(String correoUsuario, int idTipoServicio, String descripcion) {
        String numeroTicket = null;
        try {
            // Iniciar Transacción
            conn.setAutoCommit(false); 

            // 1. Obtener IDs (el ID de servicio ya se tiene)
            int idUsuario = obtenerIdUsuarioPorCorreo(correoUsuario);
            int idEstadoSolicitud = obtenerIdEstadoSolicitudPorNombre("Pendiente"); 

            if (idUsuario == -1) {
                throw new Exception("Error: El usuario '" + correoUsuario + "' no se encontró en la base de datos.");
            }
            if (idEstadoSolicitud == -1) {
                throw new Exception("Error: El estado 'Pendiente' no se encontró en la base de datos.");
            }

            // 2. Generar Ticket y obtener ID del BD
            numeroTicket = generarNuevoNumeroTicket();
            int idTicket = crearTicket(idUsuario, numeroTicket);
            if (idTicket == -1) {
                throw new Exception("Error al crear el registro de Ticket.");
            }

            // 3. Crear Entidad Solicitud y Guardar
            Solicitud solicitud = new Solicitud();
            solicitud.setFechaCreacion(new Date());
            solicitud.setDescripcion(descripcion);

            boolean solicitudGuardada = guardarSolicitud(solicitud, idUsuario, idTipoServicio, idEstadoSolicitud, idTicket);
            
            if (solicitudGuardada) {
                conn.commit(); 
                return numeroTicket;
            } else {
                throw new Exception("Fallo en la inserción de Solicitud.");
            }

        } catch (Exception e) {
            System.err.println("❌ Error CRÍTICO en la transacción de Solicitud: " + e.getMessage());
            try {
                if (conn != null) conn.rollback(); 
            } catch (SQLException ex) {
                System.err.println("Error al hacer rollback: " + ex.getMessage());
            }
            return null;
        } finally {
             try {
                if (conn != null) conn.setAutoCommit(true); 
            } catch (SQLException ex) {
                System.err.println("Error al restablecer auto-commit: " + ex.getMessage());
            }
        }
    }
}