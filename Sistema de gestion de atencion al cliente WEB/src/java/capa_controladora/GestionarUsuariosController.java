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

public class GestionarUsuariosController {

    public GestionarUsuariosController() {
    }

    public List<String> obtenerTiposUsuarioParaCargos() throws SQLException {
        List<String> cargos = new ArrayList<>();
        String sql = "SELECT DISTINCT cargo FROM TIPO_USUARIO ORDER BY cargo";
        
        try (Connection conn = ConexionBD.conectar(); 
              PreparedStatement pstmt = conn.prepareStatement(sql); 
              ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                cargos.add(rs.getString("cargo").trim());
            }
        } catch (SQLException e) {
            System.out.println("❌ Error al obtener cargos: " + e.getMessage());
            throw e; 
        }
        return cargos;
    }

    public void agregarUsuario(Usuario u) throws Exception {
        Connection con = null;
        PreparedStatement psPass = null;
        PreparedStatement psUsuario = null;

        try {
            con = ConexionBD.conectar();
            if (con == null) throw new SQLException("Fallo de conexión.");
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

            String sqlUsuario = """
                INSERT INTO usuario (nombres, apellidos, correoelectronico, idtipousuario, idpasword)
                VALUES (?, ?, ?, ?, ?)
                """;
            psUsuario = con.prepareStatement(sqlUsuario);
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
                if (con != null) con.rollback();
            } catch (SQLException ex) {
                System.out.println("⚠️ Error al hacer rollback: " + ex.getMessage());
            }
            throw new Exception("Error en la transacción al agregar usuario.", e);
        } finally {
            try {
                if (psPass != null) psPass.close();
                if (psUsuario != null) psUsuario.close();
                if (con != null) con.setAutoCommit(true);
                if (con != null) con.close();
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

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

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
            if (con == null) throw new SQLException("Fallo de conexión.");
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
                if (con != null) con.rollback();
            } catch (SQLException ex) {
                System.out.println("⚠️ Error al hacer rollback: " + ex.getMessage());
            }
            throw new Exception("Error en la transacción al actualizar usuario.", e);
        } finally {
            try {
                if (con != null) con.setAutoCommit(true);
                if (con != null) con.close();
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
            if (con == null) throw new SQLException("Fallo de conexión.");
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
                if (con != null) con.rollback();
            } catch (SQLException ex) {
                System.out.println("⚠️ Error al hacer rollback: " + ex.getMessage());
            }
            throw new Exception("Error en la transacción al eliminar usuario.", e);
        } finally {
             try {
                if (con != null) con.setAutoCommit(true);
                if (con != null) con.close();
            } catch (SQLException e) {
                System.out.println("⚠️ Error al cerrar conexión: " + e.getMessage());
            }
        }
    }
}