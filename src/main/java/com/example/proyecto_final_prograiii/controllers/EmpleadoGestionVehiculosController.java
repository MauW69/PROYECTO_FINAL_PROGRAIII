package com.example.proyecto_final_prograiii.controllers;

import com.example.proyecto_final_prograiii.DAO.VehiculosDAO;
import com.example.proyecto_final_prograiii.models.Vehiculo;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class EmpleadoGestionVehiculosController {

    @FXML private TableView<Vehiculo> tblVehiculos;
    @FXML private TableColumn<Vehiculo, Integer> colId;
    @FXML private TableColumn<Vehiculo, String> colModelo;
    @FXML private TableColumn<Vehiculo, String> colPlaca;
    @FXML private TableColumn<Vehiculo, String> colTipo;
    @FXML private TableColumn<Vehiculo, Integer> colYear;
    @FXML private TableColumn<Vehiculo, String> colColor;
    @FXML private TableColumn<Vehiculo, Integer> colKm;
    @FXML private TableColumn<Vehiculo, String> colEstado;
    @FXML private TableColumn<Vehiculo, Void> colEditar;
    @FXML private TableColumn<Vehiculo, Void> colEliminar;
    @FXML private TableColumn<Vehiculo, BigDecimal> colPrecio;

    @FXML private Button btnRefrescar;
    @FXML private Button btnAgregar;
    @FXML private Button btnCerrar;

    private final ObservableList<Vehiculo> lista = FXCollections.observableArrayList();
    private final VehiculosDAO dao = new VehiculosDAO();

    @FXML
    public void initialize() {
        configurarColumnas();
        cargarVehiculos();
    }

    private void configurarColumnas() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colModelo.setCellValueFactory(new PropertyValueFactory<>("modelo"));
        colPlaca.setCellValueFactory(new PropertyValueFactory<>("placa"));
        colTipo.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleStringProperty(String.valueOf(cell.getValue().getTipoVehiculoId()))
        );
        colYear.setCellValueFactory(new PropertyValueFactory<>("year"));
        colColor.setCellValueFactory(new PropertyValueFactory<>("color"));
        colKm.setCellValueFactory(new PropertyValueFactory<>("kilometraje"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precioPorDia"));
        colPrecio.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : String.format("$%.2f", item));
            }
        });

        colEditar.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Editar");
            {
                btn.setOnAction(e -> {
                    Vehiculo v = getTableView().getItems().get(getIndex());
                    abrirDialogEditar(v);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        colEliminar.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Eliminar");

            {
                btn.setOnAction(e -> {
                    Vehiculo v = getTableView().getItems().get(getIndex());
                    eliminarVehiculoConfirm(v);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        tblVehiculos.setItems(lista);
    }

    private void cargarVehiculos() {
        List<Vehiculo> listaDb = dao.obtenerTodosVehiculos();
        lista.clear();
        if (listaDb != null) lista.addAll(listaDb);
    }

    @FXML
    void refrescarOnAction(ActionEvent event) {
        cargarVehiculos();
    }

    @FXML
    void agregarOnAction(ActionEvent event) {
        try {
            javafx.fxml.FXMLLoader loader =
                    new javafx.fxml.FXMLLoader(getClass().getResource("/com/example/proyecto_final_prograiii/crearvehiculo-view.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            Stage stage = new Stage();
            stage.setTitle("Agregar Vehículo");
            stage.setScene(scene);
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.showAndWait();
            cargarVehiculos();
        } catch (Exception ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Error al abrir formulario: " + ex.getMessage()).showAndWait();
        }
    }

    @FXML
    void cerrarOnAction(ActionEvent event) {
        Stage st = (Stage) btnCerrar.getScene().getWindow();
        st.close();
    }

    private void eliminarVehiculoConfirm(Vehiculo v) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Confirmar eliminación");
        a.setHeaderText("Eliminar vehículo id=" + v.getId());
        a.setContentText("¿Seguro que desea eliminar este vehículo?");
        Optional<ButtonType> res = a.showAndWait();

        if (res.isPresent() && res.get() == ButtonType.OK) {
            boolean ok = dao.eliminarVehiculo(v.getId());
            if (ok) {
                new Alert(Alert.AlertType.INFORMATION, "Vehículo eliminado").show();
                cargarVehiculos();
            } else {
                new Alert(Alert.AlertType.ERROR, "Error al eliminar").show();
            }
        }
    }

    private void abrirDialogEditar(Vehiculo v) {
        Dialog<Vehiculo> dialog = new Dialog<>();
        dialog.setTitle("Editar Vehículo id=" + v.getId());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField tfModelo = new TextField(v.getModelo());
        TextField tfPlaca = new TextField(v.getPlaca());
        TextField tfTipo = new TextField(String.valueOf(v.getTipoVehiculoId()));
        TextField tfYear = new TextField(String.valueOf(v.getYear()));
        TextField tfColor = new TextField(v.getColor());
        TextField tfKm = new TextField(String.valueOf(v.getKilometraje()));
        TextField tfPrecio = new TextField(v.getPrecioPorDia() != null ? v.getPrecioPorDia().toPlainString() : "");

        ComboBox<String> cbEstado = new ComboBox<>();
        cbEstado.getItems().addAll("disponible", "mantenimiento", "fuera de servicio");
        cbEstado.setValue(v.getEstado());

        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.addRow(0, new Label("Modelo:"), tfModelo);
        grid.addRow(1, new Label("Placa:"), tfPlaca);
        grid.addRow(2, new Label("Tipo ID:"), tfTipo);
        grid.addRow(3, new Label("Año:"), tfYear);
        grid.addRow(4, new Label("Color:"), tfColor);
        grid.addRow(5, new Label("Kilometraje:"), tfKm);
        grid.addRow(6, new Label("Precio/día:"), tfPrecio);
        grid.addRow(7, new Label("Estado:"), cbEstado);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn != ButtonType.OK) return null;

            try {
                Vehiculo nuevo = new Vehiculo();

                nuevo.setId(v.getId());
                nuevo.setModelo(tfModelo.getText());
                nuevo.setPlaca(tfPlaca.getText());
                nuevo.setTipoVehiculoId(Integer.parseInt(tfTipo.getText()));
                nuevo.setYear(Integer.parseInt(tfYear.getText()));
                nuevo.setColor(tfColor.getText());
                nuevo.setKilometraje(Integer.parseInt(tfKm.getText()));

                BigDecimal precio = tfPrecio.getText().isBlank()
                        ? BigDecimal.ZERO
                        : new BigDecimal(tfPrecio.getText());

                nuevo.setPrecioPorDia(precio);
                nuevo.setEstado(cbEstado.getValue());

                // ⭐⭐⭐ IMPORTANTE: PRESERVAR IMAGEN ⭐⭐⭐
                nuevo.setImagen(v.getImagen());

                return nuevo;

            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, "Datos inválidos").show();
                return null;
            }
        });

        Optional<Vehiculo> res = dialog.showAndWait();

        if (res.isPresent() && res.get() != null) {
            boolean ok = dao.actualizarVehiculo(res.get());
            if (ok) {
                new Alert(Alert.AlertType.INFORMATION, "Vehículo actualizado").show();
                cargarVehiculos();
            } else {
                new Alert(Alert.AlertType.ERROR, "Error al actualizar").show();
            }
        }
    }
}
