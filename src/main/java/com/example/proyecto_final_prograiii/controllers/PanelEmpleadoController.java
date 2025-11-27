package com.example.proyecto_final_prograiii.controllers;

import com.example.proyecto_final_prograiii.DAO.AlquilerDAO;
import com.example.proyecto_final_prograiii.DTO.AlquilerHistorialDTO;
import com.example.proyecto_final_prograiii.DTO.AlquilerSolicitudDTO;
import com.example.proyecto_final_prograiii.utils.Sesion;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * PanelEmpleadoController — muestra solicitudes y el historial.
 * Ajustado para llamar correctamente al controller de detalle (cast).
 */
public class PanelEmpleadoController {

    @FXML private Button agregarBtn;
    @FXML private Button btnCerrarSesion;
    @FXML private Button btnGestionar;
    @FXML private Label lblBienvenida;

    // TAB SOLICITUDES
    @FXML private TableView<AlquilerSolicitudDTO> tblSolicitudes;
    @FXML private TableColumn<AlquilerSolicitudDTO, Void> colCancelarSolicitud;
    @FXML private TableColumn<AlquilerSolicitudDTO, Void> colDetalleSolicitud;
    @FXML private TableColumn<AlquilerSolicitudDTO, String> colClienteSolicitud;
    @FXML private TableColumn<AlquilerSolicitudDTO, String> colVehiculoSolicitud;
    @FXML private TableColumn<AlquilerSolicitudDTO, BigDecimal> colPrecioSolicitud;
    @FXML private TableColumn<AlquilerSolicitudDTO, String> colEstadoSolicitud;
    @FXML private TableColumn<AlquilerSolicitudDTO, LocalDate> colFechaInicioSolicitud;

    // TAB HISTORIAL
    @FXML private TableView<AlquilerHistorialDTO> tblHistorial;
    @FXML private TableColumn<AlquilerHistorialDTO, Integer> colIdHistorial;
    @FXML private TableColumn<AlquilerHistorialDTO, String> colVehiculoHistorial;
    @FXML private TableColumn<AlquilerHistorialDTO, LocalDate> colFechainicioHistorial;
    @FXML private TableColumn<AlquilerHistorialDTO, LocalDate> colFechafinHistorial;
    @FXML private TableColumn<AlquilerHistorialDTO, BigDecimal> colTotalPagadoHistorial;
    @FXML private TableColumn<AlquilerHistorialDTO, String> colMetodoPagoHistorial;
    @FXML private TableColumn<AlquilerHistorialDTO, String> colEstadoHistorial;

    private final AlquilerDAO alquilerDao = new AlquilerDAO();
    private ObservableList<AlquilerSolicitudDTO> listaSolicitudes = FXCollections.observableArrayList();
    private ObservableList<AlquilerHistorialDTO> listaHistorial = FXCollections.observableArrayList();

    // ================================
    //          INICIALIZACIÓN
    // ================================
    @FXML
    public void initialize() {

        if (Sesion.getUsuarioActual() != null) {
            lblBienvenida.setText("Bienvenido, " + Sesion.getUsuarioActual().getNombreUsuario());
        }

        InicializarTablaSolicitudes();
        InicializarTablaHistorial();
        cargarDatos();
    }

    // ================================
    //      TABLA SOLICITUDES
    // ================================
    private void InicializarTablaSolicitudes() {

        // Usa los nombres de propiedades que definen tus DTO (getNombreCliente, getNombreVehiculo, etc.)
        colClienteSolicitud.setCellValueFactory(new PropertyValueFactory<>("nombreCliente"));
        colVehiculoSolicitud.setCellValueFactory(new PropertyValueFactory<>("nombreVehiculo"));
        colPrecioSolicitud.setCellValueFactory(new PropertyValueFactory<>("precioDiario"));
        colEstadoSolicitud.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colFechaInicioSolicitud.setCellValueFactory(new PropertyValueFactory<>("fechaInicio"));

        agregarBotonCancelar();
        agregarBotonVerDetalle();

        tblSolicitudes.setPlaceholder(new Label("Tabla sin contenido"));
    }

    private void agregarBotonCancelar() {
        colCancelarSolicitud.setCellFactory(col -> new TableCell<>() {
            private final Button btnCancel = new Button("Cancelar");

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) { setGraphic(null); return; }

                btnCancel.setOnAction(e -> cancelarSolicitud(getTableView().getItems().get(getIndex())));
                setGraphic(btnCancel);
            }
        });
    }

    private void agregarBotonVerDetalle() {
        colDetalleSolicitud.setCellFactory(col -> new TableCell<>() {
            private final Button btnVer = new Button("Ver");

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) { setGraphic(null); return; }

                btnVer.setOnAction(e -> {
                    AlquilerSolicitudDTO solicitud = getTableView().getItems().get(getIndex());
                    abrirDetalleVehiculo(solicitud.getVehiculoId());
                });

                setGraphic(btnVer);
            }
        });
    }

    private void cancelarSolicitud(AlquilerSolicitudDTO solicitud) {

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar cancelación");
        confirm.setHeaderText("¿Deseas cancelar esta solicitud?");
        confirm.setContentText("Vehículo: " + solicitud.getNombreVehiculo());

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {

            boolean exito = alquilerDao.cancelarSolicitud(solicitud.getId());

            if (exito) {
                new Alert(Alert.AlertType.INFORMATION, "Solicitud cancelada correctamente.").show();
                cargarDatos();
            } else {
                new Alert(Alert.AlertType.ERROR, "No se pudo cancelar la solicitud.").show();
            }
        }
    }

    /**
     * Abre la ventana de detalle para el vehículo.
     * IMPORTANTE: casteamos getController() al tipo concreto para poder llamar cargarVehiculo(id).
     */
    private void abrirDetalleVehiculo(int id) {
        try {
            URL url = getClass().getResource("/com/example/proyecto_final_prograiii/vehiculos-detalles-view.fxml");
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();

            // CAST explícito para poder llamar cargarVehiculo(...)
            com.example.proyecto_final_prograiii.controllers.VehiculosDetallerController ctrl =
                    (com.example.proyecto_final_prograiii.controllers.VehiculosDetallerController) loader.getController();

            ctrl.cargarVehiculo(id);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Detalle del Vehículo");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(tblSolicitudes.getScene().getWindow());
            stage.showAndWait();

            // refrescar datos al volver
            cargarDatos();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // ================================
    //          TABLA HISTORIAL
    // ================================
    private void InicializarTablaHistorial() {

        colIdHistorial.setCellValueFactory(new PropertyValueFactory<>("id"));
        colVehiculoHistorial.setCellValueFactory(new PropertyValueFactory<>("vehiculo"));
        colFechainicioHistorial.setCellValueFactory(new PropertyValueFactory<>("fechaInicio"));
        colFechafinHistorial.setCellValueFactory(new PropertyValueFactory<>("fechaFin"));

        colTotalPagadoHistorial.setCellValueFactory(new PropertyValueFactory<>("totalPagado"));
        colTotalPagadoHistorial.setCellFactory( col ->
                new TableCell<>() {
                    @Override
                    protected void updateItem(BigDecimal item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(empty || item == null ? "" : "$" + item);
                    }
                });

        colMetodoPagoHistorial.setCellValueFactory(new PropertyValueFactory<>("metodoPago"));
        colEstadoHistorial.setCellValueFactory(new PropertyValueFactory<>("estado"));

        tblHistorial.setPlaceholder(new Label("No hay historial registrado"));
    }

    // ================================
    //              DATOS
    // ================================
    private void cargarDatos() {

        List<AlquilerSolicitudDTO> solicitudes = alquilerDao.obtenerSolicitudesActivas();
        listaSolicitudes.setAll(solicitudes);
        tblSolicitudes.setItems(listaSolicitudes);

        List<AlquilerHistorialDTO> historial = alquilerDao.obtenerHistorial();
        listaHistorial.setAll(historial);
        tblHistorial.setItems(listaHistorial);
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

            cargarDatos();

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
