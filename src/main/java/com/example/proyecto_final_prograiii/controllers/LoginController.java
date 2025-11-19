package com.example.proyecto_final_prograiii.controllers;

import com.example.proyecto_final_prograiii.DAO.UsuarioDAO;
import com.example.proyecto_final_prograiii.config.ConexionDB;
import com.example.proyecto_final_prograiii.models.Usuario;
import com.example.proyecto_final_prograiii.utils.ClaveUtil;
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

    private UsuarioDAO usuarioDAO = new UsuarioDAO();


    //inicializador
    public void initialize(){
    }

    //evento de botones y label
    @FXML
    void CrearCliente(ActionEvent event) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/proyecto_final_prograiii/crearusuariocliente-view.fxml"));
        try {
            Parent parent = loader.load();
            Scene scene = new Scene(parent);
            Stage stage = new Stage();
            stage.setTitle("Crear usuario cliente");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) {
            alerta("Error", "Error al abrir el formulario", Alert.AlertType.ERROR);
        }

    }

    @FXML
    void Ingresar(ActionEvent event) {
        //para los tipo usuarios con rol cliente abrira el panel de clientes (apartado para rentar autos etc)
        //para el que tenga rol de admin abrira el panel de admin(opciones del administrador)
        //para el que tenga rol de cliente abrira el panel de empelado(opciones del empleado)
        String nombreUsuario = txtUsuario.getText().trim();
        String clave = txtClave.getText().trim();

        //validacion de campos vacios
        if (nombreUsuario.isEmpty() || clave.isEmpty()) {
            alerta("Campos vacios", "Debes ingresar usuario y contraseña.", Alert.AlertType.INFORMATION);
            return;
        }
        //consultar usuario
        Usuario usuario = usuarioDAO.obtenerPorNombreUsuario(nombreUsuario);

        if (usuario == null) {
            alerta("Usuario no encontrado", "El usuario ingresado no existe.", Alert.AlertType.INFORMATION);
            return;
        }
        //validar clave
        if (!ClaveUtil.verificarClave(clave, usuario.getClaveHash())) {
            alerta("Contraseña incorrecta", "La contraseña no coincide.", Alert.AlertType.ERROR);
            return;
        }
        // =====================
        // discriminacion por rol
        // =====================
        switch (usuario.getRolId()) {
            case 1:
                abrirVista("paneladmin-view.fxml", "Panel Administrador");
                break;

            case 2:
                abrirVista("panelempleado-view.fxml", "Panel de Empleado");
                break;

            case 3:
                abrirVista("panelcliente-view.fxml", "Panel de Clientes");
                break;

            default:
                alerta("Error", "Rol no reconocido.", Alert.AlertType.ERROR);
                break;
        }

    }

    @FXML
    void ModoInvitado(ActionEvent event) {
        btnModoInvitado.getScene().getWindow().hide();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/proyecto_final_prograiii/panelcliente-view.fxml"));
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

        //este abrira el panel de clientes, mas no podra realizar interacciones y en caso de querer interactural manadara hacia la vista de
        //crear usuario vista "crearusuariocliente-view.fxml" controller "CrearUsuarioClienteController.Java"(la vista de crear usuario solo creara usuarios de tipo cliente)
    }

    //metodo para abrir vistas
    private void abrirVista(String fxml, String titulo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/proyecto_final_prograiii/" + fxml));
            Scene scene = new Scene(loader.load());
            //falta implementar css
            Stage stage = (Stage) btnIngresar.getScene().getWindow();
            stage.setTitle(titulo);
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            alerta("Error al cargar vista", e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }



    //testeo de la base de datos (se ignora solo es de prueba)
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


    public static void alerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setContentText(mensaje);
        alert.show();
    }
}
