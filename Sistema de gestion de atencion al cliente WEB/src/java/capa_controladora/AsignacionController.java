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

/**
 * Controlador que maneja toda la lógica de negocio relacionada con la
 * asignación de tickets Actúa como intermediario entre la capa de presentación
 * (Servlet) y la capa de datos (Base de datos)
 */
public class AsignacionController {

    // Atributos de instancia para mantener el estado durante el proceso de asignación
    private Solicitud solicitudSeleccionada; // Solicitud actual que se está procesando
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); // Formato para fechas
    private List<Solicitud> listaTodasSolicitudes; // Cache de todas las solicitudes cargadas
    private List<String> listaTodosCargos; // Cache de todos los cargos disponibles
    private List<Usuario> listaTodosTecnicos; // Cache de todos los técnicos cargados en memoria

    // Consultas SQL predefinidas como constantes para reutilización y mantenibilidad
    // Obtener todos los tipos de cargo únicos de la tabla TIPO_USUARIO
    private static final String SQL_OBTENER_CARGOS = "SELECT DISTINCT cargo FROM TIPO_USUARIO";

    // Obtener técnicos (nombres, apellidos y cargo) filtrados por un cargo específico
    private static final String SQL_OBTENER_TECNICOS_POR_CARGO = "SELECT u.nombres, u.apellidos, tu.cargo FROM USUARIO u JOIN TIPO_USUARIO tu ON u.idtipousuario = tu.idtipousuario WHERE tu.cargo = ?";

    // Obtener el ID de un técnico buscando por nombres y apellidos
    private static final String SQL_OBTENER_ID_TECNICO_BY_NAME
            = "SELECT idusuario FROM USUARIO WHERE nombres = ? AND apellidos = ?";

    // Consulta compleja que obtiene todas las solicitudes con sus relaciones (ticket, técnico, estados, servicios)
    private static final String SQL_OBTENER_SOLICITUDES
            = "SELECT s.idsolicitud, s.fechacreacion, s.descripcion, t.numeroticket, t.fechaasignacion, "
            + "   u.idusuario AS idtecnico, u.nombres AS tecnico_nombres, u.apellidos AS tecnico_apellidos, "
            + "   tu.cargo AS tecnico_cargo, t.idestadoticket, es.estadosolicitud, et.nivelprioridad, ts.nombreservicio "
            + "FROM SOLICITUD s "
            + "JOIN TICKET t ON s.idticket = t.idticket "
            + "JOIN ESTADO_SOLICITUD es ON s.idestadosolicitud = es.idestadosolicitud "
            + "JOIN TIPO_SERVICIO ts ON s.idtiposervicio = ts.idtiposervicio "
            + "LEFT JOIN ESTADO_TICKET et ON t.idestadoticket = et.idestadoticket "
            + "LEFT JOIN USUARIO u ON t.idusuario = u.idusuario "
            + "LEFT JOIN TIPO_USUARIO tu ON u.idtipousuario = tu.idtipousuario "
            + "ORDER BY s.idsolicitud DESC";

    // Obtener el ID de un estado de ticket basándose en el nivel de prioridad
    private static final String SQL_ID_PRIORIDAD = "SELECT idestadoticket FROM ESTADO_TICKET WHERE TRIM(nivelprioridad) = ?";

    // Obtener el ID de un estado de solicitud basándose en su nombre
    private static final String SQL_ID_ESTADO_SOLICITUD = "SELECT idestadosolicitud FROM ESTADO_SOLICITUD WHERE TRIM(estadosolicitud) = ?";

    // Actualizar un ticket con nueva prioridad, técnico asignado y fecha de asignación
    private static final String SQL_UPDATE_TICKET = "UPDATE TICKET SET idestadoticket = ?, idusuario = ?, fechaasignacion = ? WHERE numeroticket = ?";

    // Actualizar el estado de una solicitud identificándola por el número de ticket
    private static final String SQL_UPDATE_SOLICITUD = "UPDATE SOLICITUD SET idestadosolicitud = ? WHERE idticket = (SELECT idticket FROM TICKET WHERE numeroticket = ?)";

    // Obtener todos los niveles de prioridad disponibles
    private static final String SQL_OBTENER_PRIORIDADES = "SELECT NIVELPRIORIDAD FROM ESTADO_TICKET";

    // Obtener todos los estados de solicitud disponibles
    private static final String SQL_OBTENER_ESTADOS_SOLICITUD = "SELECT ESTADOSOLICITUD FROM ESTADO_SOLICITUD";

    // Obtener todos los números de ticket ordenados de forma descendente
    private static final String SQL_OBTENER_TICKETS = "SELECT numeroticket FROM TICKET ORDER BY numeroticket DESC";

    /**
     * Constructor que inicializa el controlador Prepara las listas vacías para
     * almacenar datos
     */
    public AsignacionController() {
        this.solicitudSeleccionada = null;
        this.listaTodosTecnicos = new ArrayList<>();
    }

    /**
     * Método auxiliar privado para obtener un ID de la base de datos Recibe una
     * consulta SQL y un valor a buscar, retorna el ID correspondiente
     *
     * @param conn Conexión a la base de datos
     * @param sql Consulta SQL preparada para buscar el ID
     * @param valor Valor a buscar en la consulta
     * @return ID encontrado
     * @throws SQLException si no se encuentra el valor o hay error en la BD
     */
    private int obtenerId(Connection conn, String sql, String valor) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, valor.trim()); // Trimear para eliminar espacios en blanco
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1); // Retornar el primer campo (el ID)
                }
            }
        }
        // Si no se encuentra, lanzar excepción
        throw new SQLException("Error: No se encontró ID para el valor: " + valor);
    }

    /**
     * Método específico para obtener el ID de un técnico buscando por nombres y
     * apellidos
     *
     * @param conn Conexión a la base de datos
     * @param nombres Nombres del técnico
     * @param apellidos Apellidos del técnico
     * @return ID del técnico
     * @throws SQLException si no se encuentra el técnico
     */
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

    /**
     * Busca una solicitud en la lista cargada en memoria por su número de
     * ticket
     *
     * @param numeroTicket Número de ticket a buscar
     * @return Solicitud encontrada o null si no existe
     */
    public Solicitud buscarSolicitudPorTicket(String numeroTicket) {
        // Validar que los parámetros no sean nulos
        if (numeroTicket == null || listaTodasSolicitudes == null) {
            return null;
        }

        // Recorrer la lista de solicitudes buscando coincidencia
        for (Solicitud s : listaTodasSolicitudes) {
            if (s.getTicket().getNumeroTicket().equals(numeroTicket)) {
                return s;
            }
        }
        return null; // No se encontró
    }

    /**
     * Busca un técnico en la lista cargada en memoria por su nombre completo
     * Incluye múltiples mecanismos de búsqueda: exacta y case-insensitive
     * Contiene logs de depuración extensivos para diagnosticar problemas
     *
     * @param nombreCompleto Nombre completo del técnico (Nombres Apellidos)
     * @return Usuario (técnico) encontrado o null si no existe
     */
    public Usuario buscarTecnicoPorNombre(String nombreCompleto) {
        // Log de depuración: información sobre el parámetro recibido
        System.out.println("DEBUG: Buscando técnico: '" + nombreCompleto + "' (length: " + (nombreCompleto != null ? nombreCompleto.length() : 0) + ")");
        System.out.println("DEBUG: Total técnicos en lista: " + (listaTodosTecnicos != null ? listaTodosTecnicos.size() : 0));

        // Validar que los parámetros sean válidos
        if (nombreCompleto == null || listaTodosTecnicos == null || listaTodosTecnicos.isEmpty()) {
            System.out.println("DEBUG: Lista de técnicos vacía o nombre nulo");
            return null;
        }

        // Normalizar el nombre de búsqueda: eliminar espacios extra entre palabras y al inicio/final
        String nombreBuscar = nombreCompleto.trim().replaceAll("\\s+", " ");
        System.out.println("DEBUG: Nombre normalizado: '" + nombreBuscar + "'");

        // Dividir el nombre completo en nombres y apellidos (separados por espacio)
        String[] partes = nombreBuscar.split("\\s+", 2); // Dividir en máximo 2 partes
        if (partes.length < 2) {
            // Si no hay al menos 2 partes, el formato es inválido
            System.out.println("DEBUG: Formato de nombre inválido: " + nombreBuscar);
            return null;
        }

        // Separar en nombres (primera parte) y apellidos (segunda parte)
        String nombres = partes[0].trim();
        String apellidos = partes[1].trim();

        System.out.println("DEBUG: Buscando - Nombres: '" + nombres + "', Apellidos: '" + apellidos + "'");

        // Recorrer la lista de técnicos buscando coincidencia
        for (Usuario u : listaTodosTecnicos) {
            String nombresDB = u.getNombres().trim();
            String apellidosDB = u.getApellidos().trim();

            System.out.println("DEBUG: Comparando con - Nombres: '" + nombresDB + "', Apellidos: '" + apellidosDB + "'");

            // Primer intento: Comparación exacta (distingue mayúsculas/minúsculas)
            if (nombresDB.equals(nombres) && apellidosDB.equals(apellidos)) {
                System.out.println("DEBUG: ¡Técnico encontrado (exacto)!");
                return u;
            }

            // Segundo intento: Comparación sin distinguir mayúsculas/minúsculas
            if (nombresDB.equalsIgnoreCase(nombres) && apellidosDB.equalsIgnoreCase(apellidos)) {
                System.out.println("DEBUG: ¡Técnico encontrado (case-insensitive)!");
                return u;
            }
        }

        // No se encontró ninguna coincidencia
        System.out.println("DEBUG: Técnico NO encontrado");
        return null;
    }

    /**
     * Obtiene todos los cargos disponibles desde la base de datos Filtra solo
     * los cargos relevantes: Cliente, Programador y Técnico (excluye Admin)
     *
     * @return Lista de nombres de cargos
     */
    public List<String> obtenerCargos() {
        List<String> cargos = new ArrayList<>();
        try (Connection conn = ConexionBD.conectar(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(SQL_OBTENER_CARGOS)) {

            // Validar que la conexión se estableció correctamente
            if (conn == null) {
                return cargos;
            }

            // Iterar sobre los resultados
            while (rs.next()) {
                String cargo = rs.getString("cargo").trim();
                // Filtrar solo los cargos deseados (excluir Admin)
                if (cargo.equalsIgnoreCase("Cliente")
                        || cargo.equalsIgnoreCase("Programador")
                        || cargo.equalsIgnoreCase("Técnico")) {
                    cargos.add(cargo);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener cargos: " + e.getMessage());
        }
        return cargos;
    }

    /**
     * Obtiene todas las prioridades de ticket disponibles desde la base de
     * datos
     *
     * @return Lista de niveles de prioridad
     */
    public List<String> obtenerPrioridades() {
        List<String> prioridades = new ArrayList<>();
        try (Connection conn = ConexionBD.conectar(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(SQL_OBTENER_PRIORIDADES)) {

            if (conn == null) {
                return prioridades;
            }

            while (rs.next()) {
                // Agregar cada nivel de prioridad a la lista, eliminando espacios
                prioridades.add(rs.getString("NIVELPRIORIDAD").trim());
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener prioridades: " + e.getMessage());
        }
        return prioridades;
    }

    /**
     * Obtiene todos los estados de solicitud disponibles desde la base de datos
     *
     * @return Lista de estados de solicitud
     */
    public List<String> obtenerEstadosSolicitud() {
        List<String> estados = new ArrayList<>();
        try (Connection conn = ConexionBD.conectar(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(SQL_OBTENER_ESTADOS_SOLICITUD)) {

            if (conn == null) {
                return estados;
            }

            while (rs.next()) {
                // Agregar cada estado a la lista, eliminando espacios
                estados.add(rs.getString("ESTADOSOLICITUD").trim());
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener estados de solicitud: " + e.getMessage());
        }
        return estados;
    }

    /**
     * Obtiene todos los números de ticket existentes desde la base de datos
     * Ordenados de forma descendente (más recientes primero)
     *
     * @return Lista de números de ticket
     */
    public List<String> obtenerTodosNumerosTicket() {
        List<String> numerosTicket = new ArrayList<>();

        try (Connection conn = ConexionBD.conectar(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(SQL_OBTENER_TICKETS)) {

            if (conn == null) {
                return numerosTicket;
            }

            while (rs.next()) {
                // Agregar cada número de ticket a la lista
                numerosTicket.add(rs.getString("numeroticket").trim());
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener números de ticket: " + e.getMessage());
        }
        return numerosTicket;
    }

    /**
     * Obtiene los nombres completos de todos los técnicos que tienen un cargo
     * específico Importante: También carga los técnicos en la lista interna
     * (listaTodosTecnicos) Esta lista se usa luego para buscar técnicos por
     * nombre
     *
     * @param cargo Cargo por el cual filtrar (ej: "Técnico", "Programador")
     * @return Lista de nombres completos de técnicos
     */
    public List<String> obtenerNombresTecnicosPorCargo(String cargo) {
        List<String> nombresTecnicos = new ArrayList<>();
        // Formatear el cargo con padding a 10 caracteres (aparentemente así está en la BD)
        String cargoPadded = String.format("%-10s", cargo);

        try (Connection conn = ConexionBD.conectar(); PreparedStatement ps = conn.prepareStatement(SQL_OBTENER_TECNICOS_POR_CARGO)) {

            if (conn == null) {
                return nombresTecnicos;
            }

            // Establecer el parámetro de búsqueda (cargo)
            ps.setString(1, cargoPadded);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // Crear objeto TipoUsuario con el cargo
                    TipoUsuario tipo = new TipoUsuario(rs.getString("cargo").trim());
                    Usuario tecnico = new Usuario();

                    // Obtener nombres y apellidos del técnico
                    String nombres = rs.getString("nombres").trim();
                    String apellidos = rs.getString("apellidos").trim();

                    // Configurar el objeto Usuario
                    tecnico.setNombres(nombres);
                    tecnico.setApellidos(apellidos);
                    tecnico.setTipoUsuario(tipo);

                    // IMPORTANTE: Agregar el técnico a la lista interna del controlador
                    // Esta lista se usa luego en buscarTecnicoPorNombre()
                    this.listaTodosTecnicos.add(tecnico);

                    // Crear nombre completo y agregarlo a la lista de retorno
                    String nombreCompleto = nombres + " " + apellidos;
                    nombresTecnicos.add(nombreCompleto);

                    // Log de depuración para verificar la carga
                    System.out.println("DEBUG: Técnico cargado - '" + nombreCompleto + "' (bytes: " + nombreCompleto.length() + ") del cargo: " + cargo);
                }
            }
            System.out.println("DEBUG: Total técnicos del cargo '" + cargo + "': " + nombresTecnicos.size());
        } catch (SQLException e) {
            System.err.println("Error al obtener técnicos por cargo: " + e.getMessage());
            e.printStackTrace();
        }
        return nombresTecnicos;
    }

    /**
     * Obtiene todas las solicitudes desde la base de datos con sus relaciones
     * completas Incluye: estado, servicio, ticket, prioridad, técnico asignado,
     * fechas, etc. Los resultados se almacenan en listaTodasSolicitudes para
     * uso posterior
     *
     * @return Lista completa de solicitudes
     */
    public List<Solicitud> obtenerTodasSolicitudes() {
        List<Solicitud> solicitudes = new ArrayList<>();
        try (Connection conn = ConexionBD.conectar(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(SQL_OBTENER_SOLICITUDES)) {

            if (conn == null) {
                return solicitudes;
            }

            while (rs.next()) {
                // Crear objetos de los estados y servicios
                EstadoSolicitud estado = new EstadoSolicitud(rs.getString("estadosolicitud").trim());
                TipoServicio servicio = new TipoServicio(rs.getString("nombreservicio").trim());

                // Inicializar variables opcionales (pueden ser null)
                Usuario tecnicoAsignado = null;
                Date fechaAsignacion = rs.getDate("fechaasignacion");
                EstadoTicket nivelPrioridad = null;

                // Si hay nivel de prioridad asignado, crear el objeto
                if (rs.getString("nivelprioridad") != null) {
                    nivelPrioridad = new EstadoTicket(rs.getString("nivelprioridad").trim());
                }

                // Si hay técnico asignado (idtecnico != 0), crear el objeto Usuario
                if (rs.getInt("idtecnico") != 0) {
                    TipoUsuario tipoTecnico = new TipoUsuario(rs.getString("tecnico_cargo").trim());
                    tecnicoAsignado = new Usuario();
                    tecnicoAsignado.setNombres(rs.getString("tecnico_nombres").trim());
                    tecnicoAsignado.setApellidos(rs.getString("tecnico_apellidos").trim());
                    tecnicoAsignado.setTipoUsuario(tipoTecnico);
                }

                // Crear objeto Ticket con todos sus datos
                Ticket ticket = new Ticket(nivelPrioridad, fechaAsignacion, rs.getString("numeroticket").trim(), tecnicoAsignado);

                // Crear objeto Solicitud completo con todos sus componentes
                Solicitud s = new Solicitud(null, servicio, estado, ticket, rs.getDate("fechacreacion"), rs.getString("descripcion").trim());
                solicitudes.add(s);
            }
            // Guardar las solicitudes en el atributo de instancia para uso posterior
            this.listaTodasSolicitudes = solicitudes;
        } catch (SQLException e) {
            System.err.println("Error al obtener solicitudes: " + e.getMessage());
        }
        return solicitudes;
    }

    /**
     * Ejecuta la asignación de un ticket a un técnico en la base de datos
     * Actualiza tanto la tabla TICKET (prioridad, técnico, fecha) como
     * SOLICITUD (estado) Utiliza transacciones para garantizar consistencia
     * (commit o rollback completo)
     *
     * @param solicitud Objeto Solicitud con los datos de la asignación
     * @return true si la asignación fue exitosa, false en caso contrario
     * @throws Exception si ocurre algún error durante el proceso
     */
    public boolean ejecutarAsignacion(Solicitud solicitud) throws Exception {
        Connection conn = ConexionBD.conectar();
        if (conn == null) {
            return false;
        }

        boolean exito = false;

        try {
            // Desactivar auto-commit para manejar transacción manual
            conn.setAutoCommit(false);

            // Extraer datos necesarios del objeto Solicitud
            String prioridad = solicitud.getTicket().getEstadoTicket().getNivelPrioridad();
            String estado = solicitud.getEstadoSolicitud().getEstadoSolicitud();
            Usuario tecnicoAsignado = solicitud.getTicket().getTecnicoAsignado();

            // Obtener IDs correspondientes de las tablas de catálogo
            int idPrioridad = obtenerId(conn, SQL_ID_PRIORIDAD, prioridad);
            int idNuevoEstado = obtenerId(conn, SQL_ID_ESTADO_SOLICITUD, estado);
            int idTecnico = obtenerIdTecnico(conn, tecnicoAsignado.getNombres(), tecnicoAsignado.getApellidos());

            // Primera actualización: Tabla TICKET
            // Actualizar prioridad, técnico asignado y fecha de asignación
            try (PreparedStatement psTicket = conn.prepareStatement(SQL_UPDATE_TICKET)) {
                psTicket.setInt(1, idPrioridad);
                psTicket.setInt(2, idTecnico);
                psTicket.setDate(3, new java.sql.Date(solicitud.getTicket().getFechaAsignacion().getTime()));
                psTicket.setString(4, solicitud.getTicket().getNumeroTicket());
                psTicket.executeUpdate();
            }

            // Segunda actualización: Tabla SOLICITUD
            // Actualizar el estado de la solicitud asociada al ticket
            try (PreparedStatement psSolicitud = conn.prepareStatement(SQL_UPDATE_SOLICITUD)) {
                psSolicitud.setInt(1, idNuevoEstado);
                psSolicitud.setString(2, solicitud.getTicket().getNumeroTicket());
                psSolicitud.executeUpdate();
            }

            // Si ambas operaciones fueron exitosas, confirmar la transacción
            conn.commit();
            exito = true;

        } catch (SQLException e) {
            // Si ocurre algún error, imprimir el error
            System.err.println("Error en la transacción de asignación: " + e.getMessage());
            try {
                // Revertir todos los cambios realizados en la transacción
                conn.rollback();
            } catch (SQLException ex) {
                System.err.println("Error al hacer rollback: " + ex.getMessage());
            }
            // Lanzar excepción para notificar al servlet del error
            throw new Exception("Fallo en la asignación: " + e.getMessage(), e);
        } finally {
            // Siempre cerrar la conexión, haya habido éxito o error
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                // Ignorar errores al cerrar
            }
        }
        return exito;
    }
}
