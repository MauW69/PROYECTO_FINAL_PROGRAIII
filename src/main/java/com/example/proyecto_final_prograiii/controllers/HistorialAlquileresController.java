package com.example.proyecto_final_prograiii.controllers;

import com.example.proyecto_final_prograiii.DAO.AlquilerDAO;
import com.example.proyecto_final_prograiii.DTO.AlquilerHistorialDTO;
import com.example.proyecto_final_prograiii.DTO.AlquilerSolicitudDTO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleStringProperty;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller para mostrar:
 * - Tab "Solicitudes/Activos"  -> usa AlquilerSolicitudDTO (obtenerSolicitudesActivas)
 * - Tab "Historial"           -> usa AlquilerHistorialDTO  (obtenerHistorial)
 */
public class HistorialAlquileresController {

    // TAB SOLICITUDES / ACTIVOS
    @FXML private TableView<AlquilerSolicitudDTO> tblActivos;
    @FXML private TableColumn<AlquilerSolicitudDTO, Integer> colAId;
    @FXML private TableColumn<AlquilerSolicitudDTO, String> colAVehiculo;
    @FXML private TableColumn<AlquilerSolicitudDTO, String> colACliente;
    @FXML private TableColumn<AlquilerSolicitudDTO, String> colAFechaInicio;
    @FXML private TableColumn<AlquilerSolicitudDTO, BigDecimal> colAPrecioDia;
    @FXML private TableColumn<AlquilerSolicitudDTO, String> colAEstado;

    // TAB HISTORIAL
    @FXML private TableView<AlquilerHistorialDTO> tblHistorial;
    @FXML private TableColumn<AlquilerHistorialDTO, Integer> colHId;
    @FXML private TableColumn<AlquilerHistorialDTO, String> colHVehiculo;
    @FXML private TableColumn<AlquilerHistorialDTO, String> colHFechaInicio;
    @FXML private TableColumn<AlquilerHistorialDTO, String> colHFechaFin;
    @FXML private TableColumn<AlquilerHistorialDTO, BigDecimal> colHTotalPagado;
    @FXML private TableColumn<AlquilerHistorialDTO, String> colHMetodo;
    @FXML private TableColumn<AlquilerHistorialDTO, String> colHEstado;

    private final AlquilerDAO dao = new AlquilerDAO();
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public void initialize() {
        setupActivosTable();
        setupHistorialTable();
        cargarActivos();
        cargarHistorial();
    }

    // ----------------- TAB ACTIVOS --------------------
    private void setupActivosTable() {
        colAId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colAVehiculo.setCellValueFactory(new PropertyValueFactory<>("vehiculo"));
        colACliente.setCellValueFactory(new PropertyValueFactory<>("cliente"));

        // Aquí calculamos el string y devolvemos una SimpleStringProperty,
        // evitando capturar variables mutables en lambdas.
        colAFechaInicio.setCellValueFactory(cell -> {
            AlquilerSolicitudDTO dto = cell.getValue();
            String formatted = "";
            try {
                if (dto != null && dto.getFechaInicio() != null) {
                    formatted = dto.getFechaInicio().format(fmt);
                }
            } catch (Exception ignored) {}
            return new SimpleStringProperty(formatted);
        });

        // Ajusta el nombre del property string si tu DTO usa otro getter/prop name.
        // Si tu getter se llama getPrecioDiario() cambia "precio_diario" por "precioDiario".
        colAPrecioDia.setCellValueFactory(new PropertyValueFactory<>("precio_diario"));
        colAPrecioDia.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : String.format("$%.2f", item));
            }
        });

        colAEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        tblActivos.setPlaceholder(new Label("Tabla sin contenido"));
    }

    private void cargarActivos() {
        try {
            List<AlquilerSolicitudDTO> list = dao.obtenerSolicitudesActivas();
            ObservableList<AlquilerSolicitudDTO> items = FXCollections.observableArrayList(list);
            tblActivos.setItems(items);
        } catch (Exception ex) {
            ex.printStackTrace();
            tblActivos.setItems(FXCollections.emptyObservableList());
        }
    }

    // ----------------- TAB HISTORIAL --------------------
    private void setupHistorialTable() {
        colHId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colHVehiculo.setCellValueFactory(new PropertyValueFactory<>("vehiculo"));

        colHFechaInicio.setCellValueFactory(cell -> {
            AlquilerHistorialDTO dto = cell.getValue();
            String s = "";
            try {
                if (dto != null && dto.getFechaInicio() != null) {
                    s = dto.getFechaInicio().format(fmt);
                }
            } catch (Exception ignored) {}
            return new SimpleStringProperty(s);
        });

        colHFechaFin.setCellValueFactory(cell -> {
            AlquilerHistorialDTO dto = cell.getValue();
            String s = "";
            try {
                if (dto != null && dto.getFechaFin() != null) {
                    s = dto.getFechaFin().format(fmt);
                }
            } catch (Exception ignored) {}
            return new SimpleStringProperty(s);
        });

        colHTotalPagado.setCellValueFactory(new PropertyValueFactory<>("totalPagado"));
        colHTotalPagado.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : String.format("$%.2f", item));
            }
        });

        colHMetodo.setCellValueFactory(cell -> {
            AlquilerHistorialDTO dto = cell.getValue();
            String m = (dto != null && dto.getMetodoPago() != null) ? dto.getMetodoPago() : "";
            return new SimpleStringProperty(m);
        });

        colHEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        tblHistorial.setPlaceholder(new Label("No hay historial registrado."));
    }

    private void cargarHistorial() {
        try {
            List<AlquilerHistorialDTO> hist = dao.obtenerHistorial();
            ObservableList<AlquilerHistorialDTO> items = FXCollections.observableArrayList(hist);
            tblHistorial.setItems(items);
        } catch (Exception ex) {
            ex.printStackTrace();
            tblHistorial.setItems(FXCollections.emptyObservableList());
        }
    }
}
