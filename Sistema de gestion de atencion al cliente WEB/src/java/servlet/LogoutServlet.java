/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package servlet;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Servlet (Controlador) encargado de manejar el cierre de sesión del usuario.
 * Su única responsabilidad es invalidar la sesión HTTP actual y redirigir al
 * usuario a la página de login.
 *
 * * @author RYZEN (Comentario original del IDE/Usuario)
 */
@WebServlet("/Logout")
public class LogoutServlet extends HttpServlet {

    /**
     * Maneja las peticiones HTTP GET, ya que el logout es típicamente invocado
     * con un simple enlace (<a>).
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Obtener la sesión actual. 
        // getSession(false) es crucial: si no hay una sesión activa, devuelve null
        // en lugar de crear una nueva, evitando recursos innecesarios.
        HttpSession session = request.getSession(false);

        // 2. Verificar si la sesión existe y es válida
        if (session != null) {
            // 3. INVALIDAR LA SESIÓN
            // Este es el paso clave: elimina todos los atributos de la sesión 
            // (incluyendo "usuarioActual") y la destruye en el servidor.
            session.invalidate();
        }

        // 4. Redirigir al usuario a la página de inicio de sesión.
        // Después de invalidar la sesión, se envía al usuario a la pantalla de login.
        response.sendRedirect("login.jsp");
    }
}
