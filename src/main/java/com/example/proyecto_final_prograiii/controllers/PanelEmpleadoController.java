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
import java.net.URL;

public class PanelEmpleadoController {



    @FXML
    private Button agregarBtn;

    @FXML
    private Button btnCerrarSesion;

    @FXML
    private Button btnGestionar; // nuevo

    @FXML
    private Label lblBienvenida;

    @FXML
    void agregarOnAction(ActionEvent event) {
        try {
            URL res = getClass().getResource("/com/example/proyecto_final_prograiii/crearvehiculo-view.fxml");
            FXMLLoader fxmlLoader = new FXMLLoader(res);
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
    void gestionarOnAction(ActionEvent event) {
        try {
            URL res = getClass().getResource("/com/example/proyecto_final_prograiii/vehiculos-gestion-view.fxml");
            FXMLLoader loader = new FXMLLoader(res);
            Parent root = loader.load();

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/com/example/proyecto_final_prograiii/css/login.css").toExternalForm());

            Stage stage = new Stage();
            stage.setTitle("GESTIONAR VEHÍCULOS");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    public void initialize() {

        if (Sesion.getUsuarioActual() != null) {
            lblBienvenida.setText("Bienvenido, " + Sesion.getUsuarioActual().getNombreUsuario());
        } else {
            lblBienvenida.setText("Bienvenido");
        }
    }

    @FXML
    void cerrarOnAction(ActionEvent event) {
        //limpiar la sesion
        Sesion.cerrarSesion();
        btnCerrarSesion.getScene().getWindow().hide();
        try {
            FXMLLoader loader = new FXMLLoader( getClass().getResource("/com/example/proyecto_final_prograiii/login-view.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/com/example/proyecto_final_prograiii/css/login.css").toExternalForm());
            Stage stage = (Stage) btnCerrarSesion.getScene().getWindow();
            stage.setTitle("INICIO DE SESIÓN");
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}
