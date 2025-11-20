/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package base_datos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {
    private static final String URL ="jdbc:postgresql://localhost:5432/proyecto_prnIII";
    private static final String USER="postgres";
    private static final String PASSWORD="Admin";
    
    public static Connection conectar(){
        Connection conn=null;
        try {
            Class.forName("org.postgresql.Driver");
            //establecer conexion
            conn=DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Conexion exitosa a PostgreSQL");
        } catch (ClassNotFoundException e) {
            System.out.println("No se encontró el driver JDBC de PostgreSQL");
            e.printStackTrace();
        }catch(SQLException e){
            System.out.println("Error al conectar a la BD");
            e.printStackTrace();
        }
        return conn;
    }
    
}