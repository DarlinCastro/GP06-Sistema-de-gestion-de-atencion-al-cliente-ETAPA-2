/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package capa_modelo;

import java.util.Date;

/**
 *
 * @author DELL
 */
public class Ticket {
    private EstadoTicket estadoTicket;
    private Date fechaAsignacion;
    private String numeroTicket;
    private Usuario tecnicoAsignado; 

    public Ticket() {
    }

    // Constructor actualizado para incluir el Usuario técnico asignado
    public Ticket(EstadoTicket estadoTicket, Date fechaAsignacion, String numeroTicket, Usuario tecnicoAsignado) {
        this.estadoTicket = estadoTicket;
        this.fechaAsignacion = fechaAsignacion;
        this.numeroTicket = numeroTicket != null ? numeroTicket.trim() : "";
        this.tecnicoAsignado = tecnicoAsignado; 
    }

    // --- GETTERS ---
    
    public Date getFechaAsignacion() {
        return fechaAsignacion;
    }

    public EstadoTicket getEstadoTicket() {
        return estadoTicket;
    }

    public String getNumeroTicket() {
        return numeroTicket;
    }

    public Usuario getTecnicoAsignado() {
        return tecnicoAsignado;
    }

    // --- SETTERS ---
    
    public void setEstadoTicket(EstadoTicket estadoTicket) {
        this.estadoTicket = estadoTicket;
    }

    public void setFechaAsignacion(Date fechaAsignacion) {
        this.fechaAsignacion = fechaAsignacion;
    }

    public void setNumeroTicket(String numeroTicket) {
        this.numeroTicket = numeroTicket;
    }

    // 💡 SETTER PARA EL TÉCNICO ASIGNADO
    public void setTecnicoAsignado(Usuario tecnicoAsignado) {
        this.tecnicoAsignado = tecnicoAsignado;
    }
    
    // Opcional: El toString se puede dejar sin implementar o devolver el número de ticket
    @Override
    public String toString() {
        return "Ticket N°: " + numeroTicket;
    }
}
