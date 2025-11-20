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

public class GestionarUsuariosController {

    public GestionarUsuariosController() {
    }

    /*public List<String> obtenerTiposUsuarioParaCargos() throws SQLException {
        List<String> cargos = new ArrayList<>();
        String sql = "SELECT DISTINCT cargo FROM TIPO_USUARIO ORDER BY cargo";

        try (Connection conn = ConexionBD.conectar(); PreparedStatement pstmt = conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                cargos.add(rs.getString(1).trim());
                //cargos.add(rs.getString("cargo").trim());
            }
        } catch (SQLException e) {
            System.out.println("❌ Error al obtener cargos: " + e.getMessage());
            throw e;
        }
        return cargos;
    }*/
    public List<String> obtenerTiposUsuarioParaCargos() throws SQLException {
        List<String> cargos = new ArrayList<>();

        // Prueba ambas versiones del nombre de tabla
        String[] sqlVariants = {
            "SELECT DISTINCT cargo FROM tipo_usuario ORDER BY cargo",
            "SELECT DISTINCT cargo FROM TIPO_USUARIO ORDER BY cargo",
            "SELECT DISTINCT cargo FROM \"TIPO_USUARIO\" ORDER BY cargo"
        };

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        SQLException lastException = null;

        for (String sql : sqlVariants) {
            try {
                conn = ConexionBD.conectar();
                System.out.println("✅ Intentando query: " + sql);

                pstmt = conn.prepareStatement(sql);
                rs = pstmt.executeQuery();

                while (rs.next()) {
                    String cargo = rs.getString("cargo");
                    if (cargo != null && !cargo.trim().isEmpty()) {
                        cargos.add(cargo.trim());
                        System.out.println("✅ Cargo agregado: [" + cargo.trim() + "]");
                    }
                }

                System.out.println("📋 Total de cargos obtenidos: " + cargos.size());

                // Si encontró resultados, salir del loop
                if (!cargos.isEmpty()) {
                    break;
                }

            } catch (SQLException e) {
                System.err.println("⚠️ Falló con query: " + sql);
                System.err.println("   Mensaje: " + e.getMessage());
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
                    System.err.println("⚠️ Error al cerrar recursos: " + e.getMessage());
                }
            }
        }

        if (cargos.isEmpty() && lastException != null) {
            System.err.println("❌ No se pudo obtener cargos con ninguna variante de query");
            throw lastException;
        }

        if (cargos.isEmpty()) {
            System.err.println("⚠️ WARNING: La tabla tipo_usuario está VACÍA");
        }

        return cargos;
    }

    public boolean existeIdentificador(String identificador) throws SQLException {
        String sql = "SELECT COUNT(*) FROM pasword WHERE identificador = ?";
        try (Connection conn = ConexionBD.conectar(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, identificador);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

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

            String sqlTipo = "SELECT idtipousuario FROM tipo_usuario WHERE cargo = ?";
            PreparedStatement psTipo = con.prepareStatement(sqlTipo);
            psTipo.setString(1, u.getTipoUsuario().getCargo());
            ResultSet rsTipo = psTipo.executeQuery();
            int idTipoUsuario = -1;
            if (rsTipo.next()) {
                idTipoUsuario = rsTipo.getInt("idtipousuario");
            } else {
                throw new SQLException("Cargo no encontrado en tipo_usuario");
            }
///////////////////////////////////////////////////////////////////////////////////////////////////////////
            //Para insertar usuario se ocupa sqlUser ya que sqlUsuario está para la sentencia UPDATE
            String sqlUser = """
                INSERT INTO usuario (nombres, apellidos, correoelectronico, idtipousuario, idpasword)
                VALUES (?, ?, ?, ?, ?)
                """;
////////////////////////////////////////////////////////////////////////////////////////////////////////////            
            psUsuario = con.prepareStatement(sqlUser);
            psUsuario.setString(1, u.getNombres());
            psUsuario.setString(2, u.getApellidos());
            psUsuario.setString(3, u.getCorreoElectronico());
            psUsuario.setInt(4, idTipoUsuario);
            psUsuario.setInt(5, idPasword);

            psUsuario.executeUpdate();
            con.commit();

        } catch (SQLException e) {
            System.out.println("❌ Error al agregar usuario: " + e.getMessage());
            try {
                if (con != null) {
                    con.rollback();
                }
            } catch (SQLException ex) {
                System.out.println("⚠️ Error al hacer rollback: " + ex.getMessage());
            }
            throw new Exception("Error en la transacción al agregar usuario.", e);
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
                System.out.println("⚠️ Error al cerrar conexión: " + e.getMessage());
            }
        }
    }

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

    public void actualizarUsuario(Usuario u) throws Exception {
        Connection con = null;
        try {
            con = ConexionBD.conectar();
            if (con == null) {
                throw new SQLException("Fallo de conexión.");
            }
            con.setAutoCommit(false);

            String sqlPass = "UPDATE pasword SET claveacceso=? WHERE identificador=?";
            PreparedStatement psPass = con.prepareStatement(sqlPass);
            psPass.setString(1, u.getPassword().getClaveAcceso());
            psPass.setString(2, u.getPassword().getIdentificador());
            psPass.executeUpdate();

            String sqlTipo = "SELECT idtipousuario FROM tipo_usuario WHERE cargo = ?";
            PreparedStatement psTipo = con.prepareStatement(sqlTipo);
            psTipo.setString(1, u.getTipoUsuario().getCargo());
            ResultSet rsTipo = psTipo.executeQuery();
            int idTipoUsuario = -1;
            if (rsTipo.next()) {
                idTipoUsuario = rsTipo.getInt("idtipousuario");
            } else {
                throw new SQLException("Cargo no encontrado");
            }

            String sqlUsuario = """
                UPDATE usuario 
                    SET nombres=?, apellidos=?, correoelectronico=?, idtipousuario=?
                    WHERE idpasword IN (
                        SELECT idpasword FROM pasword WHERE identificador = ?
                    )
                """;
            PreparedStatement psUsuario = con.prepareStatement(sqlUsuario);
            psUsuario.setString(1, u.getNombres());
            psUsuario.setString(2, u.getApellidos());
            psUsuario.setString(3, u.getCorreoElectronico());
            psUsuario.setInt(4, idTipoUsuario);
            psUsuario.setString(5, u.getPassword().getIdentificador());
            psUsuario.executeUpdate();

            con.commit();

        } catch (SQLException e) {
            System.out.println("❌ Error al actualizar usuario: " + e.getMessage());
            try {
                if (con != null) {
                    con.rollback();
                }
            } catch (SQLException ex) {
                System.out.println("⚠️ Error al hacer rollback: " + ex.getMessage());
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
                System.out.println("⚠️ Error al cerrar conexión: " + e.getMessage());
            }
        }
    }

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
            System.out.println("❌ Error al eliminar usuario: " + e.getMessage());
            try {
                if (con != null) {
                    con.rollback();
                }
            } catch (SQLException ex) {
                System.out.println("⚠️ Error al hacer rollback: " + ex.getMessage());
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
                System.out.println("⚠️ Error al cerrar conexión: " + e.getMessage());
            }
        }
    }
}
