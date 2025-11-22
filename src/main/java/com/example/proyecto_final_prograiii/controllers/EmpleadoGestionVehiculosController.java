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
    @FXML private TableColumn<Vehiculo, BigDecimal> colPrecio; // NUEVO

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
        colTipo.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(String.valueOf(cell.getValue().getTipoVehiculoId())));
        colYear.setCellValueFactory(new PropertyValueFactory<>("year"));
        colColor.setCellValueFactory(new PropertyValueFactory<>("color"));
        colKm.setCellValueFactory(new PropertyValueFactory<>("kilometraje"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        // Precio: mostrar formateado
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precioPorDia"));
        colPrecio.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("");
                } else {
                    setText(String.format("$%.2f", item));
                }
            }
        });

        // columna editar (boton)
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

        // columna eliminar (boton)
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
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/example/proyecto_final_prograiii/crearvehiculo-view.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            Stage stage = new Stage();
            stage.setTitle("Agregar Vehículo");
            stage.setScene(scene);
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.showAndWait();
            // luego refrescar tabla
            cargarVehiculos();
        } catch (Exception ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Error al abrir formulario de crear: " + ex.getMessage()).showAndWait();
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
                new Alert(Alert.AlertType.ERROR, "Error al eliminar vehículo").show();
            }
        }
    }

    private void abrirDialogEditar(Vehiculo v) {
        Dialog<Vehiculo> dialog = new Dialog<>();
        dialog.setTitle("Editar Vehículo id=" + v.getId());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // --- CAMPOS ---
        Label lblId = new Label(String.valueOf(v.getId()));
        TextField tfModelo = new TextField(v.getModelo());
        TextField tfPlaca  = new TextField(v.getPlaca());
        TextField tfTipo   = new TextField(String.valueOf(v.getTipoVehiculoId()));
        TextField tfYear   = new TextField(String.valueOf(v.getYear()));
        TextField tfColor  = new TextField(v.getColor());
        TextField tfKm     = new TextField(String.valueOf(v.getKilometraje()));

        // NUEVO: campo precio por dia
        TextField tfPrecio = new TextField(v.getPrecioPorDia() != null ? v.getPrecioPorDia().toPlainString() : "");

        // Estado: combo con opciones fijas
        ObservableList<String> opcionesEstado = FXCollections.observableArrayList(
                "mantenimiento", "disponible", "fuera de servicio"
        );
        ComboBox<String> cbEstado = new ComboBox<>(opcionesEstado);
        cbEstado.setEditable(false);
        String estadoActual = v.getEstado() == null ? "" : v.getEstado().trim().toLowerCase();
        if (opcionesEstado.contains(estadoActual)) {
            cbEstado.setValue(estadoActual);
        } else {
            if (estadoActual.contains("manten") ) cbEstado.setValue("mantenimiento");
            else if (estadoActual.contains("disp")) cbEstado.setValue("disponible");
            else if (estadoActual.contains("fuera") || estadoActual.contains("fuera de")) cbEstado.setValue("fuera de servicio");
            else cbEstado.setValue("disponible");
        }

        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        int row = 0;
        grid.addRow(row++, new Label("ID:"), lblId);
        grid.addRow(row++, new Label("Modelo:"), tfModelo);
        grid.addRow(row++, new Label("Placa:"), tfPlaca);
        grid.addRow(row++, new Label("Tipo ID:"), tfTipo);
        grid.addRow(row++, new Label("Año:"), tfYear);
        grid.addRow(row++, new Label("Color:"), tfColor);
        grid.addRow(row++, new Label("Kilometraje:"), tfKm);
        grid.addRow(row++, new Label("Precio por dia:"), tfPrecio); // fila nueva
        grid.addRow(row++, new Label("Estado:"), cbEstado);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    Vehiculo nuevo = new Vehiculo();
                    nuevo.setId(v.getId());

                    String modelo = tfModelo.getText().trim();
                    String placa  = tfPlaca.getText().trim();
                    String tipoStr = tfTipo.getText().trim();
                    String yearStr = tfYear.getText().trim();
                    String color = tfColor.getText().trim();
                    String kmStr = tfKm.getText().trim();
                    String precioStr = tfPrecio.getText().trim();
                    String estadoSel = cbEstado.getValue();

                    if (modelo.isEmpty() || placa.isEmpty()) {
                        throw new IllegalArgumentException("Modelo y placa no pueden estar vacíos.");
                    }

                    int tipoId = tipoStr.isEmpty() ? 0 : Integer.parseInt(tipoStr);
                    int year = yearStr.isEmpty() ? 0 : Integer.parseInt(yearStr);
                    int km = kmStr.isEmpty() ? 0 : Integer.parseInt(kmStr);

                    BigDecimal precio = null;
                    if (!precioStr.isEmpty()) {
                        try {
                            precio = new BigDecimal(precioStr);
                            if (precio.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("El precio debe ser >= 0");
                        } catch (NumberFormatException nfe) {
                            throw new IllegalArgumentException("Precio invalido");
                        }
                    }

                    nuevo.setModelo(modelo);
                    nuevo.setPlaca(placa);
                    nuevo.setTipoVehiculoId(tipoId);
                    nuevo.setYear(year);
                    nuevo.setColor(color);
                    nuevo.setKilometraje(km);
                    nuevo.setPrecioPorDia(precio);
                    nuevo.setEstado(estadoSel == null ? "disponible" : estadoSel);

                    return nuevo;
                } catch (NumberFormatException nfe) {
                    nfe.printStackTrace();
                    new Alert(Alert.AlertType.ERROR, "Asegurate de que Tipo, Año y Kilometraje son numeros validos.").showAndWait();
                    return null;
                } catch (IllegalArgumentException iae) {
                    iae.printStackTrace();
                    new Alert(Alert.AlertType.ERROR, iae.getMessage()).showAndWait();
                    return null;
                } catch (Exception ex) {
                    ex.printStackTrace();
                    new Alert(Alert.AlertType.ERROR, "Error: " + ex.getMessage()).showAndWait();
                    return null;
                }
            }
            return null;
        });

        Optional<Vehiculo> res = dialog.showAndWait();
        if (res.isPresent()) {
            Vehiculo actualizado = res.get();
            if (actualizado != null) {
                boolean ok = dao.actualizarVehiculo(actualizado);
                if (ok) {
                    new Alert(Alert.AlertType.INFORMATION, "Vehiculo actualizado").show();
                    cargarVehiculos();
                } else {
                    new Alert(Alert.AlertType.ERROR, "Error al actualizar vehiculo").show();
                }
            }
        }
    }
}
