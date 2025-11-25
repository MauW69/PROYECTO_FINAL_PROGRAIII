package com.example.proyecto_final_prograiii.utils;

import com.example.proyecto_final_prograiii.models.Cliente;
import com.example.proyecto_final_prograiii.models.Usuario;

public class Sesion {

    // === SESION DEL USUARIO GENERAL (empleado o cliente) ===
    private static Usuario usuarioActual;

    public static void iniciarSesion(Usuario usuario) {
        usuarioActual = usuario;
    }

    public static Usuario getUsuarioActual() {
        return usuarioActual;
    }

    public static int getRolActual() {
        return (usuarioActual != null) ? usuarioActual.getRolId() : -1;
    }

    // === DATOS EXTRA DEL CLIENTE (solo si el rol es CLIENTE) ===
    private static Cliente clienteActual;

    public static void setClienteActual(Cliente cliente) {
        clienteActual = cliente;
    }

    public static Cliente getClienteActual() {
        return clienteActual;
    }


    // === CERRAR SESION ===
    public static void cerrarSesion() {
        usuarioActual = null;
        clienteActual = null; // Limpieza TOTAL
    }
}
