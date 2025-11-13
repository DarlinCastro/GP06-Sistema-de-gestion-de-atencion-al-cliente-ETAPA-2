/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package capa_modelo;

/**
 *
 * @author DELL
 */
public class TipoUsuario {
    
    private String cargo;

    public TipoUsuario() {
    }

    public TipoUsuario(String cargo) {
        this.cargo = cargo != null ? cargo.trim() : "";
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public String getCargo() {
        return cargo;
    }

    @Override
    public String toString() {
        return cargo;
    }
}
