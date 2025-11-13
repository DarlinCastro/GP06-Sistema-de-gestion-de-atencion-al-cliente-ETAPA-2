/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package capa_controladora;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 *
 * @author RYZEN
 */
@WebServlet("/Logout")
public class LogoutServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Obtener la sesión actual. Usamos getSession(false) para no crear una sesión si no existe.
        HttpSession session = request.getSession(false);

        // 2. Verificar si la sesión existe y es válida
        if (session != null) {
            // 3. INVALIDAR LA SESIÓN (Esto elimina todos los atributos, incluyendo "usuarioActual")
            session.invalidate();
        }

        // 4. Redirigir al usuario a la página de inicio de sesión
        response.sendRedirect("login.jsp");
    }
}
