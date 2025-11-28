package com.example.proyecto_final_prograiii.controllers;

import com.example.proyecto_final_prograiii.DAO.ClienteDAO;
import com.example.proyecto_final_prograiii.DAO.UsuarioDAO;
import com.example.proyecto_final_prograiii.DTO.ClienteDTO;
import com.example.proyecto_final_prograiii.models.Cliente;
import com.example.proyecto_final_prograiii.models.Usuario;
import com.example.proyecto_final_prograiii.utils.ClaveUtil;
import com.example.proyecto_final_prograiii.utils.Sesion;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public class PanelAdminController {

    @FXML
    private Button btnCrearEmpleado;

    @FXML
    private Button btnEditar;

    @FXML
    private Button btnEditarEmpleado;

    @FXML
    private Label lblBienvenida;

    // Botón nuevo de cerrar sesión
    @FXML
    private Button btnCerrarSesion;

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
    private final ObservableList<ClienteDTO> listaClientes = FXCollections.observableArrayList();

    //-------- TABLA EMPLEADOS INSTANCIAS ----
    @FXML
    private TableView<Usuario> tblEmpleados;
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
    private final ObservableList<Usuario> listaEmpleados = FXCollections.observableArrayList();


    @FXML
    private Spinner<Integer> spEdadCliente;


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


    public void initialize(){
        // Protección por si Sesion.getUsuarioActual() es null
        try {
            if (Sesion.getUsuarioActual() != null) {
                lblBienvenida.setText(String.format("Bienvenido : %s", Sesion.getUsuarioActual().getNombreUsuario()));
            } else {
                lblBienvenida.setText("Bienvenido");
            }
        } catch (Exception ex) {
            lblBienvenida.setText("Bienvenido");
        }

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
    private void InicializarTablaEmpleados() {

        // Columnas normales
        colIdEmpleado.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombreUsuarioEmpleado.setCellValueFactory(new PropertyValueFactory<>("nombreUsuario"));
        colFCreacionEmpleado.setCellValueFactory(new PropertyValueFactory<>("fechaCreacion"));

        // Rol fijo en texto
        colRolEmpleado.setCellValueFactory(cell -> new SimpleStringProperty("Empleado"));

        // Boton Leer
        LeerEmpleado();

        // Boton Eliminar
        EliminarEmpleado();

        // Cargar empleados
        cargarEmpleados();

        tblEmpleados.setItems(listaEmpleados);
    }
    private void cargarEmpleados() {
        UsuarioDAO dao = new UsuarioDAO();
        List<Usuario> listaEmpleado = dao.obtenerEmpleados();

        listaEmpleados.clear();
        listaEmpleados.addAll(listaEmpleado);
    }
    private void LeerEmpleado() {
        colLeerEmpleado.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Leer");
            {
                btn.setOnAction(e -> {
                    Usuario emp = getTableView().getItems().get(getIndex());
                    mostrarDatosEmpleado(emp);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
    }
    private void mostrarDatosEmpleado(Usuario emp) {
        txtNombreUsuarioEmpleado.setText(emp.getNombreUsuario());
        txtClaveEmpleado.clear();
        txtClaveEmpleadoConfimacion.clear();
    }
    private void EliminarEmpleado() {
        colElimianrEmpleado.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Eliminar");
            {
                btn.setOnAction(e -> {
                    Usuario emp = getTableView().getItems().get(getIndex());
                    if (confirmarEliminarEmpleado(emp)) {
                        eliminarEmpleado(emp.getId());
                        listaEmpleados.remove(emp);
                        tblEmpleados.refresh();
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
    private boolean confirmarEliminarEmpleado(Usuario emp) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmacion");
        alert.setHeaderText("¿Eliminar empleado?");
        alert.setContentText("Se eliminara el usuario empleado: " + emp.getNombreUsuario());
        return alert.showAndWait().filter(btn -> btn == ButtonType.OK).isPresent();
    }
    private void eliminarEmpleado(int usuarioId) {
        UsuarioDAO dao = new UsuarioDAO();
        dao.eliminarUsuario(usuarioId);
    }

    //botones para clientes
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


    //botones para Empleados
    //por emplear
    @FXML
    public void CrearEmpleado(ActionEvent event) {

        String nombreUsuario = txtNombreUsuarioEmpleado.getText().trim();
        String clave = txtClaveEmpleado.getText();
        String claveConfirmada = txtClaveEmpleadoConfimacion.getText();

        // campos vacios
        if (camposVacios(nombreUsuario, clave, claveConfirmada)) {
            alerta("Alerta", "Complete todos los campos", Alert.AlertType.INFORMATION);
            return;
        }

        // validar nombre de usuario unico
        UsuarioDAO usuarioDAO = new UsuarioDAO();
        String errorValidacion = usuarioDAO.validarNombreUsuarioUnico(nombreUsuario, -1); // -1 porque es creación
        if (errorValidacion != null) {
            alerta("Error", errorValidacion, Alert.AlertType.WARNING);
            return;
        }

        // validar contraseñas
        if (!clave.equals(claveConfirmada)) {
            alerta("Error", "Las contraseñas no coinciden", Alert.AlertType.WARNING);
            return;
        }
        if (clave.length() < 8) {
            alerta("Error", "La contraseña debe tener al menos 8 caracteres", Alert.AlertType.WARNING);
            return;
        }

        //Hashear contraseña
        String claveHash = ClaveUtil.hashClave(clave);

        // crear empleado en BD
        boolean ok = usuarioDAO.crearEmpleado(nombreUsuario, claveHash);

        if (ok) {
            alerta("Exito", "Empleado creado correctamente", Alert.AlertType.INFORMATION);
            cargarEmpleados();

            // Limpiar campos del formulario
            txtNombreUsuarioEmpleado.clear();
            txtClaveEmpleado.clear();
            txtClaveEmpleadoConfimacion.clear();
        } else {
            alerta("Error", "No se pudo crear el empleado", Alert.AlertType.ERROR);
        }
    }

    public void ActualizarEmpleado(ActionEvent event) {
        Usuario seleccionado = tblEmpleados.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            alerta("Alerta", "Debe seleccionar un empleado", Alert.AlertType.INFORMATION);
            return;
        }

        int usuarioId = seleccionado.getId();
        String nombreUsuario = txtNombreUsuarioEmpleado.getText().trim();
        String clave = txtClaveEmpleado.getText();           // campo visible en UI (vacio por defecto)
        String claveConfirmada = txtClaveEmpleadoConfimacion.getText();

        // campo vacio
        if (camposVacios(nombreUsuario)) {
            alerta("Alerta", "No deje vacio el nombre de usuario", Alert.AlertType.INFORMATION);
            return;
        }

        // Validar nombre de usuario unico (permite mantener el mismo nombre si no se cambia)
        UsuarioDAO usuarioDAO = new UsuarioDAO();
        String errorValidacion = usuarioDAO.validarNombreUsuarioUnico(nombreUsuario, usuarioId);
        if (errorValidacion != null) {
            alerta("Error", errorValidacion, Alert.AlertType.WARNING);
            return;
        }

        // si se cambia clave se valida
        String claveHashActualizada = null;
        if (!clave.isEmpty() || !claveConfirmada.isEmpty()) {
            if (!clave.equals(claveConfirmada)) {
                alerta("Error", "Las contraseñas no coinciden", Alert.AlertType.ERROR);
                return;
            }
            if (clave.length() < 8) {
                alerta("advertencia", "La contraseña debe tener al menos 8 caracteres", Alert.AlertType.WARNING);
                return;
            }
            // se hashea la nueva clave
            claveHashActualizada = ClaveUtil.hashClave(clave);
        }

        // Ejecutar actualizacion
        boolean ok = usuarioDAO.actualizarEmpleado(usuarioId, nombreUsuario, claveHashActualizada);
        if (ok) {
            alerta("Exito", "Empleado actualizado", Alert.AlertType.INFORMATION);
            cargarEmpleados();
            // limpiar campos
            txtNombreUsuarioEmpleado.clear();
            txtClaveEmpleado.clear();
            txtClaveEmpleadoConfimacion.clear();
        } else {
            alerta("Error", "No se pudo actualizar el empleado", Alert.AlertType.ERROR);
        }
    }

    //metodos extra
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

    /**
     * Método para cerrar sesión:
     * - Intenta limpiar la sesión (llamando Sesion.cerrarSesion() o Sesion.setUsuarioActual(null) si existe)
     * - Carga login-view.fxml y lo muestra en la misma Stage
     */
    @FXML
    public void cerrarSesion(ActionEvent event) {
        // Intentar limpiar la sesión de forma flexible (reflexión para no depender exactamente del nombre)
        try {
            try {
                // Primero, si existe un método Sesion.cerrarSesion(), invocarlo
                java.lang.reflect.Method mCerrar = Sesion.class.getMethod("cerrarSesion");
                if (mCerrar != null) {
                    mCerrar.invoke(null);
                }
            } catch (NoSuchMethodException ignore) {
                // Si no existe, intentar setUsuarioActual(null) (si existe)
                try {
                    java.lang.reflect.Method mSet = Sesion.class.getMethod("setUsuarioActual", Object.class);
                    if (mSet != null) {
                        mSet.invoke(null, new Object[]{null});
                    }
                } catch (NoSuchMethodException ignore2) {
                    // No hay método público para limpiar la sesión: continuamos igualmente a la pantalla de login
                }
            }
        } catch (Exception ex) {
            // No dejar que un error de limpieza de sesión impida el cierre; lo registramos en consola
            ex.printStackTrace();
        }

        // Rutas probadas para login-view.fxml (según la estructura que mostraste)
        String[] posiblesRutas = {
                "/com/example/proyecto_final_prograiii/login-view.fxml",
                "/com/example/proyecto_final_prograiii/views/login-view.fxml",
                "/login-view.fxml",
                "/com/example/proyecto_final_prograiii/login-view.fxml", // repetida intencionalmente por claridad
                "login-view.fxml"
        };

        URL fxmlUrl = null;
        String rutaEncontrada = null;
        for (String r : posiblesRutas) {
            fxmlUrl = getClass().getResource(r);
            if (fxmlUrl != null) {
                rutaEncontrada = r;
                System.out.println("login-view.fxml encontrado en: " + r + " -> " + fxmlUrl);
                break;
            }
        }

        if (fxmlUrl == null) {
            String msg = "No se pudo encontrar login-view.fxml en las rutas probadas.\nRutas probadas:\n";
            for (String r : posiblesRutas) msg += "  - " + r + "\n";
            msg += "\nColoca login-view.fxml dentro de src/main/resources/com/example/proyecto_final_prograiii/ o ajusta la ruta en el código.";
            alerta("Error al cerrar sesión", msg, Alert.AlertType.ERROR);
            System.err.println(msg);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/com/example/proyecto_final_prograiii/css/login.css").toExternalForm());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Login");
            stage.show();
        } catch (IOException e) {
            alerta("Error", "No se pudo volver a la pantalla de login: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

}
