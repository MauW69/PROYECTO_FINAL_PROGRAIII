package com.example.proyecto_final_prograiii.controllers;

import com.example.proyecto_final_prograiii.config.ConexionDB;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class LoginController {

    @FXML
    private Button btnIngresar;

    @FXML
    private Button btnModoInvitado;

    @FXML
    private Button btnProbarConexion;

    @FXML
    private Hyperlink lblCrearCliente;

    @FXML
    private PasswordField txtClave;

    @FXML
    private TextField txtUsuario;


    //inicializador
    public void initialize(){
    }

    //evento de botones y label
    @FXML
    void CrearCliente(ActionEvent event) {

    }

    @FXML
    void Ingresar(ActionEvent event) {

    }

    @FXML
    void ModoInvitado(ActionEvent event) {
        btnModoInvitado.getScene().getWindow().hide();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/proyecto_final_prograiii/rentacar-view.fxml"));
        try {
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root);
            // Obtener la ventana actual desde el botón btnModoInvitado
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("RENTA CAR");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }





    //testeo de la base de datos
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
