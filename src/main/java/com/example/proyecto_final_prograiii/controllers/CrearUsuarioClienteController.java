package com.example.proyecto_final_prograiii.controllers;

import com.example.proyecto_final_prograiii.DAO.ClienteDAO;
import com.example.proyecto_final_prograiii.DAO.UsuarioDAO;
import com.example.proyecto_final_prograiii.models.Cliente;
import com.example.proyecto_final_prograiii.models.Usuario;
import com.example.proyecto_final_prograiii.utils.ClaveUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class CrearUsuarioClienteController {

    @FXML
    private Button btnRegistrarCliente;

    @FXML
    private Spinner<Integer> spEdad;

    @FXML
    private TextField txtApellido;

    @FXML
    private PasswordField txtClave;

    @FXML
    private TextField txtCorreo;

    @FXML
    private TextField txtDireccion;

    @FXML
    private TextField txtNombre;

    @FXML
    private TextField txtNombreUsuario;

    @FXML
    private TextField txtTelefono;

    public void initialize(){
        spEdad.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 1));
    }
    @FXML
    void RegistrarCliente(ActionEvent event) {
        String nombre = txtNombre.getText().trim();
        String apellido = txtApellido.getText().trim();
        int edad = spEdad.getValue();
        String telefono = txtTelefono.getText().trim();
        String correo = txtCorreo.getText().trim();
        String direccion = txtDireccion.getText().trim();
        String nombreUsuario = txtNombreUsuario.getText().trim();
        String clave = txtClave.getText().trim();

        //validacion de campos vacios
        if (nombreUsuario.isEmpty() || correo.isEmpty() || clave.isEmpty()
                || nombre.isEmpty() || apellido.isEmpty()
                || telefono.isEmpty() || direccion.isEmpty()) {
            alerta("Campos incompletos", "Todos los campos son obligatorios", Alert.AlertType.WARNING);
            return;
        }

        //VALIDACION CORREO ELECTRONICO
        if (correo == null || !correo.matches("^[\\w._%+-]+@[\\w.-]+\\.[a-zA-Z]{2,6}$")) {
            alerta("Correo inválido", "El correo electrónico no es válido.", Alert.AlertType.WARNING);
            return;
        }
        //validacion de edad (mayores de edad)
        if (edad <= 17) {
            alerta("Edad invalida", "solo se pueden registrar mayores de edad.", Alert.AlertType.WARNING);
            return;
        }
        //validacion de la cantidad de digitos del telefono
        if(telefono.length() != 8){
            alerta("Telefono", "El numero de telefono tiene que ser 8 digitos", Alert.AlertType.INFORMATION);
            return;
        }


        //UNA VEZ PASADA LAS VALIDACIONES

        //clave hasheada
        String claveHash = ClaveUtil.hashClave(clave);

        // ------------ CREAR USUARIO -------------
        //se valida que el username sea unico
        UsuarioDAO usuarioDAO = new UsuarioDAO();
        if (usuarioDAO.existeNombreUsuario(nombreUsuario)) {
            alerta("Error", "El nombre de usuario ya esta en uso.", Alert.AlertType.ERROR);
            return;
        }
        Usuario nuevoUsuario = new Usuario(nombreUsuario, claveHash, 3);//este controller solo es para los clintes por lo que siempre tendra rolId 3

        //retornamos el id
        int usuarioId = usuarioDAO.crearUsuario(nuevoUsuario);
        if (usuarioId == -1){
            alerta("Error", "No se pudo crear el usuario.", Alert.AlertType.ERROR);
            return;
        }

        // ------------ CREAR CLIENTE -------------
        ClienteDAO clienteDAO = new ClienteDAO();
        Cliente nuevoCliente = new Cliente(usuarioId, nombre, apellido, edad, correo, telefono, direccion);

        boolean creadoCliente = clienteDAO.crearCliente(nuevoCliente);
        if (!creadoCliente) {
            //rollback simple: eliminar usuario creado para evitar usuario huerfano en caso de error
            usuarioDAO.eliminarUsuario(usuarioId);
            alerta("Error", "No se pudo crear el cliente. Se deshizo la creación del usuario.", Alert.AlertType.ERROR);
            return;
        }

        alerta("Exito", "Usuario y cliente creados exitosamente", Alert.AlertType.INFORMATION);
        btnRegistrarCliente.getScene().getWindow().hide();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/proyecto_final_prograiii/login-view.fxml"));
        try {
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setTitle("INICIO DE SESION");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    public static void alerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

}
