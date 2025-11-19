package com.example.proyecto_final_prograiii.controllers;

import com.example.proyecto_final_prograiii.models.Usuario;
import com.example.proyecto_final_prograiii.utils.Sesion;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class PanelClienteController {

    @FXML
    private Label lblBienvenida;

    public void initialize(){
        Usuario usuario = Sesion.getUsuarioActual();

        if (usuario != null) {
            // INGRESO COMO USUARIO EXISTENTE
            lblBienvenida.setText(usuario.getNombreUsuario());
        } else {
            // INGRESO MODO INVITADO
            lblBienvenida.setText("Ha ingresado como invitado");
        }
    }
}
