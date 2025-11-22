package com.example.proyecto_final_prograiii.controllers;

import com.example.proyecto_final_prograiii.DAO.VehiculosDAO;
import com.example.proyecto_final_prograiii.models.Vehiculo;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CrearVehiculoController {

    @FXML private TextField tfTipoId;
    @FXML private TextField tfModelo;
    @FXML private TextField tfPlaca;
    @FXML private TextField tfYear;
    @FXML private TextField tfColor;
    @FXML private TextField tfKm;
    @FXML private ComboBox<String> cbEstado;
    @FXML private TextField tfPrecio;
    @FXML private Button btnAceptar;
    @FXML private Button btnCancelar;

    private final VehiculosDAO dao = new VehiculosDAO();

    @FXML
    public void initialize() {
        cbEstado.getItems().addAll("disponible", "mantenimiento", "fuera de servicio");
        cbEstado.setValue("disponible");
    }

    @FXML
    public void onAceptar() {
        try {
            String tipoStr = tfTipoId.getText().trim();
            String modelo = tfModelo.getText().trim();
            String placa = tfPlaca.getText().trim();
            String yearStr = tfYear.getText().trim();
            String color = tfColor.getText().trim();
            String kmStr = tfKm.getText().trim();
            String estado = cbEstado.getValue();
            String precioStr = tfPrecio.getText().trim();

            if (modelo.isEmpty() || placa.isEmpty()) {
                alerta("Modelo y placa son obligatorios");
                return;
            }

            int tipoId = tipoStr.isEmpty() ? 0 : Integer.parseInt(tipoStr);
            int year = yearStr.isEmpty() ? 0 : Integer.parseInt(yearStr);
            int km = kmStr.isEmpty() ? 0 : Integer.parseInt(kmStr);

            BigDecimal precio = null;
            if (!precioStr.isEmpty()) {
                try {
                    precio = new BigDecimal(precioStr);
                    if (precio.compareTo(BigDecimal.ZERO) < 0) {
                        alerta("Precio debe ser mayor o igual a 0");
                        return;
                    }
                } catch (NumberFormatException ex) {
                    alerta("Precio invalido");
                    return;
                }
            }

            Vehiculo v = new Vehiculo();
            v.setTipoVehiculoId(tipoId);
            v.setModelo(modelo);
            v.setPlaca(placa);
            v.setYear(year);
            v.setColor(color);
            v.setKilometraje(km);
            v.setEstado(estado == null ? "disponible" : estado);
            v.setFechaCreacion(LocalDateTime.now());
            v.setPrecioPorDia(precio);

            boolean ok = dao.crearVehiculo(v);
            if (ok) {
                new Alert(Alert.AlertType.INFORMATION, "Vehiculo creado").showAndWait();
                cerrarVentana();
            } else {
                alerta("No se pudo crear vehiculo");
            }
        } catch (NumberFormatException nfe) {
            alerta("Asegurate de que los campos numericos son validos");
        } catch (Exception ex) {
            ex.printStackTrace();
            alerta("Error: " + ex.getMessage());
        }
    }

    @FXML
    public void onCancelar() {
        cerrarVentana();
    }

    private void cerrarVentana() {
        Stage st = (Stage) btnCancelar.getScene().getWindow();
        st.close();
    }

    private void alerta(String msg) {
        new Alert(Alert.AlertType.WARNING, msg).showAndWait();
    }
}
