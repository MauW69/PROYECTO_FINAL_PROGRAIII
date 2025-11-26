package com.example.proyecto_final_prograiii.controllers;

import com.example.proyecto_final_prograiii.DAO.AlquilerDAO;
import com.example.proyecto_final_prograiii.DAO.PagoDAO;
import com.example.proyecto_final_prograiii.DAO.VehiculosDAO;
import com.example.proyecto_final_prograiii.DTO.AlquilerHistorialDTO;
import com.example.proyecto_final_prograiii.DTO.AlquilerSolicitudDTO;
import com.example.proyecto_final_prograiii.models.Alquiler;
import com.example.proyecto_final_prograiii.models.Usuario;
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
import java.util.Optional;

import static com.example.proyecto_final_prograiii.controllers.LoginController.alerta;

public class PanelEmpleadoController {



    @FXML
    private Button agregarBtn;

    @FXML
    private Button btnCerrarSesion;

    @FXML
    private Button btnGestionar; // nuevo

    @FXML
    private Label lblBienvenida;



    //===tabla de SOLICITUDES REALIZADAS POR EL CLIENTE===
    @FXML
    private TableView<AlquilerSolicitudDTO> tblSolicitudes;

    @FXML
    private TableColumn<AlquilerSolicitudDTO, Void> colCancelarSolicitud;
    @FXML
    private TableColumn<AlquilerSolicitudDTO, String> colClienteSolicitud;
    @FXML
    private TableColumn<AlquilerSolicitudDTO, Void> colDetalleSolicitud;
    @FXML
    private TableColumn<AlquilerSolicitudDTO, String> colVehiculoSolicitud;
    @FXML
    private TableColumn<AlquilerSolicitudDTO, BigDecimal> colPrecioSolicitud;
    @FXML
    private TableColumn<AlquilerSolicitudDTO, String> colEstadoSolicitud;
    @FXML
    private TableColumn<AlquilerSolicitudDTO, LocalDate> colFechaInicioSolicitud;


    //===tabla de HISTORIAL DE SOLICITUDES PAGADAS===
    @FXML
    private TableView<Alquiler> tblHistorial;

    @FXML
    private TableColumn<AlquilerSolicitudDTO, Integer> colIdHistorial;
    @FXML
    private TableColumn<AlquilerSolicitudDTO, String> colVehiculoHistorial;
    @FXML
    private TableColumn<AlquilerSolicitudDTO, LocalDate> colFechainicioHistorial;
    @FXML
    private TableColumn<AlquilerSolicitudDTO, LocalDate> colFechafinHistorial;
    @FXML
    private TableColumn<AlquilerSolicitudDTO, BigDecimal> colTotalPagadoHistorial;
    @FXML
    private TableColumn<AlquilerSolicitudDTO, String> colMetodoPagoHistorial;
    @FXML
    private TableColumn<AlquilerSolicitudDTO, String> colEstadoHistorial;


    private final AlquilerDAO alquilerDao = new AlquilerDAO();
    private final PagoDAO pagoDao = new PagoDAO();

    //obserbablesList
    private ObservableList<AlquilerSolicitudDTO> listaSolicitudes = FXCollections.observableArrayList();
    private ObservableList<AlquilerHistorialDTO> listaHistorial = FXCollections.observableArrayList() ;

    //metodos de inicializacion de tablas

    private void InicializarTablaSolicitudes(){
        colClienteSolicitud.setCellValueFactory(new PropertyValueFactory<>("nombreCliente"));
        colVehiculoSolicitud.setCellValueFactory(new PropertyValueFactory<>("nombreVehiculo"));
        colPrecioSolicitud.setCellValueFactory(new PropertyValueFactory<>("precioDiario"));
        colEstadoSolicitud.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colFechaInicioSolicitud.setCellValueFactory(new PropertyValueFactory<>("fechaInicio"));

        BotonCancelar();
        VerDetalle();

    }
    //inicializacion de botones para la tabla de soliitudes
    private void BotonCancelar() {
        colCancelarSolicitud.setCellFactory(col -> new TableCell<>() {
            private final Button btnCacelar = new Button("Cancelar");

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }

                btnCacelar.setOnAction(e -> {
                    AlquilerSolicitudDTO solicitud = getTableView().getItems().get(getIndex());
                    cancelarSolicitud(solicitud);
                });

                setGraphic(btnCacelar);
            }
        });
    }
    private void VerDetalle() {
        colDetalleSolicitud.setCellFactory(col -> new TableCell<>() {

            private final Button btnVer = new Button("Ver");

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                    return;
                }

                btnVer.setOnAction(e -> {
                    AlquilerSolicitudDTO solicitud = getTableView().getItems().get(getIndex());
                    abrirDetalleDesdeSolicitud(solicitud);
                });

                setGraphic(btnVer);
            }
        });
    }
    //metodos para los botones
    private void abrirDetalleDesdeSolicitud(AlquilerSolicitudDTO solicitud) {
        abrirDetalleVehiculo(solicitud.getVehiculoId());
    }
    private void abrirDetalleVehiculo(int id) {
        try {
            URL url = getClass().getResource("/com/example/proyecto_final_prograiii/vehiculos-detalles-view.fxml");
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();

            VehiculosDetallerController ctrl = loader.getController();
            ctrl.cargarVehiculo(id);

            Stage stage = new Stage();
            stage.setTitle("Detalle del vehículo");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(tblSolicitudes.getScene().getWindow());
            stage.showAndWait();
            cargarDatos();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    private void cancelarSolicitud(AlquilerSolicitudDTO solicitud) {

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar cancelación");
        confirm.setHeaderText("¿Deseas cancelar esta solicitud?");
        confirm.setContentText("Vehículo: " + solicitud.getNombreVehiculo());

        Optional<ButtonType> resultado = confirm.showAndWait();

        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {

            boolean exito = alquilerDao.cancelarSolicitud(solicitud.getId());

            if (exito) {
                Alert ok = new Alert(Alert.AlertType.INFORMATION);
                ok.setTitle("Cancelado correctamente");
                ok.setHeaderText(null);
                ok.setContentText("La solicitud ha sido cancelada.");
                ok.showAndWait();

                cargarDatos(); // refrescar la tabla
            } else {
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setTitle("Error");
                error.setHeaderText("No se pudo cancelar");
                error.setContentText("Ocurrió un error durante la cancelación.");
                error.showAndWait();
            }
        }
    }



    private void InicializarTablaHistorial(){


    }

    private void cargarDatos() {

        listaSolicitudes.setAll(alquilerDao.obtenerSolicitudesActivas());
        tblSolicitudes.setItems(listaSolicitudes);

        tblSolicitudes.setItems(listaSolicitudes);
       // tblHistorial.setItems(listaHistorial);
    }



    @FXML
    void agregarOnAction(ActionEvent event) {
        try {
            URL res = getClass().getResource("/com/example/proyecto_final_prograiii/crearvehiculo-view.fxml");
            FXMLLoader fxmlLoader = new FXMLLoader(res);
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/com/example/proyecto_final_prograiii/css/login.css").toExternalForm());
            Stage stage = new Stage();
            stage.setTitle("AGREGAR VEHICULO");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    void gestionarOnAction(ActionEvent event) {
        try {
            URL res = getClass().getResource("/com/example/proyecto_final_prograiii/vehiculos-gestion-view.fxml");
            FXMLLoader loader = new FXMLLoader(res);
            Parent root = loader.load();

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/com/example/proyecto_final_prograiii/css/login.css").toExternalForm());

            Stage stage = new Stage();
            stage.setTitle("GESTIONAR VEHÍCULOS");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    public void initialize() {

        if (Sesion.getUsuarioActual() != null) {
            lblBienvenida.setText("Bienvenido, " + Sesion.getUsuarioActual().getNombreUsuario());
        } else {
            lblBienvenida.setText("Bienvenido");
        }

        InicializarTablaSolicitudes();
        InicializarTablaHistorial();
        cargarDatos();
    }

    @FXML
    void cerrarOnAction(ActionEvent event) {
        //limpiar la sesion
        Sesion.cerrarSesion();
        btnCerrarSesion.getScene().getWindow().hide();
        try {
            FXMLLoader loader = new FXMLLoader( getClass().getResource("/com/example/proyecto_final_prograiii/login-view.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/com/example/proyecto_final_prograiii/css/login.css").toExternalForm());
            Stage stage = (Stage) btnCerrarSesion.getScene().getWindow();
            stage.setTitle("INICIO DE SESIÓN");
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}
