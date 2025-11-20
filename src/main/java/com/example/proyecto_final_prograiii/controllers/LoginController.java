package com.example.proyecto_final_prograiii.controllers;

import com.example.proyecto_final_prograiii.DAO.UsuarioDAO;
import com.example.proyecto_final_prograiii.config.ConexionDB;
import com.example.proyecto_final_prograiii.models.Usuario;
import com.example.proyecto_final_prograiii.utils.ClaveUtil;
import com.example.proyecto_final_prograiii.utils.Sesion;
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
        //clave para prueba despues de borra
        String contraPrueba = "111111";
        String clave = ClaveUtil.hashClave(contraPrueba);
        System.out.println(clave);
    }

    //evento de botones y label
    @FXML
    void CrearCliente(ActionEvent event) {
        abrirVista("crearusuariocliente-view.fxml", "Crear Cuenta de Cliente","login.css");

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
        //se guarda la sesion
        Sesion.iniciarSesion(usuario);
        switch (usuario.getRolId()) {
            case 1:
                abrirVista("paneladmin-view.fxml", "Panel Administrador","login.css");
                break;

            case 2:
                abrirVista("panelempleado-view.fxml", "Panel de Empleado","login.css");
                break;

            case 3:
                abrirVista("panel-cliente-view.fxml", "Panel de Clientes","login.css");
                break;

            default:
                alerta("Error", "Rol no reconocido.", Alert.AlertType.ERROR);
                break;
        }

    }

    @FXML
    void ModoInvitado(ActionEvent event) {
       abrirVista("panel-cliente-view.fxml", "Panel de Clientes - Modo Invitado","login.css");
    }

    //metodo para abrir vistas
    private void abrirVista(String fxml, String titulo,String css) {
        btnModoInvitado.getScene().getWindow().hide();
        btnIngresar.getScene().getWindow().hide();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/proyecto_final_prograiii/" + fxml));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            //scene.getStylesheets().add(getClass().getResource("/com/example/proyecto_final_prograiii/css/"+css).toExternalForm());
            Stage stage = new Stage();
            stage.setTitle(titulo);
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (Exception e) {
            alerta("Error al cargar vista", e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    public static void alerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setContentText(mensaje);
        alert.show();
    }







    //testeo de la base de datos (se ignora solo es de prueba)
    @FXML
    void ProbarConexion(ActionEvent event) {
        try (Connection conn = ConexionDB.getConnection()) {
            if (conn != null) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("EXTIO");
                alert.setContentText("Conexión exitosa a la base de datos");
                alert.showAndWait();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("ERROR");
                alert.setContentText("Conexión nula, algo salió mal");
                alert.showAndWait();
            }
        } catch (SQLException e) {
            ConexionDB.alerta("Fallo en la conexión", "No se pudo conectar a la base de datos", e);
        }
    }



}
