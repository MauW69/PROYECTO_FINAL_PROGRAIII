package com.example.proyecto_final_prograiii.controllers;

import com.example.proyecto_final_prograiii.DAO.ClienteDAO;
import com.example.proyecto_final_prograiii.DTO.ClienteDTO;
import com.example.proyecto_final_prograiii.models.Cliente;
import com.example.proyecto_final_prograiii.models.Usuario;
import com.example.proyecto_final_prograiii.utils.Sesion;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.converter.IntegerStringConverter;

import java.util.List;

public class PanelAdminController {



    @FXML
    private Button btnCrearEmpleado;

    @FXML
    private Button btnEditar;

    @FXML
    private Button btnEditarEmpleado;





    @FXML
    private TableColumn<Usuario, Void> colElimianrEmpleado;


    @FXML
    private TableColumn<Usuario, String> colFCreacionEmpleado;

    @FXML
    private TableColumn<Usuario, Integer> colIdEmpleado;


    @FXML
    private TableColumn<Usuario, Void> colLeerEmpleado;


    @FXML
    private TableColumn<Usuario, Usuario> colNombreUsuarioEmpleado;

    @FXML
    private TableColumn<Usuario, String> colRolEmpleado;


    //-------- TABLA CLIENTE INSTANCIAS ----
    @FXML
    private TableView<ClienteDTO> tblClientes;
    @FXML
    private TableColumn<ClienteDTO, String> colNombreUsuarioCliente;
    @FXML
    private TableColumn<ClienteDTO, String> colNombreCliente;
    @FXML
    private TableColumn<ClienteDTO, String> colApellidoCliente;
    @FXML
    private TableColumn<ClienteDTO, Integer> colEdadCliente;
    @FXML
    private TableColumn<ClienteDTO, String> colTelefonoCliente;
    @FXML
    private TableColumn<ClienteDTO, String> colCorreoCliente;
    @FXML
    private TableColumn<ClienteDTO, String> colDireccionCliente;
    @FXML
    private TableColumn<ClienteDTO, Void> colLeerCliente;
    @FXML
    private TableColumn<ClienteDTO, Void> colEliminarCliente;





    @FXML
    private Label lblBienvenida;



    @FXML
    private Spinner<Integer> spEdadCliente;


    @FXML
    private TableView<Usuario> tblEmpleados;

    @FXML
    private TextField txtApellidoCliente;

    @FXML
    private PasswordField txtClaveEmpleado;

    @FXML
    private PasswordField txtClaveEmpleadoConfimacion;

    @FXML
    private TextField txtCorreCliente;

    @FXML
    private TextField txtDireccionCliente;

    @FXML
    private TextField txtNombreCliente;

    @FXML
    private TextField txtNombreUsuarioCliente;

    @FXML
    private TextField txtNombreUsuarioEmpleado;

    @FXML
    private TextField txtTelefonoCliente;

    private final ObservableList<ClienteDTO> listaClientes = FXCollections.observableArrayList();



    public void initialize(){
        lblBienvenida.setText(String.format("Bienvenido : %s", Sesion.getUsuarioActual().getNombreUsuario()));
        InicializarTablaClientes();
        tblClientes.setItems(listaClientes);

        InicializarTablaEmpleados();
        InicialzarEdades();
    }

    //configuracion para la tabla de clientes----------------------------------------------------
    private void InicializarTablaClientes(){
        colNombreUsuarioCliente.setCellValueFactory(new PropertyValueFactory<>("nombreUsuario"));
        colNombreCliente.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colApellidoCliente.setCellValueFactory(new PropertyValueFactory<>("apellido"));
        colEdadCliente.setCellValueFactory(new PropertyValueFactory<>("edad"));
        colTelefonoCliente.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colCorreoCliente.setCellValueFactory(new PropertyValueFactory<>("email"));
        colDireccionCliente.setCellValueFactory(new PropertyValueFactory<>("direccion"));

        //columnas de botones
        BotonLeerClientes();
        BotonEliminarClientes();

        cargarClientes();
    }
    private void cargarClientes() {
        ClienteDAO dao = new ClienteDAO();
        List<ClienteDTO> lista = dao.obtenerClientesConUsuario();

        listaClientes.clear();     //limpia sin perder la referencia con la tabla
        listaClientes.addAll(lista);
    }

    private void BotonLeerClientes() {
        colLeerCliente.setCellFactory(col -> new TableCell<>() {

            private final Button btn = new Button("Leer");

            {
                btn.setOnAction(e -> {
                    ClienteDTO cliente = getTableView().getItems().get(getIndex());
                    mostrarDatosClientes(cliente);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
    }
    private void mostrarDatosClientes(ClienteDTO cliente) {
        txtNombreCliente.setText(cliente.getNombre());
        txtApellidoCliente.setText(cliente.getApellido());
        txtCorreCliente.setText(cliente.getEmail());
        txtTelefonoCliente.setText(cliente.getTelefono());
        txtDireccionCliente.setText(cliente.getDireccion());
        spEdadCliente.getValueFactory().setValue(cliente.getEdad());
        txtNombreUsuarioCliente.setText(cliente.getNombreUsuario());
    }

    private void BotonEliminarClientes() {
        colEliminarCliente.setCellFactory(col -> new TableCell<>() {

            private final Button btn = new Button("Eliminar");

            {
                btn.setOnAction(e -> {
                    ClienteDTO cliente = getTableView().getItems().get(getIndex());

                    if (confirmarEliminacion(cliente)) {
                        eliminarCliente(cliente.getUsuarioId());
                        listaClientes.remove(cliente);  //refresca solo este elemento
                        tblClientes.refresh();
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
    }
    private boolean confirmarEliminacion(ClienteDTO cliente) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmacion");
        alert.setHeaderText("¿Eliminar cliente?");
        alert.setContentText("Se eliminara el registro del cliente: " + cliente.getNombre());
        return alert.showAndWait().filter(btn -> btn == ButtonType.OK).isPresent();
    }
    private void eliminarCliente(int usuarioId) {
        ClienteDAO dao = new ClienteDAO();
        dao.eliminarCliente(usuarioId);
        limpiarFormularioCliente();
    }


    //--------------------------------------------------------------------------------------------------------
    //configuracion para la tabla de empleados--------------------------------------------------------
    private void InicializarTablaEmpleados(){

    }
    private void InicialzarEdades(){
        spEdadCliente.setValueFactory(new  SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 0));
        spEdadCliente.setEditable(true);//SOLO permitir numeros
        TextFormatter<Integer> formatter = new TextFormatter<>(
                spEdadCliente.getValueFactory().getConverter(),
                spEdadCliente.getValue(),
                change -> {
                    String nuevoTexto = change.getControlNewText();
                    if (nuevoTexto.matches("\\d*")) {
                        return change; // permitir solo numeros
                    }
                    return null; // bloquear letras y simbolos
                }
        );

        spEdadCliente.getEditor().setTextFormatter(formatter);

// sincronizar spinner <-> editor
        formatter.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                spEdadCliente.getValueFactory().setValue(newValue);
            }
        });


    }



    //metodos de los botones

    //por emplear
    public void CrearEmpleado(javafx.event.ActionEvent event) {
    }


    //listo
    @FXML
    public void ActualizarCliente(ActionEvent event) {

        ClienteDTO seleccionado = tblClientes.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            alerta("Alerta", "Deve seleccionar una fila", Alert.AlertType.INFORMATION);
            return;
        }

        int clienteId = seleccionado.getId();

        // Obtener valores del formulario
        String nombre = txtNombreCliente.getText().trim();
        String apellido = txtApellidoCliente.getText().trim();
        String email = txtCorreCliente.getText().trim();
        String telefono = txtTelefonoCliente.getText().trim();
        String direccion = txtDireccionCliente.getText().trim();
        int edad = spEdadCliente.getValue();
        String nombreUsuario = txtNombreUsuarioCliente.getText().trim();

        if (camposVacios(nombre, apellido, email, telefono, direccion, nombreUsuario)) {
            alerta("Informacion", "Debe completar todos los campos", Alert.AlertType.INFORMATION);
            return;
        }

        if (!emailValido(email)) {
            alerta("Correo inválido", "El correo electrónico no es válido.", Alert.AlertType.WARNING);
            return;
        }
        //convertir la edad

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


        ClienteDAO dao = new ClienteDAO();

        // Validar campos únicos
        String error = dao.validarCamposUnicos(email, nombreUsuario, clienteId);

        if (error != null) {
            alerta("Actualizado","Cliente actualizado exitosamente", Alert.AlertType.INFORMATION);

            return;
        }

        // Ejecutar actualización
        boolean ok = dao.actualizarClienteYUsuario(
                clienteId,
                nombre,
                apellido,
                edad,
                telefono,
                email,
                direccion,
                nombreUsuario
        );

        if (ok) {
            alerta("Actualizado","Cliente actualizado exitosamente", Alert.AlertType.INFORMATION);
            cargarClientes();
            limpiarFormularioCliente();
        } else {
            alerta("Error","Error actualizando el cliente.", Alert.AlertType.ERROR);
        }
    }

    //por emplear
    public void ActualizarEmpleado(ActionEvent event) {
    }



    private void alerta(String titulo, String mensaje, Alert.AlertType tipoAlerta){
        Alert alert = new Alert(tipoAlerta);
        alert.setTitle(titulo);
        alert.setContentText(mensaje);
        alert.show();
    }

    private void limpiarFormularioCliente() {
        txtNombreCliente.clear();
        txtApellidoCliente.clear();
        txtCorreCliente.clear();
        txtTelefonoCliente.clear();
        txtDireccionCliente.clear();
        txtNombreUsuarioCliente.clear();
        spEdadCliente.getValueFactory().setValue(0);
    }

    private boolean camposVacios(String... valores) {
        for (String v : valores) {
            if (v == null || v.trim().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private boolean emailValido(String email) {
        return email.contains("@") && email.contains(".");
    }

}
