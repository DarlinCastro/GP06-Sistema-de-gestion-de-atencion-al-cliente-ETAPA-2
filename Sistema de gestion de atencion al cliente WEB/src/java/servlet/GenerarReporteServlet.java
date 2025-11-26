/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package servlet;

import capa_controladora.GenerarReporteController;
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

/**
 * Servlet (Controlador) que maneja las solicitudes relacionadas con la
 * generación de reportes. Actúa como el punto de entrada para la vista de
 * reportes, manejando la lógica de filtrado y delegando la interacción con la
 * base de datos al GenerarReporteController.
 */
@WebServlet(name = "GenerarReporteServlet", urlPatterns = {"/GenerarReporteController", "/GenerarReporteServlet"})
public class GenerarReporteServlet extends HttpServlet {

    // Instancia del Controller que maneja la conexión a BD y la lógica (la capa intermedia).
    // Se inicializa una sola vez para ser reutilizada por todas las peticiones (hilos).
    private final GenerarReporteController reporteManager = new GenerarReporteController();

    /**
     * Maneja las peticiones HTTP GET, que son usadas para cargar la página
     * inicial del reporte y para aplicar filtros de servicio.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. OBTENER EL ORIGEN (La ruta de regreso)
        // Se utiliza para saber a qué página debe volver el usuario si presiona 'Atrás'.
        String origen = request.getParameter("origen");

        // 2. Manejo de la acción 'atras'
        // Detecta si la solicitud es para volver a la página anterior en lugar de cargar un reporte.
        String accion = request.getParameter("accion");
        if ("atras".equals(accion)) {

            // Redirige al origen guardado. Si no existe (lo que no debería pasar), usa Admin.jsp
            String destino = (origen != null && !origen.isEmpty()) ? origen : "Admin.jsp";

            // Envía la redirección al navegador del cliente
            response.sendRedirect(destino);
            return; // Detiene la ejecución del método doGet.
        }

        // 3. Obtener filtro y manejar valor por defecto
        // Recupera el parámetro del filtro de servicio enviado desde el formulario/dropdown de la vista.
        String filtroServicio = request.getParameter("filtroServicio");
        // Si el filtro es nulo, vacío o se selecciona la opción "todos", se establece "todos" como valor.
        if (filtroServicio == null || filtroServicio.isEmpty() || "todos".equals(filtroServicio)) {
            filtroServicio = "todos";
        }

        // 4. Delegar la carga de datos al GenerarReporteController
        // Primero, cargar la lista de todos los tipos de servicio para popular el dropdown en la vista (JSP).
        List<TipoServicio> tiposServicio = reporteManager.cargarTiposServicio();
        List<ReporteData> listaReportes;

        // Determina qué método del controlador llamar basándose en el filtro.
        if ("todos".equals(filtroServicio)) {
            // Carga el reporte sin ningún filtro.
            listaReportes = reporteManager.obtenerReporteGeneral();
        } else {
            // Carga el reporte filtrando por el nombre del servicio seleccionado.
            listaReportes = reporteManager.obtenerReporteFiltrado(filtroServicio);
        }

        // 5. Pasar datos a la vista
        // Almacena los datos en el objeto Request para que sean accesibles desde el JSP.
        request.setAttribute("listaReportes", listaReportes);
        request.setAttribute("tiposServicio", tiposServicio);
        request.setAttribute("filtroSeleccionado", filtroServicio); // Necesario para mantener el dropdown seleccionado.

        // 6. Pasar el origen de vuelta al JSP para que los filtros y el botón "Atrás" lo mantengan
        request.setAttribute("origen", origen);

        // 7. Enviar a la vista (generarReporte.jsp)
        // Crea un despachador para reenviar la solicitud al JSP, manteniendo los atributos en el request.
        RequestDispatcher rd = request.getRequestDispatcher("/generarReporte.jsp");
        rd.forward(request, response);
    }
}
