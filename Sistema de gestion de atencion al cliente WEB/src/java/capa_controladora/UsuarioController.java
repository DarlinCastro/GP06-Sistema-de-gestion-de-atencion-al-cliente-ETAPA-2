/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package capa_controladora;

import capa_modelo.Usuario;
import capa_modelo.TipoUsuario;
import capa_modelo.Password;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Controlador que maneja la lógica de negocio relacionada con la autenticación
 * y la recuperación de datos de un usuario a partir de sus credenciales.
 * Trabaja directamente con la capa de persistencia (JDBC).
 */
public class UsuarioController {

    // La conexión a la base de datos es inyectada en el constructor y mantenida
    // mientras se usa el controlador para una operación específica (ej. login).
    private Connection conexion;

    /**
     * Constructor que recibe y almacena la conexión activa.
     *
     * @param conexion La conexión activa a la base de datos.
     */
    public UsuarioController(Connection conexion) {
        this.conexion = conexion;
    }

    /**
     * Verifica la existencia de un usuario con el identificador y la clave
     * proporcionados y, si existe, devuelve su rol (TipoUsuario). Nota: Este
     * método parece ser una versión más simple o previa de
     * 'obtenerUsuarioPorCredenciales', ya que solo retorna el cargo.
     *
     * * @param identificador El nombre de usuario/identificador de login.
     * @param clave La clave de acceso.
     * @return Objeto TipoUsuario con el cargo, o null si las credenciales son
     * incorrectas.
     * @throws SQLException Si ocurre un error durante la ejecución de la
     * consulta SQL.
     */
    public TipoUsuario introducirCredenciales(String identificador, String clave) throws SQLException {
        TipoUsuario tipoUsuario = null;

        // Consulta SQL para autenticar al usuario y obtener su cargo.
        // Requiere JOINS entre USUARIO, PASWORD y TIPO_USUARIO.
        String sql = "SELECT tu.cargo\n"
                + "FROM usuario u\n"
                + "INNER JOIN pasword p ON u.idpasword = p.idpasword\n"
                + "INNER JOIN tipo_usuario tu ON u.idtipousuario = tu.idtipousuario\n"
                + "WHERE p.identificador = ? AND p.claveacceso = ?";

        try (PreparedStatement pst = conexion.prepareStatement(sql)) {
            // Asignar los parámetros a la consulta SQL.
            pst.setString(1, identificador);
            pst.setString(2, clave);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    // Si se encuentra un registro, se crea el objeto TipoUsuario.
                    tipoUsuario = new TipoUsuario();
                    // Se mapea el campo 'cargo', limpiando espacios en blanco (trim).
                    tipoUsuario.setCargo(rs.getString("cargo").trim());
                }
            }
        }
        return tipoUsuario;
    }

    /**
     * Autentica al usuario y, si las credenciales son correctas, recupera todos
     * los datos relevantes del usuario, incluyendo el TipoUsuario y Password.
     * Este es el método usado en el LoginServlet para crear el objeto de
     * sesión.
     *
     * * @param identificador El nombre de usuario/identificador de login.
     * @param clave La clave de acceso.
     * @return Objeto Usuario completamente poblado, o null si las credenciales
     * son incorrectas.
     * @throws SQLException Si ocurre un error durante la ejecución de la
     * consulta SQL.
     */
    public Usuario obtenerUsuarioPorCredenciales(String identificador, String clave) throws SQLException {
        Usuario usuario = null;

        // Consulta SQL para obtener todos los campos necesarios.
        String sql = "SELECT u.nombres, u.apellidos, u.correoelectronico, tu.cargo, p.identificador, p.claveacceso "
                + "FROM usuario u "
                + "INNER JOIN pasword p ON u.idpasword = p.idpasword "
                + "INNER JOIN tipo_usuario tu ON u.idtipousuario = tu.idtipousuario "
                + "WHERE p.identificador = ? AND p.claveacceso = ?";

        try (PreparedStatement pst = conexion.prepareStatement(sql)) {
            pst.setString(1, identificador);
            pst.setString(2, clave);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    // Si el usuario existe, se procede a mapear los datos al DTO Usuario.
                    usuario = new Usuario();

                    // Mapeo de campos directos de la tabla USUARIO.
                    usuario.setNombres(rs.getString("nombres").trim());
                    usuario.setApellidos(rs.getString("apellidos").trim());
                    usuario.setCorreoElectronico(rs.getString("correoelectronico").trim());

                    // Mapeo del DTO anidado TipoUsuario.
                    TipoUsuario tipo = new TipoUsuario();
                    tipo.setCargo(rs.getString("cargo").trim());
                    usuario.setTipoUsuario(tipo);

                    // Mapeo del DTO anidado Password (para guardar el identificador y clave en el objeto).
                    Password password = new Password();
                    password.setIdentificador(rs.getString("identificador").trim());
                    password.setClaveAcceso(rs.getString("claveacceso").trim());
                    usuario.setPassword(password);
                }
            }
        }

        return usuario;
    }
}
