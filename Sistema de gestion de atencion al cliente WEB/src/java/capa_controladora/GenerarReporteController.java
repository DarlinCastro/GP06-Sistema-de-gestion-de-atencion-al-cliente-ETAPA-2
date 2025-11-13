/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package capa_controladora;

import base_datos.ConexionBD;
import capa_modelo.TipoServicio;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.text.SimpleDateFormat; 

public class GenerarReporteController {

    public static class ReporteData {
        private String numeroTicket;
        private Date fechaCreacion;
        private String estadoTicket;
        private String tipoServicio;
        private String descripcionServicio;
        private String nombreCliente;
        private Date fechaAsignacion;
        private String cargoEncargado;
        private String nombreEncargadoSoporte;

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

        public String getNumeroTicket() { return numeroTicket; }
        public Date getFechaCreacion() { return fechaCreacion; }
        public String getEstadoTicket() { return estadoTicket; }
        public String getTipoServicio() { return tipoServicio; }
        public String getDescripcionServicio() { return descripcionServicio; }
        public String getNombreCliente() { return nombreCliente; }
        public Date getFechaAsignacion() { return fechaAsignacion; }
        public String getCargoEncargado() { return cargoEncargado; }
        public String getNombreEncargadoSoporte() { return nombreEncargadoSoporte; }
    }

    private static final String CONSULTA_BASE_REPORTE
            = "SELECT "
            + "   TK.NUMEROTICKET, S.FECHACREACION, ES.ESTADOSOLICITUD, "
            + "   TS.NOMBRESERVICIO, S.DESCRIPCION, C.NOMBRES || ' ' || C.APELLIDOS AS NOMBRE_CLIENTE, "
            + "   TK.FECHAASIGNACION, TU.CARGO AS CARGO_ENCARGADO, E.NOMBRES || ' ' || E.APELLIDOS AS NOMBRE_ENCARGADO "
            + "FROM SOLICITUD S "
            + "JOIN TICKET TK ON S.IDTICKET = TK.IDTICKET "
            + "JOIN ESTADO_SOLICITUD ES ON S.IDESTADOSOLICITUD = ES.IDESTADOSOLICITUD "
            + "JOIN TIPO_SERVICIO TS ON S.IDTIPOSERVICIO = TS.IDTIPOSERVICIO "
            + "JOIN USUARIO C ON S.IDUSUARIO = C.IDUSUARIO "
            + "LEFT JOIN USUARIO E ON TK.IDUSUARIO = E.IDUSUARIO "
            + "LEFT JOIN TIPO_USUARIO TU ON E.IDTIPOUSUARIO = TU.IDTIPOUSUARIO ";

    public List<TipoServicio> cargarTiposServicio() {
        List<TipoServicio> listaServicios = new ArrayList<>();
        String sql = "SELECT NOMBRESERVICIO FROM TIPO_SERVICIO ORDER BY NOMBRESERVICIO";
        try (Connection conn = ConexionBD.conectar(); 
             PreparedStatement ps = conn.prepareStatement(sql); 
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String nombre = rs.getString("NOMBRESERVICIO");
                listaServicios.add(new TipoServicio(nombre));
            }
        } catch (SQLException e) {
            System.err.println("Error SQL al obtener Tipos de Servicio: " + e.getMessage());
        }
        return listaServicios;
    }

    private List<ReporteData> ejecutarConsultaReporte(String sql, String filtroServicio) {
        List<ReporteData> listaReportes = new ArrayList<>();
        try (Connection conn = ConexionBD.conectar(); 
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (filtroServicio != null) {
                ps.setString(1, filtroServicio.trim());
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
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
            System.err.println("Error SQL al ejecutar consulta de Reportes: " + e.getMessage());
        }
        return listaReportes;
    }

    public List<ReporteData> obtenerReporteGeneral() {
        String sql = CONSULTA_BASE_REPORTE + " ORDER BY S.FECHACREACION DESC";
        return ejecutarConsultaReporte(sql, null);
    }

    public List<ReporteData> obtenerReporteFiltrado(String nombreServicio) {
        String sql = CONSULTA_BASE_REPORTE
                + " WHERE TS.NOMBRESERVICIO = ? ORDER BY S.FECHACREACION DESC";
        return ejecutarConsultaReporte(sql, nombreServicio);
    }

}