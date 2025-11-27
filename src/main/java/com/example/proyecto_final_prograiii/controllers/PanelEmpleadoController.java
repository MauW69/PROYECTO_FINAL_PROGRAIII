package com.example.proyecto_final_prograiii.controllers;

import com.example.proyecto_final_prograiii.DAO.AlquilerDAO;
import com.example.proyecto_final_prograiii.DTO.AlquilerHistorialDTO;
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

        InicializarTablaHistorial();
        cargarDatos();
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

    @FXML
    void cerrarOnAction(ActionEvent event) {

        Sesion.cerrarSesion();
        btnCerrarSesion.getScene().getWindow().hide();

        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/example/proyecto_final_prograiii/login-view.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
