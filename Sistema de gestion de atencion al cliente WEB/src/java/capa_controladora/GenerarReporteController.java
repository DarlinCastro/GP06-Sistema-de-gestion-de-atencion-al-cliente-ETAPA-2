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

public class GenerarReporteController {

    // ====================================================================
    // | CLASES ESTRUCTURALES DE DATOS (DTOs)
    // ====================================================================
    /**
     * DTO para contener los datos de una fila del reporte.
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
     * DTO para contener los datos del filtro de servicio.
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
            + "LEFT JOIN USUARIO E ON TK.IDUSUARIO = E.IDUSUARIO "
            + "LEFT JOIN TIPO_USUARIO TU ON E.IDTIPOUSUARIO = TU.IDTIPOUSUARIO ";

    // ====================================================================
    // | MÉTODOS DE APOYO Y NORMALIZACIÓN 
    // ====================================================================
    /**
     * Normaliza la cadena eliminando tildes y ñ para permitir una comparación
     * SQL simple. Esta función se ejecuta en Java antes de la consulta.
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
     * Obtiene la lista de tipos de servicio para el dropdown. Usa RTRIM para
     * enviar nombres limpios al JSP.
     */
    public List<TipoServicio> cargarTiposServicio() {
        List<TipoServicio> listaServicios = new ArrayList<>();
        String sql = "SELECT RTRIM(NOMBRESERVICIO) AS NOMBRESERVICIO_LIMPIO FROM TIPO_SERVICIO ORDER BY NOMBRESERVICIO";

        try (Connection conn = ConexionBD.conectar(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String nombre = rs.getString("NOMBRESERVICIO_LIMPIO");
                listaServicios.add(new TipoServicio(nombre.trim()));
            }
        } catch (SQLException e) {
            System.err.println("Error SQL al obtener Tipos de Servicio: " + e.getMessage());
        }
        return listaServicios;
    }

    /**
     * Ejecuta la consulta SQL, mapeando el ResultSet a la lista de ReporteData.
     */
    private List<ReporteData> ejecutarConsultaReporte(String sql, String filtroServicio) {
        List<ReporteData> listaReportes = new ArrayList<>();
        try (Connection conn = ConexionBD.conectar(); PreparedStatement ps = conn.prepareStatement(sql)) {

            if (filtroServicio != null) {
                // El parámetro '?' recibe el nombre ya normalizado
                ps.setString(1, filtroServicio.trim());
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // Mapeo seguro, con .trim() para limpiar los datos que vienen del CHAR de la DB
                    ReporteData data = new ReporteData(
                            rs.getString("NUMEROTICKET").trim(),
                            rs.getDate("FECHACREACION"),
                            rs.getString("ESTADOSOLICITUD").trim(),
                            rs.getString("NOMBRESERVICIO").trim(),
                            rs.getString("DESCRIPCION").trim(),
                            rs.getString("NOMBRE_CLIENTE").trim(),
                            rs.getDate("FECHAASIGNACION"),
                            (rs.getString("CARGO_ENCARGADO") != null) ? rs.getString("CARGO_ENCARGADO").trim() : "N/A",
                            (rs.getString("NOMBRE_ENCARGADO") != null) ? rs.getString("NOMBRE_ENCARGADO").trim() : "PENDIENTE"
                    );
                    listaReportes.add(data);
                }
            }
        } catch (SQLException e) {
            System.err.println("¡ERROR CRÍTICO! Falló la ejecución de la consulta. Detalle: " + e.getMessage());
        }
        return listaReportes;
    }

    /**
     * Obtiene todos los tickets sin filtro.
     */
    public List<ReporteData> obtenerReporteGeneral() {
        String sql = CONSULTA_BASE_REPORTE + " ORDER BY S.FECHACREACION DESC";
        return ejecutarConsultaReporte(sql, null);
    }

    /**
     * Obtiene los tickets filtrados por el nombre del servicio. La
     * normalización (tildes/ñ) se hace en Java, la limpieza de espacios (CHAR)
     * en SQL.
     */
    public List<ReporteData> obtenerReporteFiltrado(String nombreServicio) {

        // 1. Normalizamos el parámetro que viene del JSP (eliminar tildes/ñ/mayúsculas en Java)
        String nombreNormalizado = normalizarCadena(nombreServicio);

        // 2. El SQL normaliza la columna de la DB (eliminar relleno, minúsculas, espacios internos)
        String sql = CONSULTA_BASE_REPORTE
                // El SQL normaliza la columna: quita relleno (RTRIM), quita espacios internos (REPLACE) y minúsculas (LOWER)
                + " WHERE LOWER(REPLACE(RTRIM(TS.NOMBRESERVICIO), ' ', '')) = LOWER(REPLACE(?, ' ', '')) "
                + " ORDER BY S.FECHACREACION DESC";

        // 3. Ejecutamos la consulta con el parámetro normalizado
        return ejecutarConsultaReporte(sql, nombreNormalizado);
    }
}
