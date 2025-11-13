/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package capa_controladora;

import base_datos.ConexionBD;
import capa_modelo.Usuario;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CrearCuentaController {

    // NOTA: ASUME QUE EL ID DEL TIPO USUARIO "CLIENTE" ES 2. ¡AJUSTA ESTE VALOR SI ES DIFERENTE!
    private static final int ID_TIPO_CLIENTE = 2;

    public boolean registrarUsuario(Usuario usuario, String contraseña, String identificador) {
        Connection con = null;
        PreparedStatement pstPassword = null;
        PreparedStatement pstUsuario = null;

        try {
            // 1. Establecer conexión e iniciar transacción
            con = ConexionBD.conectar();
            con.setAutoCommit(false);

            // 2. Insertar en 'pasword' y obtener el ID
            // Nota: Se recomienda encarecidamente hashear la 'contraseña' aquí.
            String sqlPassword = "INSERT INTO pasword (claveacceso, identificador) VALUES(?, ?) RETURNING idpasword";
            pstPassword = con.prepareStatement(sqlPassword);
            pstPassword.setString(1, contraseña);
            pstPassword.setString(2, identificador);

            // Usamos executeQuery para la cláusula RETURNING
            ResultSet rs = pstPassword.executeQuery();

            int idPasword = 0;
            if (rs.next()) {
                idPasword = rs.getInt("idpasword");
            } else {
                // Si la inserción de la contraseña falla, hacemos rollback y salimos.
                con.rollback();
                return false;
            }
            rs.close(); // Cierra el ResultSet

            // 3. Insertar en 'usuario' usando el ID de password
            String sqlUsuario = "INSERT INTO usuario (idpasword, idtipousuario, nombres, apellidos, correoelectronico) VALUES(?, ?, ?, ?, ?)";
            pstUsuario = con.prepareStatement(sqlUsuario);
            pstUsuario.setInt(1, idPasword);
            pstUsuario.setInt(2, ID_TIPO_CLIENTE);
            pstUsuario.setString(3, usuario.getNombres());
            pstUsuario.setString(4, usuario.getApellidos());
            pstUsuario.setString(5, usuario.getCorreoElectronico());

            int filas = pstUsuario.executeUpdate();

            // 4. Finalizar transacción
            con.commit();
            return filas > 0;

        } catch (SQLException e) {
            // Manejo de error de base de datos (ej. identificador duplicado)
            try {
                if (con != null) {
                    con.rollback();
                }
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            // NOTA: Se imprime el error en el log del servidor para depuración
            System.err.println("Error al registrar usuario: " + e.getMessage());
            return false;

        } finally {
            // 5. Cerrar recursos y conexión
            try {
                if (pstPassword != null) {
                    pstPassword.close();
                }
                if (pstUsuario != null) {
                    pstUsuario.close();
                }
                if (con != null) {
                    con.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
