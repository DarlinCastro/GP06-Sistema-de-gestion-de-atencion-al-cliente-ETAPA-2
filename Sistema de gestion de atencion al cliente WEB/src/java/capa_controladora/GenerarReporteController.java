/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package capa_controladora;

import base_datos.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Clase controladora (Controller) para la generación de reportes. Contiene la
 * lógica para obtener datos de tickets y servicios desde la base de datos,
 * incluyendo filtros y normalización de cadenas para búsquedas.
 */
public class GenerarReporteController {

    /**
     * Data Transfer Object (DTO) o clase interna para contener la información
     * completa de un ticket/solicitud para el reporte. Los campos son 'final'
     * para garantizar inmutabilidad una vez creados.
     */
    public static class ReporteData {

        private final String numeroTicket;
        private final Date fechaCreacion;
        private final String estadoTicket;
        private final String tipoServicio;
        private final String descripcionServicio;
        private final String nombreCliente;
        private final Date fechaAsignacion;
        private final String cargoEncargado;
        private final String nombreEncargadoSoporte;

        /**
         * Constructor para inicializar todos los campos del DTO.
         */
        public ReporteData(String numeroTicket, Date fechaCreacion, String estadoTicket, String tipoServicio, String descripcionServicio, String nombreCliente, Date fechaAsignacion, String cargoEncargado, String nombreEncargadoSoporte) {
            this.numeroTicket = numeroTicket;
            this.fechaCreacion = fechaCreacion;
            this.estadoTicket = estadoTicket;
            this.tipoServicio = tipoServicio;
            this.descripcionServicio = descripcionServicio;
            this.nombreCliente = nombreCliente;
            this.fechaAsignacion = fechaAsignacion;
            this.cargoEncargado = cargoEncargado;
            this.nombreEncargadoSoporte = nombreEncargadoSoporte;
        }

        // --- Getters ---
        // Métodos para acceder a los datos privados desde fuera de la clase.
        public String getNumeroTicket() {
            return numeroTicket;
        }

        public Date getFechaCreacion() {
            return fechaCreacion;
        }

        public String getEstadoTicket() {
            return estadoTicket;
        }

        public String getTipoServicio() {
            return tipoServicio;
        }

        public String getDescripcionServicio() {
            return descripcionServicio;
        }

        public String getNombreCliente() {
            return nombreCliente;
        }

        public Date getFechaAsignacion() {
            return fechaAsignacion;
        }

        public String getCargoEncargado() {
            return cargoEncargado;
        }

        public String getNombreEncargadoSoporte() {
            return nombreEncargadoSoporte;
        }
    }

    /**
     * DTO para contener los datos del filtro de servicio (solo el nombre del
     * servicio). Se usa para poblar los dropdowns/comboboxes en la vista.
     */
    public static class TipoServicio {

        private final String nombre;

        public TipoServicio(String nombre) {
            this.nombre = nombre;
        }

        public String getNombre() {
            return nombre;
        }
    }

    // ====================================================================
    // | CONSTANTE SQL BASE
    // ====================================================================
    /**
     * Consulta SQL base que obtiene todos los campos necesarios para el
     * reporte, uniendo múltiples tablas (SOLICITUD, TICKET, ESTADO_SOLICITUD,
     * TIPO_SERVICIO, USUARIO (cliente y encargado), TIPO_USUARIO). Se usan
     * `LEFT JOIN` para el encargado ya que un ticket puede no estar asignado
     * aún.
     */
    private static final String CONSULTA_BASE_REPORTE
            = "SELECT "
            + "    TK.NUMEROTICKET, S.FECHACREACION, ES.ESTADOSOLICITUD, "
            + "    TS.NOMBRESERVICIO, S.DESCRIPCION, C.NOMBRES || ' ' || C.APELLIDOS AS NOMBRE_CLIENTE, "
            + "    TK.FECHAASIGNACION, TU.CARGO AS CARGO_ENCARGADO, E.NOMBRES || ' ' || E.APELLIDOS AS NOMBRE_ENCARGADO "
            + "FROM SOLICITUD S "
            + "JOIN TICKET TK ON S.IDTICKET = TK.IDTICKET "
            + "JOIN ESTADO_SOLICITUD ES ON S.IDESTADOSOLICITUD = ES.IDESTADOSOLICITUD "
            + "JOIN TIPO_SERVICIO TS ON S.IDTIPOSERVICIO = TS.IDTIPOSERVICIO "
            + "JOIN USUARIO C ON S.IDUSUARIO = C.IDUSUARIO "
            // LEFT JOIN para el encargado de soporte, puede ser nulo
            + "LEFT JOIN USUARIO E ON TK.IDUSUARIO = E.IDUSUARIO "
            // LEFT JOIN para el cargo del encargado, puede ser nulo
            + "LEFT JOIN TIPO_USUARIO TU ON E.IDTIPOUSUARIO = TU.IDTIPOUSUARIO ";

    // ====================================================================
    // | MÉTODOS DE APOYO Y NORMALIZACIÓN 
    // ====================================================================
    /**
     * Normaliza la cadena de texto (el nombre del servicio a buscar) eliminando
     * tildes/acentos y la letra 'ñ' para permitir una comparación más simple.
     * Esto se hace en Java *antes* de la consulta para facilitar el filtrado en
     * el SQL posterior.
     *
     * @param texto La cadena a normalizar (e.g., "Instalación").
     * @return La cadena normalizada en minúsculas (e.g., "instalacion").
     */
    private String normalizarCadena(String texto) {
        if (texto == null) {
            return null;
        }
        // 1. Convertir a minúsculas
        String normalizado = texto.toLowerCase();
        // 2. Reemplazar caracteres especiales (tildes/ñ)
        normalizado = normalizado.replaceAll("[áäàâ]", "a");
        normalizado = normalizado.replaceAll("[éëèê]", "e");
        normalizado = normalizado.replaceAll("[íïìî]", "i");
        normalizado = normalizado.replaceAll("[óöòô]", "o");
        normalizado = normalizado.replaceAll("[úüùû]", "u");
        normalizado = normalizado.replaceAll("ñ", "n");
        return normalizado;
    }

    // ====================================================================
    // | MÉTODOS DE ACCESO A DATOS (DAO)
    // ====================================================================
    /**
     * Obtiene la lista de tipos de servicio para el dropdown de filtros.
     * Utiliza `RTRIM` en la consulta SQL para limpiar espacios en blanco al
     * final si la columna `NOMBRESERVICIO` es de tipo CHAR(n).
     *
     * @return Una lista de objetos TipoServicio.
     */
    public List<TipoServicio> cargarTiposServicio() {
        List<TipoServicio> listaServicios = new ArrayList<>();
        // RTRIM quita los espacios a la derecha que a veces trae un campo CHAR(n)
        String sql = "SELECT RTRIM(NOMBRESERVICIO) AS NOMBRESERVICIO_LIMPIO FROM TIPO_SERVICIO ORDER BY NOMBRESERVICIO";

        // Uso de try-with-resources para asegurar el cierre de Connection, PreparedStatement y ResultSet.
        try (Connection conn = ConexionBD.conectar(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String nombre = rs.getString("NOMBRESERVICIO_LIMPIO");
                // trim() adicional en Java para cualquier espacio restante
                listaServicios.add(new TipoServicio(nombre.trim()));
            }
        } catch (SQLException e) {
            // Manejo de errores de SQL
            System.err.println("Error SQL al obtener Tipos de Servicio: " + e.getMessage());
        }
        return listaServicios;
    }

    /**
     * Ejecuta la consulta SQL, mapeando el ResultSet a la lista de ReporteData.
     * Es un método auxiliar interno para evitar duplicar el código de ejecución
     * y mapeo.
     *
     * @param sql La consulta SQL a ejecutar.
     * @param filtroServicio El valor del parámetro para el WHERE, o null si no
     * hay filtro.
     * @return Una lista de objetos ReporteData.
     */
    private List<ReporteData> ejecutarConsultaReporte(String sql, String filtroServicio) {
        List<ReporteData> listaReportes = new ArrayList<>();
        try (Connection conn = ConexionBD.conectar(); PreparedStatement ps = conn.prepareStatement(sql)) {

            if (filtroServicio != null) {
                // Si hay filtro, se establece el parámetro `?` en la consulta.
                // El valor ya viene normalizado y limpio (aunque con el trim() se asegura).
                ps.setString(1, filtroServicio.trim());
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // Mapeo de los resultados a un objeto ReporteData.
                    // Se usa .trim() en todas las cadenas para limpiar posibles espacios de campos CHAR/VARCHAR de la DB.
                    ReporteData data = new ReporteData(
                            rs.getString("NUMEROTICKET").trim(),
                            rs.getDate("FECHACREACION"),
                            rs.getString("ESTADOSOLICITUD").trim(),
                            rs.getString("NOMBRESERVICIO").trim(),
                            rs.getString("DESCRIPCION").trim(),
                            rs.getString("NOMBRE_CLIENTE").trim(),
                            rs.getDate("FECHAASIGNACION"),
                            // Manejo de valores NULOS para encargado (ticket sin asignar)
                            (rs.getString("CARGO_ENCARGADO") != null) ? rs.getString("CARGO_ENCARGADO").trim() : "N/A",
                            (rs.getString("NOMBRE_ENCARGADO") != null) ? rs.getString("NOMBRE_ENCARGADO").trim() : "PENDIENTE"
                    );
                    listaReportes.add(data);
                }
            }
        } catch (SQLException e) {
            // Manejo de errores de SQL
            System.err.println("¡ERROR CRÍTICO! Falló la ejecución de la consulta. Detalle: " + e.getMessage());
        }
        return listaReportes;
    }

    /**
     * Obtiene todos los tickets sin aplicar ningún filtro.
     *
     * @return Una lista de ReporteData.
     */
    public List<ReporteData> obtenerReporteGeneral() {
        // Concatenamos la consulta base con la cláusula de ordenamiento.
        String sql = CONSULTA_BASE_REPORTE + " ORDER BY S.FECHACREACION DESC";
        // Ejecutamos sin filtro (null)
        return ejecutarConsultaReporte(sql, null);
    }

    /**
     * Obtiene los tickets filtrados por el nombre del servicio. Se aplica una
     * lógica de normalización robusta tanto en Java como en SQL para manejar
     * diferencias de formato.
     *
     * @param nombreServicio El nombre del servicio recibido desde la vista.
     * @return Una lista de ReporteData con los tickets filtrados.
     */
    public List<ReporteData> obtenerReporteFiltrado(String nombreServicio) {

        // 1. Normalizamos el parámetro que viene del JSP (eliminar tildes/ñ/mayúsculas en Java)
        String nombreNormalizado = normalizarCadena(nombreServicio);

        // 2. Construcción de la consulta con la cláusula WHERE y doble normalización.
        String sql = CONSULTA_BASE_REPORTE
                // WHERE con normalización robusta en SQL:
                // - RTRIM(TS.NOMBRESERVICIO): Quita espacios de relleno del campo de la DB (e.g., CHAR).
                // - REPLACE(..., ' ', ''): Quita espacios internos (e.g., "Servicio X" -> "ServicioX").
                // - LOWER(...): Convierte a minúsculas.
                // Esto se compara con el parámetro '?', al que se le aplica la misma normalización.
                + " WHERE LOWER(REPLACE(RTRIM(TS.NOMBRESERVICIO), ' ', '')) = LOWER(REPLACE(?, ' ', '')) "
                + " ORDER BY S.FECHACREACION DESC";

        // 3. Ejecutamos la consulta con el parámetro ya normalizado por el método normalizarCadena().
        return ejecutarConsultaReporte(sql, nombreNormalizado);
    }
}
