/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package capa_controladora;

import capa_modelo.Usuario;
import capa_modelo.TipoUsuario;
import capa_modelo.Password;
import capa_modelo.TipoServicio;

import base_datos.ConexionBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Clase controladora responsable de la lógica de negocio y la persistencia
 * (CRUD) de la entidad Usuario en la base de datos. Maneja transacciones
 * complejas que involucran las tablas 'usuario', 'pasword' y 'tipo_usuario'.
 */
public class GestionarUsuariosController {

    // El constructor por defecto es suficiente para esta clase
    public GestionarUsuariosController() {
    }

    /**
     * Obtiene una lista de cargos (roles) únicos de la tabla tipo_usuario.
     * Utiliza un enfoque de prueba y error para manejar la sensibilidad a
     * mayúsculas/minúsculas de PostgreSQL en los nombres de tablas.
     *
     * * @return Una lista de Strings con los nombres de los cargos.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public List<String> obtenerTiposUsuarioParaCargos() throws SQLException {
        List<String> cargos = new ArrayList<>();

        // Variantes SQL para superar problemas de mayúsculas/minúsculas 
        // en la referencia a la tabla 'tipo_usuario' de PostgreSQL.
        String[] sqlVariants = {
            "SELECT DISTINCT cargo FROM tipo_usuario ORDER BY cargo", // minúsculas (default)
            "SELECT DISTINCT cargo FROM TIPO_USUARIO ORDER BY cargo", // mayúsculas sin comillas
            "SELECT DISTINCT cargo FROM \"TIPO_USUARIO\" ORDER BY cargo" // mayúsculas con comillas (más seguro)
        };

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        SQLException lastException = null;

        for (String sql : sqlVariants) {
            try {
                conn = ConexionBD.conectar();
                System.out.println("Intentando query: " + sql);

                pstmt = conn.prepareStatement(sql);
                rs = pstmt.executeQuery();

                while (rs.next()) {
                    String cargo = rs.getString("cargo");
                    if (cargo != null && !cargo.trim().isEmpty()) {
                        cargos.add(cargo.trim());
                        System.out.println("Cargo agregado: [" + cargo.trim() + "]");
                    }
                }

                System.out.println("Total de cargos obtenidos: " + cargos.size());

                // Si se obtienen resultados, asumimos que esta consulta funcionó y salimos del loop.
                if (!cargos.isEmpty()) {
                    break;
                }

            } catch (SQLException e) {
                System.err.println("Falló con query: " + sql);
                System.err.println("    Mensaje: " + e.getMessage());
                lastException = e; // Guarda la última excepción para lanzarla si ninguna query funciona
            } finally {
                // Bloque para garantizar el CIERRE manual de recursos
                try {
                    if (rs != null) {
                        rs.close();
                    }
                    if (pstmt != null) {
                        pstmt.close();
                    }
                    if (conn != null) {
                        conn.close();
                    }
                } catch (SQLException e) {
                    System.err.println("Error al cerrar recursos: " + e.getMessage());
                }
            }
        }

        // Lanza la excepción si todas las variantes fallaron y no se encontraron cargos.
        if (cargos.isEmpty() && lastException != null) {
            System.err.println("No se pudo obtener cargos con ninguna variante de query");
            throw lastException;
        }

        if (cargos.isEmpty()) {
            System.err.println("WARNING: La tabla tipo_usuario está VACÍA");
        }

        return cargos;
    }

    /**
     * Verifica la existencia de un identificador (username) en la tabla
     * pasword. Utiliza try-with-resources para cerrar la conexión y el
     * statement automáticamente.
     *
     * * @param identificador El identificador a buscar.
     * @return true si el identificador ya existe, false en caso contrario.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public boolean existeIdentificador(String identificador) throws SQLException {
        String sql = "SELECT COUNT(*) FROM pasword WHERE identificador = ?";
        // try-with-resources: conn y pstmt se cerrarán automáticamente.
        try (Connection conn = ConexionBD.conectar(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, identificador);
            ResultSet rs = pstmt.executeQuery();

            // Si hay un resultado y el conteo es mayor que 0, existe.
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    /**
     * Inserta un nuevo usuario, incluyendo la contraseña, el tipo de usuario y
     * los datos personales. **UTILIZA TRANSACCIONES** para asegurar que tanto
     * la contraseña como el usuario se inserten correctamente o que ambas
     * operaciones se reviertan (COMMIT/ROLLBACK).
     *
     * * @param u Objeto Usuario con todos los datos a insertar.
     * @throws Exception Si ocurre un error de conexión, SQL o lógica (ej. Cargo
     * no encontrado).
     */
    public void agregarUsuario(Usuario u) throws Exception {
        Connection con = null;
        PreparedStatement psPass = null;
        PreparedStatement psUsuario = null;

        try {
            con = ConexionBD.conectar();
            if (con == null) {
                throw new SQLException("Fallo de conexión.");
            }

            // 1. INICIAR TRANSACCIÓN: Deshabilita el auto-commit
            con.setAutoCommit(false);

            // 2. Insertar en PASWORD y obtener el ID generado (PostgreSQL 'RETURNING')
            String sqlPass = "INSERT INTO pasword (claveacceso, identificador) VALUES (?, ?) RETURNING idpasword";
            psPass = con.prepareStatement(sqlPass);
            psPass.setString(1, u.getPassword().getClaveAcceso());
            psPass.setString(2, u.getPassword().getIdentificador());

            ResultSet rsPass = psPass.executeQuery(); // executeQuery porque usamos RETURNING
            int idPasword = -1;
            if (rsPass.next()) {
                idPasword = rsPass.getInt("idpasword");
            } else {
                throw new SQLException("Fallo al obtener ID de pasword.");
            }
            // Importante: rsPass y psPass deben cerrarse, lo cual se hace en el bloque finally actual.

            // 3. Obtener el ID del TIPO_USUARIO por el cargo
            String sqlTipo = "SELECT idtipousuario FROM tipo_usuario WHERE cargo = ?";
            try (PreparedStatement psTipo = con.prepareStatement(sqlTipo);) {
                psTipo.setString(1, u.getTipoUsuario().getCargo());
                try (ResultSet rsTipo = psTipo.executeQuery()) {
                    int idTipoUsuario = -1;
                    if (rsTipo.next()) {
                        idTipoUsuario = rsTipo.getInt("idtipousuario");
                    } else {
                        throw new SQLException("Cargo no encontrado en tipo_usuario");
                    }

                    // 4. Insertar en USUARIO
                    String sqlUser = """
                        INSERT INTO usuario (nombres, apellidos, correoelectronico, idtipousuario, idpasword)
                        VALUES (?, ?, ?, ?, ?)
                        """;

                    psUsuario = con.prepareStatement(sqlUser);
                    psUsuario.setString(1, u.getNombres());
                    psUsuario.setString(2, u.getApellidos());
                    psUsuario.setString(3, u.getCorreoElectronico());
                    psUsuario.setInt(4, idTipoUsuario);
                    psUsuario.setInt(5, idPasword);

                    psUsuario.executeUpdate();
                }
            }

            // 5. FINALIZAR TRANSACCIÓN: Confirma todos los cambios
            con.commit();

        } catch (SQLException e) {
            System.out.println("Error al agregar usuario: " + e.getMessage());
            // 6. ROLLBACK: Revierte los cambios si hubo un error SQL
            try {
                if (con != null) {
                    con.rollback();
                }
            } catch (SQLException ex) {
                System.out.println("Error al hacer rollback: " + ex.getMessage());
            }
            throw new Exception("Error en la transacción al agregar usuario.", e);
        } finally {
            // 7. CERRAR RECURSOS y restaurar el auto-commit
            try {
                if (psPass != null) { // Cierre manual de psPass (no envuelto en try-with-resources)
                    psPass.close();
                }
                if (psUsuario != null) { // Cierre manual de psUsuario (no envuelto en try-with-resources)
                    psUsuario.close();
                }
                if (con != null) {
                    con.setAutoCommit(true); // Restaurar el modo auto-commit
                }
                if (con != null) {
                    con.close(); // Cerrar la conexión
                }
            } catch (SQLException e) {
                System.out.println("Error al cerrar conexión: " + e.getMessage());
            }
        }
    }

    /**
     * Obtiene una lista de todos los usuarios, uniéndolos con sus tipos de
     * usuario y detalles de contraseña. Utiliza try-with-resources (moderno)
     * para el manejo automático de recursos.
     *
     * * @return Lista de objetos Usuario.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public List<Usuario> obtenerUsuarios() throws SQLException {
        List<Usuario> lista = new ArrayList<>();
        String sql = """
            SELECT 
                u.nombres,
                u.apellidos,
                u.correoelectronico,
                tu.cargo,
                p.claveacceso,
                p.identificador
            FROM usuario u
            JOIN tipo_usuario tu ON u.idtipousuario = tu.idtipousuario
            JOIN pasword p ON u.idpasword = p.idpasword
            ORDER BY u.nombres ASC
            """;

        // Uso de try-with-resources para Connection, PreparedStatement y ResultSet: 
        // Cierre automático y seguro de todos los recursos.
        try (Connection con = ConexionBD.conectar(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                // Mapeo de resultados a objetos modelo
                TipoUsuario tipo = new TipoUsuario(rs.getString("cargo"));
                Password pass = new Password(rs.getString("claveacceso"), rs.getString("identificador"));
                Usuario u = new Usuario(
                        rs.getString("nombres"),
                        rs.getString("apellidos"),
                        rs.getString("correoelectronico"),
                        tipo,
                        pass
                );
                lista.add(u);
            }

        } catch (SQLException e) {
            System.out.println("❌ Error al obtener usuarios: " + e.getMessage());
            throw e;
        }

        return lista;
    }

    /**
     * Actualiza los datos de un usuario (personales, rol y contraseña).
     * **UTILIZA TRANSACCIONES** para asegurar la actualización coherente de las
     * tablas.
     *
     * * @param u Objeto Usuario con los datos actualizados.
     * @throws Exception Si ocurre un error de base de datos o lógica.
     */
    public void actualizarUsuario(Usuario u) throws Exception {
        Connection con = null;
        // NOTA: Se recomienda usar try-with-resources aquí también para manejar 
        // automáticamente el cierre de PreparedStatement y ResultSet anidados.
        try {
            con = ConexionBD.conectar();
            if (con == null) {
                throw new SQLException("Fallo de conexión.");
            }
            con.setAutoCommit(false); // Iniciar transacción

            // 1. Actualizar PASWORD usando el identificador
            String sqlPass = "UPDATE pasword SET claveacceso=? WHERE identificador=?";
            try (PreparedStatement psPass = con.prepareStatement(sqlPass)) {
                psPass.setString(1, u.getPassword().getClaveAcceso());
                psPass.setString(2, u.getPassword().getIdentificador());
                psPass.executeUpdate();
            }

            // 2. Obtener el ID de TIPO_USUARIO
            int idTipoUsuario = -1;
            String sqlTipo = "SELECT idtipousuario FROM tipo_usuario WHERE cargo = ?";
            try (PreparedStatement psTipo = con.prepareStatement(sqlTipo)) {
                psTipo.setString(1, u.getTipoUsuario().getCargo());
                try (ResultSet rsTipo = psTipo.executeQuery()) {
                    if (rsTipo.next()) {
                        idTipoUsuario = rsTipo.getInt("idtipousuario");
                    } else {
                        throw new SQLException("Cargo no encontrado");
                    }
                }
            }

            // 3. Actualizar USUARIO usando el idpasword relacionado con el identificador
            String sqlUsuario = """
                UPDATE usuario 
                    SET nombres=?, apellidos=?, correoelectronico=?, idtipousuario=?
                    WHERE idpasword IN (
                        SELECT idpasword FROM pasword WHERE identificador = ?
                    )
                """;
            try (PreparedStatement psUsuario = con.prepareStatement(sqlUsuario)) {
                psUsuario.setString(1, u.getNombres());
                psUsuario.setString(2, u.getApellidos());
                psUsuario.setString(3, u.getCorreoElectronico());
                psUsuario.setInt(4, idTipoUsuario);
                psUsuario.setString(5, u.getPassword().getIdentificador()); // Usar el identificador para la subconsulta
                psUsuario.executeUpdate();
            }

            con.commit(); // Confirmar transacción

        } catch (SQLException e) {
            System.out.println("Error al actualizar usuario: " + e.getMessage());
            try {
                if (con != null) {
                    con.rollback(); // Revertir si hay error
                }
            } catch (SQLException ex) {
                System.out.println("Error al hacer rollback: " + ex.getMessage());
            }
            throw new Exception("Error en la transacción al actualizar usuario.", e);
        } finally {
            // Cierre manual de la conexión y restauración de auto-commit
            try {
                if (con != null) {
                    con.setAutoCommit(true);
                }
                if (con != null) {
                    con.close();
                }
            } catch (SQLException e) {
                System.out.println("Error al cerrar conexión: " + e.getMessage());
            }
        }
    }

    /**
     * Elimina un usuario y su registro de contraseña asociado por el
     * identificador. **UTILIZA TRANSACCIONES**. El orden es crucial: primero el
     * registro que depende (usuario), luego el registro principal (pasword)
     * para evitar errores de Foreign Key.
     *
     * * @param identificador El identificador del usuario a eliminar.
     * @throws Exception Si ocurre un error de base de datos o lógica.
     */
    public void eliminarUsuario(String identificador) throws Exception {

        // 1. Eliminar el registro en USUARIO (la tabla hija)
        String sqlDeleteUsuario = """
            DELETE FROM usuario 
                WHERE idpasword IN (
                    SELECT idpasword FROM pasword WHERE identificador = ?
                )
            """;

        // 2. Eliminar el registro en PASWORD (la tabla padre)
        String sqlDeletePassword = "DELETE FROM pasword WHERE identificador = ?";

        Connection con = null;
        try {
            con = ConexionBD.conectar();
            if (con == null) {
                throw new SQLException("Fallo de conexión.");
            }
            con.setAutoCommit(false); // Iniciar transacción

            // Usamos try-with-resources para los PreparedStatement
            try (PreparedStatement psUsuario = con.prepareStatement(sqlDeleteUsuario)) {
                psUsuario.setString(1, identificador);
                psUsuario.executeUpdate();
            }

            try (PreparedStatement psPass = con.prepareStatement(sqlDeletePassword)) {
                psPass.setString(1, identificador);
                psPass.executeUpdate();
            }

            con.commit(); // Confirmar ambas eliminaciones

        } catch (SQLException e) {
            System.out.println("Error al eliminar usuario: " + e.getMessage());
            try {
                if (con != null) {
                    con.rollback(); // Revertir si hay error
                }
            } catch (SQLException ex) {
                System.out.println("Error al hacer rollback: " + ex.getMessage());
            }
            throw new Exception("Error en la transacción al eliminar usuario.", e);
        } finally {
            // Cierre manual de la conexión y restauración de auto-commit
            try {
                if (con != null) {
                    con.setAutoCommit(true);
                }
                if (con != null) {
                    con.close();
                }
            } catch (SQLException e) {
                System.out.println("Error al cerrar conexión: " + e.getMessage());
            }
        }
    }
}
