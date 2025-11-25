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
            lblBienvenida.setText("Debe iniciar sesión para poder reservar los vehículos");
            vehiculosDAO = new VehiculosDAO();
            cargarTarjetasDinamicas();
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
        card.getStyleClass().add("card");
        card.setPadding(new Insets(8));

        ImageView imageView = new ImageView();
        imageView.getStyleClass().add("card-image");
        imageView.setFitWidth(240);
        imageView.setFitHeight(130);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);

        // DEBUG inicial
        System.out.println("============================================");
        System.out.println("[DEBUG] Vehículo ID: " + v.getId());
        String ruta = v.getImagen();
        System.out.println("[DEBUG] Ruta guardada en BD: " + ruta);

        boolean imageSet = false;

        // Intentar cargar imagen desde ruta absoluta guardada en BD
        if (ruta != null && !ruta.isBlank()) {
            try {
                java.io.File f = new java.io.File(ruta);
                System.out.println("[DEBUG] File.exists(): " + f.exists());
                System.out.println("[DEBUG] File absolute path: " + f.getAbsolutePath());
                System.out.println("[DEBUG] URI usada para cargar imagen: " + f.toURI());

                Image img = new Image(f.toURI().toString(), 240, 130, true, true);
                if (!img.isError()) {
                    imageView.setImage(img);
                    imageSet = true;
                    System.out.println("[DEBUG] Imagen cargada correctamente desde BD.");
                } else {
                    System.out.println("[DEBUG] Error interno cargando imagen desde BD:");
                    if (img.getException() != null) img.getException().printStackTrace();
                }
            } catch (Exception e) {
                System.out.println("[DEBUG] Excepción al cargar imagen desde BD:");
                e.printStackTrace();
            }
        } else {
            System.out.println("[DEBUG] Ruta vacía o null, no se intentó cargar imagen desde BD.");
        }

        // Si no hay imagen válida, intentar cargar placeholder desde resources (packaged)
        if (!imageSet) {
            try {
                // 1) Intentar resource empaquetado (recomendado): coloca placeholder.png en /resources/com/example/proyecto_final_prograiii/images/
                java.net.URL res = getClass().getResource("/com/example/proyecto_final_prograiii/images/placeholder.png");
                if (res != null) {
                    Image placeholder = new Image(res.toString(), 240, 130, true, true);
                    imageView.setImage(placeholder);
                    imageSet = true;
                    System.out.println("[DEBUG] Placeholder cargado desde resources.");
                } else {
                    System.out.println("[DEBUG] Placeholder en resources NO encontrado.");
                }
            } catch (Exception ex) {
                System.out.println("[DEBUG] Error cargando placeholder desde resources:");
                ex.printStackTrace();
            }
        }

        // 2) Si tampoco hubo resource, usar la imagen temporal que tenemos en entorno de testing
        if (!imageSet) {
            try {
                String placeholderPath = "/mnt/data/532069af-85e9-44f8-9743-062628545c4e.png"; // path de prueba
                java.io.File pf = new java.io.File(placeholderPath);
                if (pf.exists()) {
                    Image placeholder = new Image(pf.toURI().toString(), 240, 130, true, true);
                    imageView.setImage(placeholder);
                    imageSet = true;
                    System.out.println("[DEBUG] Placeholder cargado desde: " + placeholderPath);
                } else {
                    System.out.println("[DEBUG] Placeholder de prueba no encontrado en: " + placeholderPath);
                    // si quieres, puedes dejar un Region gris en su lugar (ya está el background en FXML)
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        // Título
        Label title = new Label(safe(v.getModelo(), "Modelo N/A") + " — " + safe(v.getPlaca(), "Placa N/A"));
        title.getStyleClass().add("card-title");

        // Información
        String tipo = obtenerNombreTipo(v.getTipoVehiculoId());
        java.math.BigDecimal precio = obtenerPrecioPorDiaDeVehiculo(v.getId());
        String precioTxt = (precio != null) ? String.format("$%.2f", precio) : "N/A";

        Label info = new Label(
                "Tipo: " + tipo + "\n" +
                        "Año: " + (v.getYear() == 0 ? "N/A" : v.getYear()) + "\n" +
                        "Color: " + (v.getColor() == null ? "N/A" : v.getColor()) + "\n" +
                        "Precio/día: " + precioTxt
        );
        info.setWrapText(true);
        info.getStyleClass().add("card-info");

        Button btnVer = new Button("Ver");
        btnVer.getStyleClass().add("btn-ver");
        btnVer.setOnAction(e -> abrirDetalleVehiculo(v.getId()));

        HBox acciones = new HBox(btnVer);
        acciones.setSpacing(10);

        card.getChildren().addAll(imageView, title, info, acciones);
        System.out.println("============================================");
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
        Usuario usuario = Sesion.getUsuarioActual();

        if (usuario == null) {
            alerta("Acceso denegado", "Debe iniciar sesión para poder reservar vehículos.", Alert.AlertType.WARNING);
            return;
        }
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
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/com/example/proyecto_final_prograiii/css/DetalleVehiculos.css").toExternalForm());
            Stage stage = new Stage();
            stage.setTitle("Detalle del vehículo");
            stage.setScene(scene);
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.initOwner(cardsContainer.getScene().getWindow());
            stage.showAndWait();
        } catch (IOException ex) {
            ex.printStackTrace();
            alerta("Error", "No se pudo abrir la ventana de detalle: " + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    //agregar metodo para inicializat tableview para mostrar las rentas del clientes



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
