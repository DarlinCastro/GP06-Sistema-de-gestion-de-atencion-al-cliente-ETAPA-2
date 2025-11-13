/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package capa_modelo;

/**
 *
 * @author DELL
 */
public class EstadoTicket {
    
    private String nivelPrioridad; 

    public EstadoTicket() {
    }

    public EstadoTicket(String nivelPrioridad) {
        // Ensures the value is cleaned and stored
        this.nivelPrioridad = nivelPrioridad != null ? nivelPrioridad.trim() : "";
    }

    public void setNivelPrioridad(String nivelPrioridad) {
        this.nivelPrioridad = nivelPrioridad;
    }

    public String getNivelPrioridad() {
        return nivelPrioridad;
    }
    
    @Override
    public String toString() {
        // Returns the stored level, ensuring no extra spaces are passed back
        return this.nivelPrioridad != null ? this.nivelPrioridad.trim() : "";
    }
}
