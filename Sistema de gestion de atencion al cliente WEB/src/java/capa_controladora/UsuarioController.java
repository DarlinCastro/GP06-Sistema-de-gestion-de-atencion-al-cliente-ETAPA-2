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

public class UsuarioController {

    private Connection conexion;

    public UsuarioController(Connection conexion) {
        this.conexion = conexion;
    }
    public TipoUsuario introducirCredenciales(String identificador, String clave) throws SQLException {
        TipoUsuario tipoUsuario = null;
        
        String sql = "SELECT tu.cargo\n"+
                     "FROM usuario u\n"+
                     "INNER JOIN pasword p ON u.idpasword = p.idpasword\n"+
                     "INNER JOIN tipo_usuario tu ON u.idtipousuario = tu.idtipousuario\n"+
                     "WHERE p.identificador = ? AND p.claveacceso = ?";

        try (PreparedStatement pst = conexion.prepareStatement(sql)) {
            pst.setString(1, identificador);
            pst.setString(2, clave);
            
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    tipoUsuario = new TipoUsuario();
                    tipoUsuario.setCargo(rs.getString("cargo").trim());
                }
            }
        } 
        return tipoUsuario;
    }

    public Usuario obtenerUsuarioPorCredenciales(String identificador, String clave) throws SQLException {
        Usuario usuario = null;
        
        String sql = "SELECT u.nombres, u.apellidos, u.correoelectronico, tu.cargo, p.identificador, p.claveacceso " +
                     "FROM usuario u " +
                     "INNER JOIN pasword p ON u.idpasword = p.idpasword " +
                     "INNER JOIN tipo_usuario tu ON u.idtipousuario = tu.idtipousuario " +
                     "WHERE p.identificador = ? AND p.claveacceso = ?";

        try (PreparedStatement pst = conexion.prepareStatement(sql)) {
            pst.setString(1, identificador);
            pst.setString(2, clave);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    usuario = new Usuario();
                    
                    usuario.setNombres(rs.getString("nombres").trim());
                    usuario.setApellidos(rs.getString("apellidos").trim());
                    usuario.setCorreoElectronico(rs.getString("correoelectronico").trim());

                    TipoUsuario tipo = new TipoUsuario();
                    tipo.setCargo(rs.getString("cargo").trim());
                    usuario.setTipoUsuario(tipo);

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