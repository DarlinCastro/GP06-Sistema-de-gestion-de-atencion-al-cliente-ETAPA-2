/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package capa_modelo;

/**
 *
 * @author DELL
 */
public class Password {
    private String claveAcceso; 
    private String identificador; 

    public Password() {
    }

    public Password(String claveAcceso, String identificador) {
        this.claveAcceso = claveAcceso;
        this.identificador = identificador != null ? identificador.trim() : "";
    }

    public String getClaveAcceso() {
        return claveAcceso;
    }

    public String getIdentificador() {
        return identificador;
    }

    public void setClaveAcceso(String claveAcceso) {
        this.claveAcceso = claveAcceso;
    }

    public void setIdentificador(String identificador) {
        this.identificador = identificador;
    }
}
