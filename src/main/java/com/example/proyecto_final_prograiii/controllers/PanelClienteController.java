package com.example.proyecto_final_prograiii.controllers;

import com.example.proyecto_final_prograiii.DAO.VehiculosDAO;
import com.example.proyecto_final_prograiii.config.ConexionDB;
import com.example.proyecto_final_prograiii.models.Usuario;
import com.example.proyecto_final_prograiii.models.Vehiculo;
import com.example.proyecto_final_prograiii.utils.Sesion;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

public class PanelClienteController {

    @FXML private Label lblBienvenida;
    @FXML private HBox cardsContainer; // fx:id en tu FXML

    private VehiculosDAO vehiculosDAO;

    public void initialize() {
        Usuario usuario = Sesion.getUsuarioActual();

        if (usuario != null) {
            lblBienvenida.setText("Bienvenido, " + usuario.getNombreUsuario());
            vehiculosDAO = new VehiculosDAO();
            cargarTarjetasDinamicas();
        } else {
            lblBienvenida.setText("Debe iniciar sesión para poder ver los vehículos");
            cardsContainer.getChildren().clear();
            Label msg = new Label("Inicia sesión para ver los vehículos disponibles.");
            msg.setStyle("-fx-font-size:14px; -fx-text-fill:#333333;");
            cardsContainer.getChildren().add(msg);
        }
    }

    private void cargarTarjetasDinamicas() {
        try {
            List<Vehiculo> lista = vehiculosDAO.listarVehiculos(100);
            System.out.println("[DEBUG] Vehiculos obtenidos: size=" + (lista == null ? "null" : lista.size()));

            cardsContainer.getChildren().clear();

            if (lista == null || lista.isEmpty()) {
                Label lbl = new Label("No hay vehículos disponibles.");
                lbl.setStyle("-fx-font-size:14px; -fx-text-fill:#333333;");
                cardsContainer.getChildren().add(lbl);
                return;
            }

            for (Vehiculo v : lista) {
                System.out.println("[DEBUG] Vehiculo -> " + v);
                VBox card = crearCardParaVehiculo(v);
                cardsContainer.getChildren().add(card);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            cardsContainer.getChildren().clear();
            Label err = new Label("Error al cargar vehículos. Revisa la consola.");
            err.setStyle("-fx-text-fill:#cc0000;");
            cardsContainer.getChildren().add(err);
        }
    }

    private VBox crearCardParaVehiculo(Vehiculo v) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(12));
        card.setPrefWidth(260);
        card.setMinHeight(220); // importante: fuerza altura mínima para que el texto quede visible
        card.setStyle("-fx-background-color:#ffffff; -fx-border-color:#e6e6e6; -fx-border-radius:10.0; -fx-background-radius:10.0;");

        // placeholder de imagen (Region)
        Region imgSpace = new Region();
        imgSpace.setPrefHeight(120);
        imgSpace.setStyle("-fx-background-color:#f4f4f4; -fx-border-radius:8.0; -fx-background-radius:8.0;");
        card.getChildren().add(imgSpace);

        // Título: modelo — placa
        String modelo = safe(v.getModelo(), "Modelo N/A");
        String placa  = safe(v.getPlaca(), "Placa N/A");
        Label title = new Label(modelo + " — " + placa);
        title.setStyle("-fx-font-weight:bold; -fx-font-size:15px; -fx-text-fill:#222222;");
        card.getChildren().add(title);

        // Obtener tipo y precio
        String tipo = obtenerNombreTipo(v.getTipoVehiculoId());
        BigDecimal precio = obtenerPrecioPorDiaDeVehiculo(v.getId());
        String precioTxt = (precio != null) ? String.format("$%.2f", precio) : "N/A";

        String yearText = (v.getYear() == 0) ? "N/A" : String.valueOf(v.getYear());
        String colorText = (v.getColor() == null || v.getColor().isBlank()) ? "N/A" : v.getColor();
        String kmText = (v.getKilometraje() == 0) ? "N/A" : String.valueOf(v.getKilometraje());
        String estadoText = (v.getEstado() == null || v.getEstado().isBlank()) ? "N/A" : v.getEstado();

        // Info label — configurado para ser visible y con wrap
        Label info = new Label(
                "Tipo: " + tipo + "\n" +
                        "Año: " + yearText + "\n" +
                        "Color: " + colorText + "\n" +
                        "Kilometraje: " + kmText + " km\n" +
                        "Estado: " + estadoText + "\n" +
                        "Precio/día: " + precioTxt
        );
        info.setWrapText(true);
        info.setStyle("-fx-font-size:12px; -fx-text-fill:#333333;");
        info.setPrefHeight(70);   // asegura espacio visible
        info.setMinHeight(Region.USE_PREF_SIZE);
        card.getChildren().add(info);

        // Botones
        HBox acciones = new HBox(8);
        Button btnVer = new Button("Ver");
        Button btnRentar = new Button("Rentar");
        btnVer.setOnAction(e -> System.out.println("[ACTION] Ver vehículo id=" + v.getId()));
        btnRentar.setOnAction(e -> System.out.println("[ACTION] Rentar vehículo id=" + v.getId()));
        acciones.getChildren().addAll(btnVer, btnRentar);
        card.getChildren().add(acciones);

        return card;
    }

    // helpers
    private String safe(String s, String def) { return (s == null || s.isBlank()) ? def : s; }

    private String obtenerNombreTipo(int tipoId) {
        if (tipoId <= 0) return "N/A";
        String sql = "SELECT nombre FROM tipos_vehiculo WHERE id = ?";
        try (Connection cn = ConexionDB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, tipoId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return safe(rs.getString("nombre"), "N/A");
            }
        } catch (Exception ex) {
            System.err.println("[DEBUG] Error obtenerNombreTipo: " + ex.getMessage());
        }
        return "N/A";
    }

    private BigDecimal obtenerPrecioPorDiaDeVehiculo(int vehiculoId) {
        if (vehiculoId <= 0) return null;
        String sql = "SELECT precio_por_dia FROM vehiculos WHERE id = ?";
        try (Connection cn = ConexionDB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, vehiculoId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getBigDecimal("precio_por_dia");
            }
        } catch (Exception ex) {
            System.err.println("[DEBUG] Error obtenerPrecio: " + ex.getMessage());
        }
        return null;
    }
}
