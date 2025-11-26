/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package servlet;

import base_datos.ConexionBD;

import capa_controladora.CrearCuentaController;

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

/**
 * Servlet que maneja las peticiones de creación de nuevas cuentas de usuario
 * Mapeado a la URL "/CrearCuentaServlet"
 */
@WebServlet("/CrearCuentaServlet")
public class CrearCuentaServlet extends HttpServlet {

    /**
     * Método doPost: Procesa el formulario de registro de nuevos usuarios
     * Recibe los datos del formulario, crea los objetos necesarios y delega la
     * lógica de registro al controlador
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Obtener parámetros enviados desde el formulario de registro
        String nombre = request.getParameter("nombres");
        String apellido = request.getParameter("apellidos");
        String correo = request.getParameter("correo");
        String identificador = request.getParameter("identificador"); // Usuario o identificador único
        String clave = request.getParameter("clave"); // Contraseña sin hashear (texto plano)

        // Variables para almacenar el mensaje de resultado y la página destino
        String mensaje = "";
        String destino = "crearCuenta.jsp"; // Por defecto regresa al formulario

        try {
            // 2. Crear objetos modelo con los datos recibidos
            // Password: objeto que encapsula la contraseña y el identificador
            Password password = new Password(clave, identificador);

            // TipoUsuario: por defecto todos los nuevos usuarios son "Cliente"
            TipoUsuario tipoUsuario = new TipoUsuario("Cliente");

            // Usuario: objeto completo con todos los datos del nuevo usuario
            Usuario nuevoUsuario = new Usuario(nombre, apellido, correo, tipoUsuario, password);

            // 3. Llamar al controlador para ejecutar la lógica de negocio
            // El controlador maneja la conexión a BD internamente
            CrearCuentaController ccc = new CrearCuentaController();
            boolean creado = ccc.registrarUsuario(nuevoUsuario, clave, identificador);

            // 4. Evaluar el resultado del registro
            if (creado) {
                // Registro exitoso: mensaje de éxito y redirigir al login
                mensaje = "¡Cuenta creada exitosamente! Por favor, inicie sesión.";
                destino = "login.jsp";
            } else {
                // Registro fallido: probablemente por duplicación de datos
                mensaje = "Error: El registro falló. Es posible que el identificador o correo ya existan.";
            }

        } catch (Exception e) {
            // Capturar cualquier excepción inesperada durante el proceso
            System.err.println("Error en la creación de cuenta: " + e.getMessage());
            mensaje = "Error interno del servidor al registrar la cuenta.";

        } finally {
            // Nota: No cerramos la conexión aquí porque el Controlador se encarga de eso
            // Este bloque garantiza que siempre se asignen valores a 'mensaje' y 'destino'
        }

        // 5. Enviar el mensaje de resultado a la página JSP y redirigir
        request.setAttribute("mensaje", mensaje);
        request.getRequestDispatcher(destino).forward(request, response);
    }
}
