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

/**
 * Controlador que maneja la lógica de negocio para la creación de nuevas
 * cuentas de usuario Se encarga de insertar los datos del usuario en las tablas
 * 'pasword' y 'usuario' usando transacciones
 */
public class CrearCuentaController {

    // Constante que define el ID del tipo de usuario "Cliente" en la base de datos
    // NOTA: ASUME QUE EL ID DEL TIPO USUARIO "CLIENTE" ES 2. ¡AJUSTA ESTE VALOR SI ES DIFERENTE!
    private static final int ID_TIPO_CLIENTE = 2;

    /**
     * Registra un nuevo usuario en el sistema insertando datos en dos tablas
     * relacionadas Usa una transacción para garantizar que ambas inserciones se
     * completen o se reviertan juntas
     *
     * @param usuario Objeto Usuario con los datos del nuevo usuario
     * @param contraseña Contraseña en texto plano (debería hashearse por
     * seguridad)
     * @param identificador Identificador único del usuario
     * @return true si el registro fue exitoso, false en caso contrario
     */
    public boolean registrarUsuario(Usuario usuario, String contraseña, String identificador) {
        // Variables para manejar la conexión y las consultas preparadas
        Connection con = null;
        PreparedStatement pstPassword = null;
        PreparedStatement pstUsuario = null;

        try {
            // 1. Establecer conexión a la base de datos e iniciar transacción
            con = ConexionBD.conectar();
            con.setAutoCommit(false); // Desactivar auto-commit para control manual de transacción

            // 2. Primera inserción: Tabla 'pasword' (nota: tabla con typo en nombre)
            // Se inserta la contraseña e identificador, y se obtiene el ID generado
            // Nota: Se recomienda encarecidamente hashear la 'contraseña' aquí por seguridad
            String sqlPassword = "INSERT INTO pasword (claveacceso, identificador) VALUES(?, ?) RETURNING idpasword";
            pstPassword = con.prepareStatement(sqlPassword);
            pstPassword.setString(1, contraseña); // Contraseña en texto plano (inseguro)
            pstPassword.setString(2, identificador); // Identificador único

            // Ejecutar y obtener el ID generado usando RETURNING (característica de PostgreSQL)
            // Usamos executeQuery() porque RETURNING devuelve un ResultSet
            ResultSet rs = pstPassword.executeQuery();
            int idPasword = 0;

            if (rs.next()) {
                // Obtener el ID de password generado automáticamente
                idPasword = rs.getInt("idpasword");
            } else {
                // Si la inserción de la contraseña falla (no retorna ID), hacer rollback y salir
                con.rollback();
                return false;
            }
            rs.close(); // Cerrar el ResultSet para liberar recursos

            // 3. Segunda inserción: Tabla 'usuario' usando el ID de password obtenido
            // Se relaciona el usuario con su password mediante la clave foránea
            String sqlUsuario = "INSERT INTO usuario (idpasword, idtipousuario, nombres, apellidos, correoelectronico) VALUES(?, ?, ?, ?, ?)";
            pstUsuario = con.prepareStatement(sqlUsuario);
            pstUsuario.setInt(1, idPasword); // FK: ID de la password recién insertada
            pstUsuario.setInt(2, ID_TIPO_CLIENTE); // FK: ID del tipo de usuario (Cliente = 2)
            pstUsuario.setString(3, usuario.getNombres()); // Nombres del usuario
            pstUsuario.setString(4, usuario.getApellidos()); // Apellidos del usuario
            pstUsuario.setString(5, usuario.getCorreoElectronico()); // Correo electrónico

            // Ejecutar la inserción y obtener número de filas afectadas
            int filas = pstUsuario.executeUpdate();

            // 4. Finalizar transacción: Si todo salió bien, confirmar cambios
            con.commit(); // Commit: hacer permanentes ambas inserciones

            // Retornar true si se insertó al menos 1 fila en la tabla usuario
            return filas > 0;

        } catch (SQLException e) {
            // Manejo de errores de base de datos (ej. violación de unique constraint)
            // Si ocurre algún error, revertir todas las operaciones de la transacción
            try {
                if (con != null) {
                    con.rollback(); // Rollback: deshacer todas las inserciones
                }
            } catch (SQLException rollbackEx) {
                // Si el rollback también falla, imprimir el error
                rollbackEx.printStackTrace();
            }

            // Imprimir el error en el log del servidor para depuración
            // NOTA: Se imprime el error en el log del servidor para depuración
            System.err.println("Error al registrar usuario: " + e.getMessage());
            return false; // Retornar false indicando fallo en el registro

        } finally {
            // 5. Cerrar recursos y conexión (siempre se ejecuta, haya o no excepción)
            // Es crítico cerrar los recursos para evitar memory leaks y agotamiento de conexiones
            try {
                if (pstPassword != null) {
                    pstPassword.close(); // Cerrar PreparedStatement de password
                }
                if (pstUsuario != null) {
                    pstUsuario.close(); // Cerrar PreparedStatement de usuario
                }
                if (con != null) {
                    con.close(); // Cerrar conexión a la base de datos
                }
            } catch (SQLException e) {
                // Si hay error al cerrar recursos, imprimir pero no lanzar excepción
                e.printStackTrace();
            }
        }
    }
}
