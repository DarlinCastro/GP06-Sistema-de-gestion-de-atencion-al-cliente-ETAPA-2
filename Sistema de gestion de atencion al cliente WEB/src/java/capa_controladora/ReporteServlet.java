/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package capa_controladora;

import base_datos.ConexionBD;
import capa_modelo.TipoServicio;
import java.io.IOException;
import java.sql.Connection;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/Reporte")
public class ReporteServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (request.getSession().getAttribute("usuarioActual") == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        String filtroServicio = request.getParameter("filtroServicio");
        
        Connection conn = null;
        try {
            conn = ConexionBD.conectar();
            if (conn != null) {
                GenerarReporteController grc = new GenerarReporteController(); 
                List<TipoServicio> listaServicios = grc.cargarTiposServicio();
                request.setAttribute("listaServicios", listaServicios); 
                
                List<GenerarReporteController.ReporteData> listaReporte;
                
                if (filtroServicio != null && !filtroServicio.isEmpty() && !"Todos los Servicios".equals(filtroServicio)) {
                    listaReporte = grc.obtenerReporteFiltrado(filtroServicio);
                    request.setAttribute("reporteTitulo", "REPORTE FILTRADO por: " + filtroServicio);
                } else {
                    listaReporte = grc.obtenerReporteGeneral();
                    request.setAttribute("reporteTitulo", "REPORTE GENERAL");
                }
                
                request.setAttribute("listaReporte", listaReporte); 
                request.getRequestDispatcher("generarReporte.jsp").forward(request, response);
                return;
            }
        } catch (Exception e) {
            System.err.println("Error al generar reporte: " + e.getMessage());
            request.setAttribute("error", "Error al generar el reporte: " + e.getMessage());
            request.getRequestDispatcher("MenuAdmin.jsp").forward(request, response); 
            return;
        } finally {
            try { if (conn != null) conn.close(); } catch (Exception e) {}
        }
    }
}