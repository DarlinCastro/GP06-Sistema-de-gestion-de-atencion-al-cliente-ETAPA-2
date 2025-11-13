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
import javax.swing.JOptionPane;

public class CrearCuentaController {

    public boolean registrarUsuario(Usuario usuario, String contraseña, String identificador) {
        Connection con = null;
        PreparedStatement pstPassword = null;
        PreparedStatement pstUsuario = null;

        try {
            con = ConexionBD.conectar();
            con.setAutoCommit(false);
            String sqlPassword = "INSERT INTO pasword (claveacceso, identificador) VALUES(?, ?) RETURNING idpasword";
            pstPassword = con.prepareStatement(sqlPassword);
            pstPassword.setString(1, contraseña);
            pstPassword.setString(2, identificador);
            ResultSet rs=pstPassword.executeQuery();
            
            int idPasword=0;
            if(rs.next()){
                idPasword=rs.getInt("idpasword");
            }
            int tipoUsuario=2;

            String sqlUsuario="INSERT INTO usuario (idpasword, idtipousuario, nombres, apellidos, correoelectronico) VALUES(?, ?, ?, ?, ?)";
            pstUsuario=con.prepareStatement(sqlUsuario);
            pstUsuario.setInt(1, idPasword);
            pstUsuario.setInt(2, tipoUsuario);
            pstUsuario.setString(3, usuario.getNombres());
            pstUsuario.setString(4, usuario.getApellidos());
            pstUsuario.setString(5, usuario.getCorreoElectronico());
            
            int filas=pstUsuario.executeUpdate();

            con.commit();
            return filas>0;
            
        } catch (Exception e) {
            try {
                if(con!=null) con.rollback();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error al registrar usuario: " + e.getMessage());
            return false;
        } finally{
            try {
                if(pstPassword!=null) pstPassword.close();
                if(pstUsuario!=null) pstUsuario.close();
                if(con != null) con.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
