/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package capa_controladora;

import capa_modelo.Usuario;

/**
 * Clase utilitaria estática para mantener el estado del usuario actualmente
 * logueado en la aplicación.
 */
public class SesionActual {

    /**
     * Variable estática que almacena la instancia del objeto Usuario que
     * representa al usuario que ha iniciado sesión. * El uso de 'public static'
     * permite el acceso y modificación directo desde cualquier parte de la
     * aplicación, como: SesionActual.usuarioActual = nuevoUsuario;
     */
    public static Usuario usuarioActual;
}
