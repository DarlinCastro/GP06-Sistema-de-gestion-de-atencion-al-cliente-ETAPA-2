/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package capa_controladora;

import capa_modelo.Usuario;
import capa_modelo.TipoUsuario;
import capa_modelo.Password;

import base_datos.ConexionBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase controladora responsable de la lógica de negocio y la persistencia
 * (CRUD) de la entidad Usuario en la base de datos. Maneja transacciones
 * complejas que involucran las tablas 'usuario', 'pasword' y 'tipo_usuario'.
 */
public class GestionarUsuariosController {

    public GestionarUsuariosController() {
    }

    /**
     * Obtiene una lista de cargos (roles) únicos de la tabla tipo_usuario.
     * Utiliza un enfoque de prueba y error para manejar la sensibilidad a
     * mayúsculas/minúsculas de PostgreSQL en los nombres de tablas.
     *
     * @return Una lista de Strings con los nombres de los cargos.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public List<String> obtenerTiposUsuarioParaCargos() throws SQLException {
        List<String> cargos = new ArrayList<>();

        String[] sqlVariants = {
            "SELECT DISTINCT TRIM(cargo) as cargo FROM tipo_usuario WHERE cargo IS NOT NULL ORDER BY cargo",
            "SELECT DISTINCT TRIM(cargo) as cargo FROM TIPO_USUARIO WHERE cargo IS NOT NULL ORDER BY cargo",
            "SELECT DISTINCT TRIM(cargo) as cargo FROM \"TIPO_USUARIO\" WHERE cargo IS NOT NULL ORDER BY cargo"
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

                if (!cargos.isEmpty()) {
                    break;
                }

            } catch (SQLException e) {
                System.err.println("Falló con query: " + sql);
                System.err.println("    Mensaje: " + e.getMessage());
                lastException = e;
            } finally {
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
     * pasword.
     *
     * @param identificador El identificador a buscar.
     * @return true si el identificador ya existe, false en caso contrario.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public boolean existeIdentificador(String identificador) throws SQLException {
        String sql = "SELECT COUNT(*) FROM pasword WHERE identificador = ?";
        try (Connection conn = ConexionBD.conectar(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, identificador);
            ResultSet rs = pstmt.executeQuery();

            return rs.next() && rs.getInt(1) > 0;
        }
    }

    /**
     * Inserta un nuevo usuario, incluyendo la contraseña, el tipo de usuario y
     * los datos personales. UTILIZA TRANSACCIONES para asegurar que tanto la
     * contraseña como el usuario se inserten correctamente o que ambas
     * operaciones se reviertan (COMMIT/ROLLBACK).
     *
     * CORREGIDO: Ahora busca cargos de forma insensible a acentos.
     *
     * @param u Objeto Usuario con todos los datos a insertar.
     * @throws Exception Si ocurre un error de conexión, SQL o lógica.
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

            con.setAutoCommit(false);

            // 1. Insertar en PASWORD y obtener el ID generado
            String sqlPass = "INSERT INTO pasword (claveacceso, identificador) VALUES (?, ?) RETURNING idpasword";
            psPass = con.prepareStatement(sqlPass);
            psPass.setString(1, u.getPassword().getClaveAcceso());
            psPass.setString(2, u.getPassword().getIdentificador());

            ResultSet rsPass = psPass.executeQuery();
            int idPasword = -1;
            if (rsPass.next()) {
                idPasword = rsPass.getInt("idpasword");
            } else {
                throw new SQLException("Fallo al obtener ID de pasword.");
            }

            // 2. Obtener el ID del TIPO_USUARIO (insensible a acentos)
            String cargoOriginal = u.getTipoUsuario().getCargo();
            System.out.println("🔍 Buscando cargo: [" + cargoOriginal + "]");

            // Búsqueda que ignora acentos usando TRANSLATE
            String sqlTipo = """
                SELECT idtipousuario, cargo 
                FROM tipo_usuario 
                WHERE TRIM(cargo) = TRIM(?)
                   OR UPPER(TRANSLATE(TRIM(cargo), 'ÁÉÍÓÚáéíóú', 'AEIOUaeiou')) = 
                      UPPER(TRANSLATE(TRIM(?), 'ÁÉÍÓÚáéíóú', 'AEIOUaeiou'))
                LIMIT 1
                """;

            int idTipoUsuario = -1;

            try (PreparedStatement psTipo = con.prepareStatement(sqlTipo)) {
                psTipo.setString(1, cargoOriginal);
                psTipo.setString(2, cargoOriginal);

                try (ResultSet rsTipo = psTipo.executeQuery()) {
                    if (rsTipo.next()) {
                        idTipoUsuario = rsTipo.getInt("idtipousuario");
                        String cargoEncontrado = rsTipo.getString("cargo");
                        System.out.println("✅ Cargo encontrado: [" + cargoEncontrado + "] con ID: " + idTipoUsuario);
                    } else {
                        // Diagnóstico: Listar cargos disponibles
                        System.err.println("❌ Cargo NO encontrado: [" + cargoOriginal + "]");
                        System.err.println("📋 Cargos disponibles:");

                        String sqlListar = "SELECT cargo FROM tipo_usuario";
                        try (PreparedStatement psListar = con.prepareStatement(sqlListar); ResultSet rsListar = psListar.executeQuery()) {
                            while (rsListar.next()) {
                                System.err.println("   - [" + rsListar.getString("cargo") + "]");
                            }
                        }

                        throw new SQLException(
                                "Cargo '" + cargoOriginal + "' no encontrado en tipo_usuario. "
                                + "Verifique que el cargo existe en la base de datos."
                        );
                    }
                }
            }

            // 3. Insertar en USUARIO
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
            System.out.println("✅ Usuario insertado correctamente");

            con.commit();

        } catch (SQLException e) {
            System.err.println("❌ Error al agregar usuario: " + e.getMessage());
            try {
                if (con != null) {
                    con.rollback();
                }
            } catch (SQLException ex) {
                System.err.println("Error al hacer rollback: " + ex.getMessage());
            }
            throw new Exception("Error en la transacción al agregar usuario: " + e.getMessage(), e);
        } finally {
            try {
                if (psPass != null) {
                    psPass.close();
                }
                if (psUsuario != null) {
                    psUsuario.close();
                }
                if (con != null) {
                    con.setAutoCommit(true);
                }
                if (con != null) {
                    con.close();
                }
            } catch (SQLException e) {
                System.err.println("Error al cerrar conexión: " + e.getMessage());
            }
        }
    }

    /**
     * Obtiene una lista de todos los usuarios, uniéndolos con sus tipos de
     * usuario y detalles de contraseña.
     *
     * @return Lista de objetos Usuario.
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

        try (Connection con = ConexionBD.conectar(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
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
     * Actualiza los datos de un usuario (personales, rol y contraseña). UTILIZA
     * TRANSACCIONES para asegurar la actualización coherente de las tablas.
     *
     * CORREGIDO: Ahora busca cargos de forma insensible a acentos.
     *
     * @param u Objeto Usuario con los datos actualizados.
     * @throws Exception Si ocurre un error de base de datos o lógica.
     */
    public void actualizarUsuario(Usuario u) throws Exception {
        Connection con = null;
        try {
            con = ConexionBD.conectar();
            if (con == null) {
                throw new SQLException("Fallo de conexión.");
            }
            con.setAutoCommit(false);

            // 1. Actualizar PASWORD usando el identificador
            String sqlPass = "UPDATE pasword SET claveacceso=? WHERE identificador=?";
            try (PreparedStatement psPass = con.prepareStatement(sqlPass)) {
                psPass.setString(1, u.getPassword().getClaveAcceso());
                psPass.setString(2, u.getPassword().getIdentificador());
                psPass.executeUpdate();
            }

            // 2. Obtener el ID de TIPO_USUARIO (insensible a acentos)
            int idTipoUsuario = -1;
            String sqlTipo = """
                SELECT idtipousuario 
                FROM tipo_usuario 
                WHERE TRIM(cargo) = TRIM(?)
                   OR UPPER(TRANSLATE(TRIM(cargo), 'ÁÉÍÓÚáéíóú', 'AEIOUaeiou')) = 
                      UPPER(TRANSLATE(TRIM(?), 'ÁÉÍÓÚáéíóú', 'AEIOUaeiou'))
                LIMIT 1
                """;

            try (PreparedStatement psTipo = con.prepareStatement(sqlTipo)) {
                psTipo.setString(1, u.getTipoUsuario().getCargo());
                psTipo.setString(2, u.getTipoUsuario().getCargo());
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
                psUsuario.setString(5, u.getPassword().getIdentificador());
                psUsuario.executeUpdate();
            }

            con.commit();

        } catch (SQLException e) {
            System.out.println("Error al actualizar usuario: " + e.getMessage());
            try {
                if (con != null) {
                    con.rollback();
                }
            } catch (SQLException ex) {
                System.out.println("Error al hacer rollback: " + ex.getMessage());
            }
            throw new Exception("Error en la transacción al actualizar usuario.", e);
        } finally {
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
     * identificador. UTILIZA TRANSACCIONES. El orden es crucial: primero el
     * registro que depende (usuario), luego el registro principal (pasword)
     * para evitar errores de Foreign Key.
     *
     * @param identificador El identificador del usuario a eliminar.
     * @throws Exception Si ocurre un error de base de datos o lógica.
     */
    public void eliminarUsuario(String identificador) throws Exception {

        String sqlDeleteUsuario = """
            DELETE FROM usuario 
                WHERE idpasword IN (
                    SELECT idpasword FROM pasword WHERE identificador = ?
                )
            """;

        String sqlDeletePassword = "DELETE FROM pasword WHERE identificador = ?";

        Connection con = null;
        try {
            con = ConexionBD.conectar();
            if (con == null) {
                throw new SQLException("Fallo de conexión.");
            }
            con.setAutoCommit(false);

            try (PreparedStatement psUsuario = con.prepareStatement(sqlDeleteUsuario)) {
                psUsuario.setString(1, identificador);
                psUsuario.executeUpdate();
            }

            try (PreparedStatement psPass = con.prepareStatement(sqlDeletePassword)) {
                psPass.setString(1, identificador);
                psPass.executeUpdate();
            }

            con.commit();

        } catch (SQLException e) {
            System.out.println("Error al eliminar usuario: " + e.getMessage());
            try {
                if (con != null) {
                    con.rollback();
                }
            } catch (SQLException ex) {
                System.out.println("Error al hacer rollback: " + ex.getMessage());
            }
            throw new Exception("Error en la transacción al eliminar usuario.", e);
        } finally {
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
