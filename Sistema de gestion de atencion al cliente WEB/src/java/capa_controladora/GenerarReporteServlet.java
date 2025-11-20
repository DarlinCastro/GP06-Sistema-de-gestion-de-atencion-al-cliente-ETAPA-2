/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package capa_controladora;

// Importamos las clases DTO anidadas del otro archivo Controller
import capa_controladora.GenerarReporteController.ReporteData;
import capa_controladora.GenerarReporteController.TipoServicio;

import java.io.IOException;
import java.util.List;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "GenerarReporteServlet", urlPatterns = {"/GenerarReporteController", "/GenerarReporteServlet"})
public class GenerarReporteServlet extends HttpServlet {

    // Instancia del Controller que maneja la conexión a BD y la lógica
    private final GenerarReporteController reporteManager = new GenerarReporteController();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. OBTENER EL ORIGEN (La ruta de regreso)
        String origen = request.getParameter("origen");

        // 2. Manejo de la acción 'atras'
        String accion = request.getParameter("accion");
        if ("atras".equals(accion)) {

            // Redirige al origen guardado. Si no existe (lo que no debería pasar), usa Admin.jsp
            String destino = (origen != null && !origen.isEmpty()) ? origen : "Admin.jsp";

            response.sendRedirect(destino);
            return;
        }

        // 3. Obtener filtro y manejar valor por defecto
        String filtroServicio = request.getParameter("filtroServicio");
        if (filtroServicio == null || filtroServicio.isEmpty() || "todos".equals(filtroServicio)) {
            filtroServicio = "todos";
        }

        // 4. Delegar la carga de datos al GenerarReporteController
        List<TipoServicio> tiposServicio = reporteManager.cargarTiposServicio();
        List<ReporteData> listaReportes;

        if ("todos".equals(filtroServicio)) {
            listaReportes = reporteManager.obtenerReporteGeneral();
        } else {
            listaReportes = reporteManager.obtenerReporteFiltrado(filtroServicio);
        }

        // 5. Pasar datos a la vista
        request.setAttribute("listaReportes", listaReportes);
        request.setAttribute("tiposServicio", tiposServicio);
        request.setAttribute("filtroSeleccionado", filtroServicio);

        // 6. Pasar el origen de vuelta al JSP para que los filtros y el botón "Atrás" lo mantengan
        request.setAttribute("origen", origen);

        // 7. Enviar a la vista (GenerarReporte.jsp)
        RequestDispatcher rd = request.getRequestDispatcher("/GenerarReporte.jsp");
        rd.forward(request, response);
    }
}
