/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package capa_controladora;

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
 * Controlador que gestiona la lógica de negocio para la creación de Solicitudes
 * Maneja tanto operaciones de lectura (listado de servicios) como escritura
 * (creación de solicitudes) Contiene un método estático para la lectura
 * (combobox) y métodos de instancia para la transacción (escritura).
 */
public class CrearSolicitudController {

    // Conexión a la base de datos para los métodos de instancia (Escritura/Transacción)
    // Esta conexión se mantiene durante toda la transacción para garantizar atomicidad
    private final Connection conn;

    /**
     * Constructor usado por el Servlet para el doPost (Transacciones). Recibe
     * una conexión externa que será manejada por el servlet
     *
     * @param conn Conexión a la base de datos ya establecida
     */
    public CrearSolicitudController(Connection conn) {
        this.conn = conn;
    }

    // =========================================================================
    // --- LÓGICA ESTÁTICA PARA LECTURA (USADA POR doGet del Servlet) ---
    // =========================================================================
    /**
     * MÉTODO ESTÁTICO: Obtiene todos los IDs y Nombres de servicio para
     * llenar el JSP. Abre y cierra su propia conexión, imitando el patrón
     * exitoso de GenerarReporteController. Este método es estático porque no
     * necesita estado de instancia y maneja su propia conexión. Se usa para
     * cargar el combobox/dropdown de servicios en el formulario.
     *
     * @return Un mapa de ID del servicio (Integer) a Nombre del servicio
     * (String).
     */
    public static Map<Integer, String> listarTipoServiciosStatic() {
        Map<Integer, String> servicios = new HashMap<>();
        System.out.println("DEBUG: Iniciando listarTipoServiciosStatic");

        // Usamos TRIM(nombreservicio) para evitar los espacios de CHAR(80)
        // CHAR en SQL añade espacios padding, TRIM los elimina para texto limpio
        String sql = "SELECT idtiposervicio, TRIM(nombreservicio) AS nombre_limpio FROM tipo_servicio ORDER BY nombreservicio";

        // La conexión se abre y se cierra dentro de este bloque try-with-resources
        // Esto garantiza que los recursos se liberen automáticamente
        try (Connection connEstatica = ConexionBD.conectar(); PreparedStatement stmt = connEstatica.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {

            System.out.println("DEBUG: Conexión y query OK. Procesando resultados...");
            int count = 0;
            // Iterar sobre todos los servicios retornados por la consulta
            while (rs.next()) {
                int id = rs.getInt("idtiposervicio"); // ID del servicio
                String nombre = rs.getString("nombre_limpio"); // Nombre ya limpiado por TRIM en SQL
                System.out.println("DEBUG: Servicio #" + (++count) + ": ID=" + id + ", Nombre='" + nombre + "' (longitud: " + nombre.length() + ")");
                servicios.put(id, nombre.trim()); // .trim() extra por precaución adicional
            }
            System.out.println("DEBUG: Total servicios cargados: " + servicios.size());
        } catch (SQLException e) {
            // Capturar y loggear cualquier error SQL durante la consulta
            System.err.println("❌ ERROR SQL al listarTipoServiciosStatic: " + e.getMessage());
            e.printStackTrace(); // Detalles completos del error para debugging
        }
        return servicios; // Retornar mapa (puede estar vacío si hubo error)
    }

    // =========================================================================
    // --- MÉTODOS PRIVADOS DAO (USAN this.conn para la Transacción) ---
    // =========================================================================
    /**
     * Busca el ID del Usuario por correo electrónico. Nota: CORREOELECTRONICO
     * es CHAR(25) en la base de datos, por eso se usa TRIM También usa LOWER
     * para hacer la búsqueda case-insensitive
     *
     * @param correo Correo electrónico del usuario a buscar
     * @return ID del usuario o -1 si no se encuentra
     * @throws SQLException si hay error en la consulta
     */
    private int obtenerIdUsuarioPorCorreo(String correo) throws SQLException {
        // Usamos TRIM y LOWER para manejo de CHAR y case-insensitivity
        String sql = "SELECT idusuario FROM usuario WHERE LOWER(TRIM(correoelectronico)) = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            // Normalizar el correo: eliminar espacios y convertir a minúsculas
            stmt.setString(1, correo.trim().toLowerCase());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("idusuario"); // Usuario encontrado
                }
            }
        }
        return -1; // Usuario no encontrado
    }

    /**
     * Busca el ID del estado de solicitud por nombre. Nota: ESTADOSOLICITUD es
     * CHAR(10) en la base de datos
     *
     * @param nombreEstado Nombre del estado a buscar (ej: "Pendiente", "En
     * Proceso")
     * @return ID del estado o -1 si no se encuentra
     * @throws SQLException si hay error en la consulta
     */
    private int obtenerIdEstadoSolicitudPorNombre(String nombreEstado) throws SQLException {
        // Usamos TRIM y LOWER para manejo de CHAR y case-insensitivity
        String sql = "SELECT idestadosolicitud FROM estado_solicitud WHERE LOWER(TRIM(estadosolicitud)) = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            // Normalizar el nombre del estado
            stmt.setString(1, nombreEstado.trim().toLowerCase());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("idestadosolicitud"); // Estado encontrado
                }
            }
        }
        return -1; // Estado no encontrado
    }

    /**
     * Obtiene el total de tickets existentes en la base de datos. Se usa para
     * generar el nuevo número secuencial de ticket.
     *
     * @return Cantidad total de tickets en la tabla
     * @throws SQLException si hay error en la consulta
     */
    private int obtenerCantidadTickets() throws SQLException {
        String sql = "SELECT COUNT(*) AS total FROM ticket";
        try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("total"); // Retornar el conteo
            }
        }
        return 0; // Si no hay registros, devuelve 0
    }

    /**
     * Genera el número de ticket en formato TXXXX (ej: T0001, T0023, T0145).
     * Obtiene la cantidad actual de tickets y genera el siguiente número
     * secuencial. El formato es CHAR(5) en la base de datos.
     *
     * @return Número de ticket generado en formato TXXXX
     * @throws SQLException si hay error al obtener la cantidad de tickets
     */
    private String generarNuevoNumeroTicket() throws SQLException {
        int cantidadTickets = obtenerCantidadTickets(); // Obtener total actual
        int nuevoId = cantidadTickets + 1; // Incrementar para el nuevo ticket
        String idFormateado = String.format("%04d", nuevoId); // Formatear con ceros a la izquierda (0001, 0002, etc.)
        return "T" + idFormateado; // Concatenar prefijo "T" con el número formateado
    }

    /**
     * Crea un nuevo Ticket en la base de datos y retorna su ID generado. El
     * ticket se crea con estado inicial (ID=1) y la fecha actual de asignación.
     *
     * @param idUsuario ID del usuario que crea el ticket
     * @param numeroTicket Número de ticket generado (formato TXXXX)
     * @return ID del ticket insertado o -1 si falla
     * @throws SQLException si hay error en la inserción
     */
    private int crearTicket(int idUsuario, String numeroTicket) throws SQLException {
        // ID_ESTADOTICKET = 1 es el estado inicial (Ej: Abierto/Baja prioridad)
        // RETURNING idticket es una característica de PostgreSQL para obtener el ID generado
        String sql = "INSERT INTO ticket (idestadoticket, idusuario, fechaasignacion, numeroticket) VALUES (?, ?, ?, ?) RETURNING idticket";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, 1); // Estado inicial hardcoded como 1
            stmt.setInt(2, idUsuario); // Usuario creador
            stmt.setDate(3, new java.sql.Date(new Date().getTime())); // Fecha actual
            stmt.setString(4, numeroTicket); // Número de ticket generado

            // Ejecutar y obtener el ID generado
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("idticket"); // Retornar ID del ticket insertado
                }
            }
        }
        return -1; // Fallo en la inserción
    }

    /**
     * Guarda la Solicitud en la base de datos referenciando el Ticket creado.
     * Nota: DESCRIPCION es CHAR(300) en la base de datos, por eso se recorta si
     * es necesario.
     *
     * @param solicitud Objeto Solicitud con los datos
     * @param idUsuario ID del usuario que hace la solicitud
     * @param idTipoServicio ID del tipo de servicio solicitado
     * @param idEstadoSolicitud ID del estado inicial de la solicitud
     * @param idTicket ID del ticket asociado a esta solicitud
     * @return true si se insertó correctamente, false en caso contrario
     * @throws SQLException si hay error en la inserción
     */
    private boolean guardarSolicitud(Solicitud solicitud, int idUsuario, int idTipoServicio, int idEstadoSolicitud, int idTicket) throws SQLException {
        String sql = "INSERT INTO solicitud (idusuario, idtiposervicio, idestadosolicitud, idticket, fechacreacion, descripcion) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUsuario); // Usuario solicitante
            stmt.setInt(2, idTipoServicio); // Tipo de servicio seleccionado
            stmt.setInt(3, idEstadoSolicitud); // Estado inicial (Pendiente)
            stmt.setInt(4, idTicket); // FK al ticket creado anteriormente
            stmt.setDate(5, new java.sql.Date(solicitud.getFechaCreacion().getTime())); // Fecha de creación

            // Procesar la descripción: eliminar espacios y recortar si excede el límite
            String descripcion = solicitud.getDescripcion() != null ? solicitud.getDescripcion().trim() : "";
            // Recorte para asegurar que no exceda CHAR(300)
            if (descripcion.length() > 300) {
                descripcion = descripcion.substring(0, 300); // Truncar a 300 caracteres
            }
            stmt.setString(6, descripcion);

            // Ejecutar inserción y verificar si se insertó al menos 1 fila
            return stmt.executeUpdate() > 0;
        }
    }

    // =========================================================================
    // --- LÓGICA PRINCIPAL TRANSACCIONAL (USADA POR doPost del Servlet) ---
    // =========================================================================
    /**
     * MÉTODO ADAPTADO PARA LA WEB: Crea una Solicitud y su Ticket asociado.
     * Este método orquesta toda la transacción de creación: 1. Busca el ID del
     * usuario por correo 2. Busca el ID del estado "Pendiente" 3. Genera un
     * nuevo número de ticket 4. Crea el ticket en la BD 5. Crea la solicitud
     * asociada al ticket
     *
     * Utiliza transacciones SQL para garantizar que todo se complete o todo se
     * revierta. Utiliza el ID de Servicio directo del formulario (ya no busca
     * por nombre).
     *
     * @param correoUsuario Correo del usuario que crea la solicitud
     * @param idTipoServicio ID del tipo de servicio seleccionado
     * @param descripcion Descripción del problema/solicitud
     * @return Número de ticket generado si fue exitoso, null si falló
     */
    public String crearSolicitudConTicketWeb(String correoUsuario, int idTipoServicio, String descripcion) {
        String numeroTicket = null; // Variable para almacenar el número de ticket generado
        try {
            // Iniciar Transacción: desactivar auto-commit para control manual
            conn.setAutoCommit(false);

            // 1. Obtener IDs necesarios (el ID de servicio ya se tiene como parámetro)
            int idUsuario = obtenerIdUsuarioPorCorreo(correoUsuario);
            int idEstadoSolicitud = obtenerIdEstadoSolicitudPorNombre("Pendiente"); // Estado inicial hardcoded

            // Validar que se encontró el usuario
            if (idUsuario == -1) {
                throw new Exception("Error: El usuario '" + correoUsuario + "' no se encontró en la base de datos.");
            }
            // Validar que existe el estado "Pendiente"
            if (idEstadoSolicitud == -1) {
                throw new Exception("Error: El estado 'Pendiente' no se encontró en la base de datos.");
            }

            // 2. Generar número de Ticket y crear el registro en la tabla TICKET
            numeroTicket = generarNuevoNumeroTicket(); // Genera formato TXXXX
            int idTicket = crearTicket(idUsuario, numeroTicket); // Inserta ticket y obtiene ID
            if (idTicket == -1) {
                throw new Exception("Error al crear el registro de Ticket.");
            }

            // 3. Crear Entidad Solicitud y Guardar en la tabla SOLICITUD
            Solicitud solicitud = new Solicitud();
            solicitud.setFechaCreacion(new Date()); // Fecha actual
            solicitud.setDescripcion(descripcion); // Descripción del problema

            // Guardar la solicitud con todas las referencias necesarias
            boolean solicitudGuardada = guardarSolicitud(solicitud, idUsuario, idTipoServicio, idEstadoSolicitud, idTicket);

            if (solicitudGuardada) {
                // Si todo salió bien, confirmar la transacción
                conn.commit();
                return numeroTicket; // Retornar el número de ticket generado como señal de éxito
            } else {
                // Si la inserción de solicitud falló, lanzar excepción
                throw new Exception("Fallo en la inserción de Solicitud.");
            }

        } catch (Exception e) {
            // Capturar cualquier error durante la transacción
            System.err.println(" Error CRÍTICO en la transacción de Solicitud: " + e.getMessage());
            try {
                // Revertir todos los cambios realizados en la transacción
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                System.err.println("Error al hacer rollback: " + ex.getMessage());
            }
            return null; // Retornar null indicando fallo en la operación
        } finally {
            // Siempre restaurar el auto-commit al estado original
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                }
            } catch (SQLException ex) {
                System.err.println("Error al restablecer auto-commit: " + ex.getMessage());
            }
        }
    }
}
