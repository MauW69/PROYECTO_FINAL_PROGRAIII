package com.example.proyecto_final_prograiii.controllers;

import com.example.proyecto_final_prograiii.DAO.VehiculosDAO;
import com.example.proyecto_final_prograiii.models.Vehiculo;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class CrearVehiculoController {

    @FXML private ComboBox<String> comboTipo;
    @FXML private TextField txtPlaca;
    @FXML private TextField txtModelo;
    @FXML private TextField txtYear;
    @FXML private TextField txtColor;
    @FXML private TextField txtKilometraje;
    @FXML private ComboBox<String> comboEstado;
    @FXML private Button btnGuardar;

    private VehiculosDAO vehiculosDAO = new VehiculosDAO();

    @FXML
    public void initialize() {

        // ---- Opciones del tipo de vehículo ----
        comboTipo.getItems().addAll(
                "SEDAN",
                "COMPACTO",
                "PICKUP",
                "MICROBUS",
                "CAMIONETA",
                "DEPORTIVO"
        );

        comboTipo.setValue("SEDAN");

        // ---- Estados de vehículo ----
        comboEstado.getItems().addAll("NUEVO","USADO");
        comboEstado.setValue("NUEVO");
    }

    @FXML
    void guardarVehiculo() {

        // Validaciones
        if (comboTipo.getValue() == null ||
                txtPlaca.getText().trim().isEmpty() ||
                txtModelo.getText().trim().isEmpty() ||
                txtYear.getText().trim().isEmpty()) {

            mostrarAlerta("Campos obligatorios", "Debes llenar todos los campos requeridos.", Alert.AlertType.WARNING);
            return;
        }

        try {
            Vehiculo v = new Vehiculo();

            int tipoId = switch (comboTipo.getValue()) {
                case "SEDAN" -> 1;
                case "COMPACTO" -> 2;
                case "PICKUP" -> 3;
                case "MICROBUS" -> 4;
                case "CAMIONETA" -> 5;
                case "DEPORTIVO" -> 6;
                default -> 1;
            };

            v.setTipoVehiculoId(tipoId);
            v.setPlaca(txtPlaca.getText().trim());
            v.setModelo(txtModelo.getText().trim());
            v.setYear(Integer.parseInt(txtYear.getText()));
            v.setColor(txtColor.getText().trim());
            v.setKilometraje(Integer.parseInt(txtKilometraje.getText()));
            v.setEstado(comboEstado.getValue());

            boolean exito = vehiculosDAO.crearVehiculo(v);

            if (exito) {
                mostrarAlerta("Éxito", "Vehículo agregado correctamente.", Alert.AlertType.INFORMATION);
                cerrarVentana();
            } else {
                mostrarAlerta("Error", "No se pudo guardar el vehículo.", Alert.AlertType.ERROR);
            }

        } catch (NumberFormatException e) {
            mostrarAlerta("Formato inválido", "El año y el kilometraje deben ser numéricos.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    void cerrarVentana() {
        Stage stage = (Stage) btnGuardar.getScene().getWindow();
        stage.close();
    }

    private void mostrarAlerta(String titulo, String msg, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
