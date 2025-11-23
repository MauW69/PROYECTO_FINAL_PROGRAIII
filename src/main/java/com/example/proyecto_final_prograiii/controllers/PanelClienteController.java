package com.example.proyecto_final_prograiii.controllers;

import com.example.proyecto_final_prograiii.DAO.VehiculosDAO;
import com.example.proyecto_final_prograiii.config.ConexionDB;
import com.example.proyecto_final_prograiii.models.Usuario;
import com.example.proyecto_final_prograiii.models.Vehiculo;
import com.example.proyecto_final_prograiii.utils.Sesion;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

public class PanelClienteController {

    @FXML private Label lblBienvenida;
    @FXML private HBox cardsContainer; // fx:id en tu FXML
    @FXML private Button btnCerrarSesion;

    private VehiculosDAO vehiculosDAO;

    public void initialize() {
        Usuario usuario = null;
        try {
            usuario = Sesion.getUsuarioActual();
        } catch (Exception ex) {
            // Si Sesion falla, seguimos pero sin usuario
            usuario = null;
        }

        if (usuario != null) {
            lblBienvenida.setText("Bienvenido, " + usuario.getNombreUsuario());
            vehiculosDAO = new VehiculosDAO();
            cargarTarjetasDinamicas();
        } else {
            lblBienvenida.setText("Debe iniciar sesión para poder ver los vehículos");
            if (cardsContainer != null) {
                cardsContainer.getChildren().clear();
                Label msg = new Label("Inicia sesión para ver los vehículos disponibles.");
                msg.setStyle("-fx-font-size:14px; -fx-text-fill:#333333;");
                cardsContainer.getChildren().add(msg);
            }
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
        VBox card = new VBox(8);
        card.setPadding(new Insets(12));
        card.setPrefWidth(260);
        card.setMinHeight(220);
        card.setStyle("-fx-background-color:#ffffff; -fx-border-color:#e6e6e6; -fx-border-radius:10.0; -fx-background-radius:10.0;");

        // --- Imagen placeholder (solo diseño) ---
        ImageView imageView = new ImageView();

        String ruta = v.getImagen();
        if (ruta != null && !ruta.isBlank()) {
            try {
                Image img = new Image("file:" + ruta, 240, 120, true, true);
                imageView.setImage(img);
            } catch (Exception e) {
                System.out.println("Error cargando imagen: " + e.getMessage());
            }
        }

// Estilos y tamaño
        imageView.setFitWidth(240);
        imageView.setFitHeight(120);
        imageView.setPreserveRatio(true);
        imageView.setStyle("-fx-background-color:#f4f4f4; -fx-border-radius:8; -fx-background-radius:8;");

        card.getChildren().add(imageView);

        // --- TÍTULO: modelo — placa (destacado) ---
        String modelo = safe(v.getModelo(), "Modelo N/A");
        String placa  = safe(v.getPlaca(), "Placa N/A");
        Label title = new Label(modelo + " — " + placa);
        title.setStyle("-fx-font-weight:bold; -fx-font-size:15px; -fx-text-fill:#222222;");
        card.getChildren().add(title);

        // --- INFO CORTA: lo esencial que verás en el panel ---
        String tipo = obtenerNombreTipo(v.getTipoVehiculoId());           // consulta a tipos_vehiculo
        BigDecimal precio = obtenerPrecioPorDiaDeVehiculo(v.getId());    // si existe precio en la DB
        String precioTxt = (precio != null) ? String.format("$%.2f", precio) : "N/A";
        String yearText = (v.getYear() == 0) ? "N/A" : String.valueOf(v.getYear());
        String colorText = (v.getColor() == null || v.getColor().isBlank()) ? "N/A" : v.getColor();

        // Mostramos solo 3-4 líneas para que el panel se vea limpio
        Label info = new Label(
                "Tipo: " + tipo + "\n" +
                        "Año: " + yearText + "   Color: " + colorText + "\n" +
                        "Precio/día: " + precioTxt
        );
        info.setWrapText(true);
        info.setStyle("-fx-font-size:12px; -fx-text-fill:#333333;");
        info.setPrefHeight(60);
        info.setMinHeight(Region.USE_PREF_SIZE);
        card.getChildren().add(info);

        // ------------------------------
        // BOTONES: SOLO "VER" en el panel
        // ------------------------------
        javafx.scene.layout.HBox acciones = new javafx.scene.layout.HBox(8);
        javafx.scene.control.Button btnVer = new javafx.scene.control.Button("Ver");

        // ACCIÓN: abrir la vista detalle (usa tu FXML y controlador de detalle)
        btnVer.setOnAction(e -> abrirDetalleVehiculo(v.getId()));

        acciones.getChildren().add(btnVer);
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

    /**
     * Abre la vista de detalle (modal) para el vehículo con el id indicado.
     * Asegúrate de tener `vehiculos-detalle-view.fxml` en resources y el controller VehiculosDetallerController.
     */
    private void abrirDetalleVehiculo(int id) {
        try {
            URL url = getClass().getResource("/com/example/proyecto_final_prograiii/vehiculos-detalles-view.fxml");
            if (url == null) {
                System.err.println("No se encontró /com/example/proyecto_final_prograiii/vehiculos-detalles-view.fxml");
                alerta("Error", "No se encontró la vista de detalle del vehículo (vehiculos-detalles-view.fxml).", Alert.AlertType.ERROR);
                return;
            }

            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();

            // obtener el controlador y pasar el id para que cargue los datos
            com.example.proyecto_final_prograiii.controllers.VehiculosDetallerController ctrl =
                    loader.getController();
            ctrl.cargarVehiculo(id);

            Stage stage = new Stage();
            stage.setTitle("Detalle del vehículo");
            stage.setScene(new Scene(root));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.initOwner(cardsContainer.getScene().getWindow());
            stage.showAndWait();
        } catch (IOException ex) {
            ex.printStackTrace();
            alerta("Error", "No se pudo abrir la ventana de detalle: " + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }


    /**
     * Cerrar sesión (similar al panel admin):
     * - intenta limpiar Sesion por reflexión (cerrarSesion o setUsuarioActual(null))
     * - busca login-view.fxml en rutas probables y lo carga
     */
    @FXML
    public void cerrarSesion(ActionEvent event) {
        // 1) Limpiar Sesion usando reflexión si es posible
        try {
            try {
                java.lang.reflect.Method mCerrar = Sesion.class.getMethod("cerrarSesion");
                if (mCerrar != null) mCerrar.invoke(null);
            } catch (NoSuchMethodException ignore) {
                try {
                    java.lang.reflect.Method mSet = Sesion.class.getMethod("setUsuarioActual", Object.class);
                    if (mSet != null) mSet.invoke(null, new Object[]{null});
                } catch (NoSuchMethodException ignore2) {
                    // no existe método público para limpiar la sesión
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        // 2) Cargar login-view.fxml
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/proyecto_final_prograiii/login-view.fxml"));
        btnCerrarSesion.getScene().getWindow().hide();
        try {
            Parent root =fxmlLoader.load();
            Scene scene = new Scene(root, 589, 400);
            scene.getStylesheets().add(getClass().getResource("/com/example/proyecto_final_prograiii/css/login.css").toExternalForm());
            Stage stage = new Stage();
            stage.setTitle("INICIO DE SESION");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    // alerta helper (reutilizable)
    private void alerta(String titulo, String mensaje, Alert.AlertType tipoAlerta){
        Alert alert = new Alert(tipoAlerta);
        alert.setTitle(titulo);
        alert.setContentText(mensaje);
        alert.show();
    }
}
