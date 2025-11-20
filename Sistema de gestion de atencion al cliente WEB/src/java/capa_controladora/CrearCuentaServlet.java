/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package capa_controladora;

import base_datos.ConexionBD; 
import capa_modelo.Usuario;
import capa_modelo.Password;
import capa_modelo.TipoUsuario;
import java.io.IOException;
import java.sql.Connection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/CrearCuentaServlet")
public class CrearCuentaServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Obtener parámetros
        String nombre = request.getParameter("nombres");
        String apellido = request.getParameter("apellidos");
        String correo = request.getParameter("correo");
        String identificador = request.getParameter("identificador");
        String clave = request.getParameter("clave"); // Contraseña sin hashear

        String mensaje = "";
        String destino = "crearCuenta.jsp";

        try {
            // 2. Crear objetos modelo (TipoUsuario por defecto "Cliente")
            Password password = new Password(clave, identificador);
            TipoUsuario tipoUsuario = new TipoUsuario("Cliente");
            Usuario nuevoUsuario = new Usuario(nombre, apellido, correo, tipoUsuario, password);

            // 3. Llamar al controlador (el controlador maneja la conexión internamente)
            CrearCuentaController ccc = new CrearCuentaController();
            boolean creado = ccc.registrarUsuario(nuevoUsuario, clave, identificador);

            if (creado) {
                mensaje = "¡Cuenta creada exitosamente! Por favor, inicie sesión.";
                destino = "login.jsp";
            } else {
                mensaje = "Error: El registro falló. Es posible que el identificador o correo ya existan.";
            }

        } catch (Exception e) {
            System.err.println("Error en la creación de cuenta: " + e.getMessage());
            mensaje = "Error interno del servidor al registrar la cuenta.";
        } finally {
            // No cerramos la conexión aquí, el Controlador se encarga.
            // Solo aseguramos que la variable 'mensaje' y 'destino' se usen.
        }

        // 4. Redirigir/Reenviar con el mensaje
        request.setAttribute("mensaje", mensaje);
        request.getRequestDispatcher(destino).forward(request, response);
    }
}
