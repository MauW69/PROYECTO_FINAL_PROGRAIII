package com.example.proyecto_final_prograiii.utils;

import com.example.proyecto_final_prograiii.models.Usuario;

public class Sesion {
    private static Usuario usuarioActual;

    public static void iniciarSesion(Usuario usuario) {
        usuarioActual = usuario;
    }

    public static Usuario getUsuarioActual() {
        return usuarioActual;
    }

    public static void cerrarSesion() {
        usuarioActual = null;
    }

    public static int getRolActual() {
        if (usuarioActual != null) {
            return usuarioActual.getRolId();
        }
        return -1;
    }
}
