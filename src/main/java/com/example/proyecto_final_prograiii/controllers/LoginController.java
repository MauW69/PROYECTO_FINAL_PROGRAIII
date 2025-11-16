package com.example.proyecto_final_prograiii.controllers;

import com.example.proyecto_final_prograiii.config.ConexionDB;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;


import java.sql.Connection;
import java.sql.SQLException;

public class LoginController {

    @FXML
    private Button btnProbarConexion;



    public void initialize(){
    }

    @FXML
    void ProbarConexion(ActionEvent event) {
        try (Connection conn = ConexionDB.getConnection()) {
            if (conn != null) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Éxito");
                alert.setContentText("Conexión exitosa a la base de datos");
                alert.showAndWait();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setContentText("Conexión nula, algo salió mal");
                alert.showAndWait();
            }
        } catch (SQLException e) {
            ConexionDB.alerta("Fallo en la conexión", "No se pudo conectar a la base de datos", e);
        }
    }


    public static void alerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setContentText(mensaje);
        alert.show();
    }
}
