package com.example.proyecto_final_prograiii.controllers;

import com.example.proyecto_final_prograiii.DAO.ClienteDAO;
import com.example.proyecto_final_prograiii.utils.ClaveUtil;
import com.example.proyecto_final_prograiii.utils.RecuperarClave;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class RecuperarClaveController {

    @FXML
    private Button btnCerrarCambioClave;
    @FXML
    private Button btnCambiarClave;

    @FXML
    private Button btnMandarPin;

    @FXML
    private PasswordField txtClaveNueva;

    @FXML
    private PasswordField txtConfirmarClaveNueva;

    @FXML
    private TextField txtCorreo;

    @FXML
    private TextField txtPin;
    // PIN generado temporalmente
    private String pinGenerado;
    // Correo asociado al PIN temporalmente
    private String correoAsociado;

    // DAO del cliente
    private ClienteDAO clienteDAO;


    public void initialize(){
        //inicialmente deshabilitar campos de PIN y nuevas claves
        txtPin.setDisable(true);
        txtClaveNueva.setDisable(true);
        txtConfirmarClaveNueva.setDisable(true);
        btnCambiarClave.setDisable(true);
        //inicializar DAO
        clienteDAO = new ClienteDAO();
    }

    @FXML
    void CambiarClave(ActionEvent event) {

        String pinIngresado = txtPin.getText().trim();
        String nuevaClave = txtClaveNueva.getText().trim();
        String confirmarNuevaClave = txtConfirmarClaveNueva.getText().trim();

        if (confirmarNuevaClave.isEmpty() || pinIngresado.isEmpty() || nuevaClave.isEmpty()) {
            alerta("Error", "Todos los campos son obligatorios", Alert.AlertType.WARNING);
            return;
        }

        // Validar PIN
        if (!pinIngresado.equals(pinGenerado)) {
            alerta("Error", "El PIN ingresado es incorrecto", Alert.AlertType.ERROR);
            return;
        }

        //validar la longitud de las claves
        if (nuevaClave.length() < 8){
            alerta("alerta", "La clave debe de tener al menos 8 digitos", Alert.AlertType.INFORMATION);
            return;
        }

        //validar que ambas claves sea iguales
        if (!confirmarNuevaClave.equals(nuevaClave)){
            alerta("Error", "las claves no coinciden", Alert.AlertType.ERROR);
            return;
        }

        // Hash de la nueva clave (puedes usar SHA-256, bcrypt, etc.)
        String claveHash = ClaveUtil.hashClave(nuevaClave);

        // Actualizar clave en la base de datos usando el correo asociado al PIN
        boolean actualizado = clienteDAO.actualizarClavePorCorreo(correoAsociado, claveHash);

        if (actualizado) {
            alerta("Exito", "La contraseña se ha actualizado correctamente", Alert.AlertType.INFORMATION);
            limpiarCampos();
            // Limpiar PIN y correo asociado después de actualizar
            pinGenerado = null;
            correoAsociado = null;
            // Volver a deshabilitar los campos
            txtPin.setDisable(true);
            txtClaveNueva.setDisable(true);
            txtConfirmarClaveNueva.setDisable(true);
            btnCambiarClave.setDisable(true);
            Login();//se manda de regreso al login
        } else {
            alerta("Error", "No se pudo actualizar la contraseña", Alert.AlertType.ERROR);
        }
    }
    @FXML
    void CerrarCambioClave(ActionEvent event) {
        Login();
    }


    //metodo que manda el pin al correo
    @FXML
    void MandarPin(ActionEvent event) {

        String correo = txtCorreo.getText().trim();

        if (correo.isEmpty()) {
            alerta("Error", "Debe ingresar un correo", Alert.AlertType.WARNING);
            return;
        }

        // validar que el correo exista en la base de datos
        if (!clienteDAO.existeCorreo(correo)) {
            alerta("Informaciun", "Si el correo existe, se ha enviado un PIN", Alert.AlertType.INFORMATION);
            return;
        }
        // generar PIN
        pinGenerado = RecuperarClave.generarPin();
        correoAsociado = correo; // almacenar temporalmente para cambiar la clave
        // enviar correo
        boolean enviado = RecuperarClave.enviarPinCorreo(correo, pinGenerado);
        if (enviado) {
            alerta("Exito", "Se ha enviado un PIN al correo ingresado", Alert.AlertType.INFORMATION);
            //se activan al mandar el pin al correo
            txtPin.setDisable(false);
            txtClaveNueva.setDisable(false);
            txtConfirmarClaveNueva.setDisable(false);
            btnCambiarClave.setDisable(false);


        } else {
            alerta("Error", "No se pudo enviar el PIN. Intente nuevamente", Alert.AlertType.ERROR);
        }


    }

    //metodos auxiliares
    private void alerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void limpiarCampos() {
        txtCorreo.clear();
        txtPin.clear();
        txtClaveNueva.clear();
        txtConfirmarClaveNueva.clear();
    }

    //una vez se concrete el cambio de pin, se cerrar y se mandara al login
    private void Login() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/proyecto_final_prograiii/login-view.fxml"));
            Parent parent = loader.load();
            Scene scene = new Scene(parent);
            scene.getStylesheets().add(getClass().getResource("/com/example/proyecto_final_prograiii/css/login.css").toExternalForm());
            Stage stage = (Stage) btnCambiarClave.getScene().getWindow();
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            alerta("Error", "No se pudo abrir la ventana de login", Alert.AlertType.ERROR);
        }
    }


}
