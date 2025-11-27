package com.example.proyecto_final_prograiii.controllers;

import com.example.proyecto_final_prograiii.DAO.AlquilerDAO;
import com.example.proyecto_final_prograiii.DTO.AlquilerHistorialDTO;
import com.example.proyecto_final_prograiii.DTO.ClienteEstadisticaDTO;
import com.example.proyecto_final_prograiii.DTO.VehiculoEstadisticaDTO;
import com.example.proyecto_final_prograiii.utils.Sesion;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.application.Platform;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;

/**
 * PanelEmpleadoController — muestra solicitudes y el historial.
 * Ajustado para llamar correctamente al controller de detalle (cast).
 */
public class PanelEmpleadoController {

    @FXML private Button agregarBtn;
    @FXML private Button btnCerrarSesion;
    @FXML private Button btnGestionar;
    @FXML private Label lblBienvenida;
    @FXML
    private Button btnCalendario;

    // TAB Historial com tabla Historial
    @FXML private TableView<AlquilerHistorialDTO> tblHistorial;
    @FXML private TableColumn<AlquilerHistorialDTO, Void> colBorrarHistorial;
    @FXML private TableColumn<AlquilerHistorialDTO, Void> colDetalleHistorial;
    @FXML private TableColumn<AlquilerHistorialDTO, String> colClienteHistorial;
    @FXML private TableColumn<AlquilerHistorialDTO, String> colVehiculoHistorial;
    @FXML private TableColumn<AlquilerHistorialDTO, BigDecimal> colPrecioHistorial;
    @FXML private TableColumn<AlquilerHistorialDTO, String> colEstadoHistorial;
    @FXML private TableColumn<AlquilerHistorialDTO, LocalDate> colFechaInicioHistorial;
    @FXML private TableColumn<AlquilerHistorialDTO, LocalDate> colFechaFinHistorial;
    @FXML private TableColumn<AlquilerHistorialDTO, Integer> colDiasTotalesHistorial;
    @FXML private TableView<VehiculoEstadisticaDTO> tblVehiculos;
    @FXML private TableColumn<VehiculoEstadisticaDTO, String> colVehi;
    @FXML private TableColumn<VehiculoEstadisticaDTO, String> colPlaca;
    @FXML private TableColumn<VehiculoEstadisticaDTO, String> colEstado;
    @FXML private TableColumn<VehiculoEstadisticaDTO, Integer> colCanRen;
    @FXML private TableColumn<VehiculoEstadisticaDTO, BigDecimal> colGanancias;
    @FXML private TableView<ClienteEstadisticaDTO> tblClientes;
    @FXML private TableColumn<ClienteEstadisticaDTO, String> colCliente;
    @FXML private TableColumn<ClienteEstadisticaDTO, String> colUsuario;
    @FXML private TableColumn<ClienteEstadisticaDTO, Integer> colRentas;
    @FXML private TableColumn<ClienteEstadisticaDTO, BigDecimal> colImporte;
    @FXML private Tab tabClientes;

    // === DAO ===
    private final AlquilerDAO alquilerDao = new AlquilerDAO();

    // ================================
    //          INICIALIZACI0N
    // ================================
    @FXML
    public void initialize() {

        if (Sesion.getUsuarioActual() != null) {
            lblBienvenida.setText("Bienvenido, " + Sesion.getUsuarioActual().getNombreUsuario());
        }

        // Estas tablas sí pueden cargarse de inmediato
        InicializarTablaHistorial();
        cargarDatos();
        InicializarTablaVehiculos();
        cargarEstadisticasVehiculos();

        tabClientes.setOnSelectionChanged(event -> {
            if (tabClientes.isSelected()) {
                InicializarTablaClientes();
                cargarEstadisticasClientes();
            }
        });
    }
    private void cargarEstadisticasVehiculos() {
        tblVehiculos.setItems(FXCollections.observableArrayList(
                alquilerDao.obtenerEstadisticasVehiculos()
        ));
    }
    private void cargarEstadisticasClientes() {
        tblClientes.setItems(FXCollections.observableArrayList(
                alquilerDao.obtenerEstadisticasClientes()
        ));
    }
    private void InicializarTablaClientes() {
        colCliente.setCellValueFactory(new PropertyValueFactory<>("nombreCompleto"));
        colUsuario.setCellValueFactory(new PropertyValueFactory<>("nombreUsuario"));
        colRentas.setCellValueFactory(new PropertyValueFactory<>("cantidadRentas"));
        colImporte.setCellValueFactory(new PropertyValueFactory<>("importeTotal"));
    }
    private void InicializarTablaVehiculos() {

        colVehi.setCellValueFactory(new PropertyValueFactory<>("nombreVehiculo"));
        colPlaca.setCellValueFactory(new PropertyValueFactory<>("placa"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colCanRen.setCellValueFactory(new PropertyValueFactory<>("cantidadRentas"));
        colGanancias.setCellValueFactory(new PropertyValueFactory<>("ganancias"));
    }
    // ================================
    //      TABLA historial
    // ================================
    private void InicializarTablaHistorial() {

        colVehiculoHistorial.setCellValueFactory(new PropertyValueFactory<>("nombreVehiculo"));
        colClienteHistorial.setCellValueFactory(new PropertyValueFactory<>("cliente")); // si luego agregas cliente
        colFechaInicioHistorial.setCellValueFactory(new PropertyValueFactory<>("fechaInicio"));
        colFechaFinHistorial.setCellValueFactory(new PropertyValueFactory<>("fechaFin"));
        colDiasTotalesHistorial.setCellValueFactory(new PropertyValueFactory<>("diasTotales"));
        colPrecioHistorial.setCellValueFactory(new PropertyValueFactory<>("montoPagado"));
        colEstadoHistorial.setCellValueFactory(new PropertyValueFactory<>("estado"));
        BotonVerDetalles();
        BotonBorrar();
    }
    //metodos de botones
    private void BotonVerDetalles() {
        colDetalleHistorial.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Ver");

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                    return;
                }

                btn.setOnAction(e -> {
                    AlquilerHistorialDTO dto =
                            getTableView().getItems().get(getIndex());

                    // PASAMOS vehiculoId y alquilerId
                    abrirDetalleVehiculo(dto.getVehiculoId(), dto.getAlquilerId());
                });

                setGraphic(btn);
            }
        });
    }


    private void BotonBorrar() {
        colBorrarHistorial.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Borrar");

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                    return;
                }

                btn.setOnAction(e -> {
                    AlquilerHistorialDTO dto = getTableView().getItems().get(getIndex());

                    Alert a = new Alert(Alert.AlertType.CONFIRMATION);
                    a.setHeaderText("Eliminar del historial");
                    a.setContentText("¿Deseas eliminar este registro?");
                    if (a.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

                    eliminarHistorial(dto.getAlquilerId());
                    cargarDatos();
                });

                setGraphic(btn);
            }
        });
    }
    // ================================
    //          ELIMINAR HISTORIAL
    // ================================
    private void eliminarHistorial(int alquilerId) {
        boolean ok = alquilerDao.eliminarAlquilerCompleto(alquilerId);

        if (!ok) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Error");
            alert.setContentText("No se pudo eliminar el registro.");
            alert.showAndWait();
            return;
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText("Eliminado");
        alert.setContentText("Registro eliminado correctamente.");
        alert.showAndWait();
    }

    /**
     * Abre la ventana de detalle para el vehículo.
     * IMPORTANTE: casteamos getController() al tipo concreto para poder llamar cargarVehiculo(id).
     */
    private void abrirDetalleVehiculo(int vehiculoId, int alquilerId) {
        try {
            URL url = getClass().getResource("/com/example/proyecto_final_prograiii/vehiculos-detalles-view.fxml");
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();

            VehiculosDetallerController ctrl = loader.getController();

            ctrl.cargarVehiculo(vehiculoId, alquilerId);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Detalle del Vehículo");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(tblHistorial.getScene().getWindow());
            stage.showAndWait();

            cargarDatos(); // refrescar al cerrar

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }



    // ================================
    //          TABLA HISTORIAL
    // ================================


    // ================================
    //              DATOS
    // ================================
    private void cargarDatos() {
            List<AlquilerHistorialDTO> historial = alquilerDao.obtenerHistorial();
            tblHistorial.setItems(FXCollections.observableArrayList(historial));
    }

    // ================================
    //            BOTONES UI
    // ================================
    @FXML
    void agregarOnAction(ActionEvent event) {
        try {
            URL res = getClass().getResource("/com/example/proyecto_final_prograiii/crearvehiculo-view.fxml");
            Parent root = FXMLLoader.load(res);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Agregar Vehículo");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();



        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void gestionarOnAction(ActionEvent event) {
        try {
            URL res = getClass().getResource("/com/example/proyecto_final_prograiii/vehiculos-gestion-view.fxml");
            Parent root = FXMLLoader.load(res);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Gestión de Vehículos");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            cargarDatos();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //boton para abrir el calendario
    @FXML
    void AbrirCalendario(ActionEvent event) {
        try {
            URL url = getClass().getResource("/com/example/proyecto_final_prograiii/calendario-view.fxml");
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Calendario de Reservas");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            Alert a = new Alert(Alert.AlertType.ERROR, "No se pudo abrir el calendario");
            a.show();
        }
    }

    @FXML
    void cerrarOnAction(ActionEvent event) {

        Sesion.cerrarSesion();
        btnCerrarSesion.getScene().getWindow().hide();

        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/example/proyecto_final_prograiii/login-view.fxml"));
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/com/example/proyecto_final_prograiii/css/login.css").toExternalForm());
            Stage stage = new Stage();
            stage.setTitle("INICIO DE SESION");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
