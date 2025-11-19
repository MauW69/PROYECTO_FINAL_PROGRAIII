package com.example.proyecto_final_prograiii.controllers;

import com.example.proyecto_final_prograiii.utils.Sesion;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class PanelClienteController {

    @FXML
    private Label lblBienvenida;

    public void initialize(){
        lblBienvenida.setText(String.format("BIENVENIDO : %s", Sesion.getUsuarioActual().getNombreUsuario()));
    }
}
