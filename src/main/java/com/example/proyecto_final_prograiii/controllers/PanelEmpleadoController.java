package com.example.proyecto_final_prograiii.controllers;

import com.example.proyecto_final_prograiii.models.Usuario;
import com.example.proyecto_final_prograiii.utils.Sesion;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class PanelEmpleadoController {


    private Sesion sesion;
    Usuario usuario = sesion.getUsuarioActual();

    @FXML
    private Button agregarBtn;

    @FXML
    private Button btnCerrarSesion;

    @FXML
    private Label lblBienvenida;

    @FXML
    void agregarOnAction(ActionEvent event) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/proyecto_final_prograiii/crearvehiculo-view.fxml"));
        try {
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/com/example/proyecto_final_prograiii/css/login.css").toExternalForm());
            Stage stage = new Stage();
            stage.setTitle("AGREGAR VEHICULO");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
    @FXML
    public void initialize() {
        lblBienvenida.setText("Bienvenido, " + usuario.getNombreUsuario());

    }

    @FXML
    void cerrarOnAction(ActionEvent event) {
        btnCerrarSesion.getScene().getWindow().hide();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/proyecto_final_prograiii/login-view.fxml"));
        try {
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/com/example/proyecto_final_prograiii/css/login.css").toExternalForm());
            Stage stage = new Stage();
            stage.setTitle("INICIO DE SESION");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}
