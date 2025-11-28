package com.example.proyecto_final_prograiii.controllers;

import com.example.proyecto_final_prograiii.DAO.VehiculosDAO;
import com.example.proyecto_final_prograiii.config.ConexionDB;
import com.example.proyecto_final_prograiii.models.Renta;
import com.example.proyecto_final_prograiii.models.Usuario;
import com.example.proyecto_final_prograiii.models.Vehiculo;
import com.example.proyecto_final_prograiii.utils.Sesion;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.FlowPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.animation.ParallelTransition;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class PanelClienteController {

    @FXML private Label lblBienvenida;
    @FXML private FlowPane cardsContainer;
    @FXML private Button btnCerrarSesion;
    @FXML private Button misAlquileres;            // <-- agregado
    @FXML private TableView<Renta> tblAlquileres;
    @FXML private TableColumn<Renta,String> colModelo;
    @FXML private TableColumn<Renta,String> colPlaca;
    @FXML private TableColumn<Renta,String> inicio;
    @FXML private TableColumn<Renta,String> fin;
    @FXML private TableColumn<Renta,String> total;
    @FXML private TableColumn<Renta, Void> colDetalle; // nueva columna para botón
    @FXML private ScrollPane scroll;
    @FXML private Button btnVolver;

    private VehiculosDAO vehiculosDAO;

    // map que asocia cada objeto Renta mostrado con {vehiculoId, alquilerId}
    private final Map<Renta, int[]> detallesMap = new HashMap<>();

    @FXML
    public void initialize() {
        // inicializa DAO y datos
        vehiculosDAO = new VehiculosDAO();

        // Cargamos UI básica y tarjetas
        Usuario usuario = null;
        try {
            usuario = Sesion.getUsuarioActual();
        } catch (Exception ex) {
            usuario = null;
        }

        if (usuario != null) {
            lblBienvenida.setText("Bienvenido, " + usuario.getNombreUsuario());
        } else {
            lblBienvenida.setText("Bienvenido/a — Vehículos disponibles");
        }

        // configurar tabla (aunque esté oculta por defecto)
        colModelo.setCellValueFactory(new PropertyValueFactory<>("modelo"));
        colPlaca.setCellValueFactory(new PropertyValueFactory<>("placa"));
        inicio.setCellValueFactory(new PropertyValueFactory<>("inicio"));
        fin.setCellValueFactory(new PropertyValueFactory<>("fin"));
        total.setCellValueFactory(new PropertyValueFactory<>("total"));

        // configurar columna de detalles (botón)
        configurarColumnaDetalles();

        // Cargar tarjetas (vehículos)
        cargarTarjetasDinamicas();

        // Actualiza visibilidad y handlers según si hay sesión o no
        actualizarEstadoSesion();
    }

    /**
     * Crea la column "DETALLES" con un botón "Ver" por fila.
     * Usa el mapa detallesMap para obtener vehiculoId y alquilerId asociados al Renta.
     */
    private void configurarColumnaDetalles() {
        colDetalle.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Ver");

            {
                btn.getStyleClass().add("btn-ver"); // usa tu estilo
                btn.setOnAction(e -> {
                    Renta renta = getTableView().getItems().get(getIndex());
                    if (renta == null) return;
                    int[] ids = detallesMap.get(renta);
                    if (ids == null) {
                        // fallback: no tenemos ids, mostrar información básica
                        mostrarDetalleSimple(renta, null, -1);
                    } else {
                        int vehiculoId = ids[0];
                        int alquilerId = ids[1];
                        abrirDetalleAlquiler(vehiculoId, alquilerId, renta);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btn);
                }
            }
        });
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
                // agregar animación de hover suave
                agregarAnimacionHover(card);
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
        card.getStyleClass().add("card-vehiculo"); // usa la clase correcta del CSS
        card.setPadding(new Insets(8));
        card.setAlignment(Pos.TOP_CENTER);

        ImageView imageView = new ImageView();
        imageView.getStyleClass().add("card-image");
        imageView.setFitWidth(200); // ajustado a CSS prefer
        imageView.setFitHeight(120);
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
                File f = new File(ruta);
                Image img = new Image(f.toURI().toString(), 240, 130, true, true);
                if (!img.isError()) {
                    imageView.setImage(img);
                    imageSet = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // placeholder desde resources
        if (!imageSet) {
            try {
                URL res = getClass().getResource("/com/example/proyecto_final_prograiii/images/placeholder.png");
                if (res != null) {
                    Image placeholder = new Image(res.toString(), 240, 130, true, true);
                    imageView.setImage(placeholder);
                    imageSet = true;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        // fallback temporal
        if (!imageSet) {
            try {
                String placeholderPath = "/mnt/data/532069af-85e9-44f8-9743-062628545c4e.png"; // path de prueba
                File pf = new File(placeholderPath);
                if (pf.exists()) {
                    Image placeholder = new Image(pf.toURI().toString(), 240, 130, true, true);
                    imageView.setImage(placeholder);
                    imageSet = true;
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
        BigDecimal precio = obtenerPrecioPorDiaDeVehiculo(v.getId());
        String precioTxt = (precio != null) ? String.format("$%.2f", precio) : "N/A";

        Label info = new Label(
                "Tipo: " + tipo + "\n" +
                        "Año: " + (v.getYear() == 0 ? "N/A" : v.getYear()) + "\n" +
                        "Color: " + (v.getColor() == null ? "N/A" : v.getColor()) + "\n" +
                        "Precio/día: " + precioTxt
        );
        info.setWrapText(true);
        info.getStyleClass().add("card-info");

        // Botón Ver
        Button btnVer = new Button("Ver");
        btnVer.getStyleClass().add("btn-ver");
        btnVer.setOnAction(e -> abrirDetalleVehiculo(v.getId()));

        HBox acciones = new HBox(btnVer);
        acciones.setSpacing(10);
        acciones.setAlignment(Pos.CENTER_LEFT);

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
     * Este método ya existía; lo dejo intacto para uso del botón Ver de las cards.
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

            com.example.proyecto_final_prograiii.controllers.VehiculosDetallerController ctrl =
                    loader.getController();
            // intento de asegurar vista solo info = cliente (llamo método si existe)
            try {
                java.lang.reflect.Method m = ctrl.getClass().getMethod("setModoCliente", boolean.class);
                m.invoke(ctrl, true);
            } catch (NoSuchMethodException ignore) {
                // si no existe, nada; el siguiente try-catch no altera tu controller
            } catch (Exception ex) {
                // ignore invocation problems
            }

            // Intento ocultar nodos típicos por id en el FXML (si no se ocultaron con el método)
            List<String> posiblesIds = Arrays.asList(
                    "#btnReservar", "#btnPagar", "#dateInicio", "#dateFin",
                    "#fechasBox", "#pagosBox", "#actionsBox", "#btnFechas", "#btnPagos"
            );
            for (String fxId : posiblesIds) {
                try {
                    javafx.scene.Node nodo = root.lookup(fxId);
                    if (nodo != null) {
                        nodo.setVisible(false);
                        nodo.setManaged(false);
                    }
                } catch (Exception ignore) {}
            }

            // finalmente llamo al método de carga con parámetros si existe
            try {
                java.lang.reflect.Method cargar = ctrl.getClass().getMethod("cargarVehiculo", int.class, int.class);
                cargar.invoke(ctrl, id, 0);
            } catch (NoSuchMethodException nsme) {
                // si no existe, puede que ctrl tenga otra firma: intentar cargarVehiculo(int)
                try {
                    java.lang.reflect.Method cargar2 = ctrl.getClass().getMethod("cargarVehiculo", int.class);
                    cargar2.invoke(ctrl, id);
                } catch (Exception ignore) {
                    // si tampoco existe, no fatal
                }
            } catch (Exception ex) {
                // ignore invocation errors
            }

            Scene scene = new Scene(root);
            try {
                scene.getStylesheets().add(getClass().getResource("/com/example/proyecto_final_prograiii/css/DetalleVehiculos.css").toExternalForm());
            } catch (Exception ignore) {}
            Stage stage = new Stage();
            stage.setTitle("Detalle del vehículo");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            try {
                stage.initOwner(cardsContainer.getScene().getWindow());
            } catch (Exception ignore) {}
            stage.showAndWait();
        } catch (IOException ex) {
            ex.printStackTrace();
            alerta("Error", "No se pudo abrir la ventana de detalle: " + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }


    private void abrirDetalleAlquiler(int vehiculoId, int alquilerId, Renta renta) {

        try {
            URL url = getClass().getResource("/com/example/proyecto_final_prograiii/vehiculoDetallesLectura.fxml");
            if (url != null) {
                FXMLLoader loader = new FXMLLoader(url);
                Parent root = loader.load();
                Scene scene = new Scene(root);

                // casteamos al controller concreto para llamar al método que ya existe
                com.example.proyecto_final_prograiii.controllers.VehiculosDetallerController ctrl =
                        loader.getController();

                // intenta poner modo cliente si el controller tiene ese método
                boolean modoClienteEstablecido = false;
                try {
                    java.lang.reflect.Method m = ctrl.getClass().getMethod("setModoCliente", boolean.class);
                    m.invoke(ctrl, true);
                    modoClienteEstablecido = true;
                } catch (NoSuchMethodException ignore) {
                    // no existe, seguimos
                } catch (Exception ignore) {}

                // si no hay método, ocultamos nodos por fx:id (fechas, pagos, acciones)
                if (!modoClienteEstablecido) {
                    List<String> posiblesIds = Arrays.asList(
                            "#btnReservar", "#btnPagar", "#dateInicio", "#dateFin",
                            "#fechasBox", "#pagosBox", "#actionsBox", "#btnFechas", "#btnPagos"
                    );
                    for (String fxId : posiblesIds) {
                        try {
                            javafx.scene.Node nodo = root.lookup(fxId);
                            if (nodo != null) {
                                nodo.setVisible(false);
                                nodo.setManaged(false);
                            }
                        } catch (Exception ignore) {}
                    }
                }

                // llamar cargarVehiculo(vehiculoId, alquilerId) si existe
                try {
                    java.lang.reflect.Method cargar = ctrl.getClass().getMethod("cargarVehiculo", int.class, int.class);
                    cargar.invoke(ctrl, vehiculoId, alquilerId);
                } catch (NoSuchMethodException nsme) {
                    try {
                        java.lang.reflect.Method cargar2 = ctrl.getClass().getMethod("cargarVehiculo", int.class);
                        cargar2.invoke(ctrl, vehiculoId);
                    } catch (Exception ignore) {}
                } catch (Exception ignore) {}

                scene.getStylesheets().add(getClass().getResource("/com/example/proyecto_final_prograiii/css/DetalleVehiculos.css").toExternalForm());
                Stage stage = new Stage();
                stage.setTitle("Detalle de la renta");
                stage.setScene(scene);
                stage.initModality(Modality.APPLICATION_MODAL);
                try { stage.initOwner(tblAlquileres.getScene().getWindow()); } catch (Exception ignore) {}
                stage.showAndWait();

                return; // ya mostramos el detalle con el FXML, salimos
            } else {
                System.out.println("[INFO] vehiculos-detalles-view.fxml no encontrado; usar fallback.");
            }
        } catch (Exception ex) {
            System.err.println("[WARN] Error cargando vehiculos-detalles-view.fxml: " + ex.getMessage());
            // seguimos a fallback
        }

        // 2) Fallback seguro (programático) - NO consultamos columna tipo_vehiculo_id para evitar el error
        String modelo = renta != null ? renta.getModelo() : "N/A";
        String placa = renta != null ? renta.getPlaca() : "N/A";
        String fechaInicio = renta != null ? renta.getInicio() : "-";
        String fechaFin = renta != null ? renta.getFin() : "-";
        String costo = renta != null ? renta.getTotal() : "-";

        // Intentar obtener algunos datos adicionales si existen (imagen, color, year, precio_por_dia)
        String imagenPath = null;
        String color = null;
        String year = null;
        BigDecimal precioDia = null;

        try (Connection cn = ConexionDB.getConnection();
             PreparedStatement ps = cn.prepareStatement("SELECT * FROM vehiculos WHERE id = ?")) {
            ps.setInt(1, vehiculoId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // leer columnas de forma defensiva: solo si existen
                    java.sql.ResultSetMetaData md = rs.getMetaData();
                    int cols = md.getColumnCount();
                    Set<String> colsNames = new HashSet<>();
                    for (int i = 1; i <= cols; i++) colsNames.add(md.getColumnName(i).toLowerCase());

                    if (colsNames.contains("imagen")) {
                        imagenPath = rs.getString("imagen");
                    }
                    if (colsNames.contains("color")) {
                        color = rs.getString("color");
                    }
                    if (colsNames.contains("year")) {
                        year = Integer.toString(rs.getInt("year"));
                    } else if (colsNames.contains("anio")) {
                        year = Integer.toString(rs.getInt("anio"));
                    } else if (colsNames.contains("ano")) {
                        year = Integer.toString(rs.getInt("ano"));
                    }
                    if (colsNames.contains("precio_por_dia")) {
                        precioDia = rs.getBigDecimal("precio_por_dia");
                    }
                    // NO intentamos leer tipo_vehiculo_id aquí para evitar el error
                }
            }
        } catch (Exception e) {
            System.err.println("[WARN] Error leyendo vehiculos (fallback): " + e.getMessage());
        }

        // Construir modal programático (simple)
        ImageView iv = new ImageView();
        iv.setFitWidth(360);
        iv.setFitHeight(180);
        iv.setPreserveRatio(true);
        iv.setSmooth(true);

        boolean imgSet = false;
        if (imagenPath != null && !imagenPath.isBlank()) {
            try {
                File f = new File(imagenPath);
                if (f.exists()) {
                    iv.setImage(new Image(f.toURI().toString(), 360, 180, true, true));
                    imgSet = true;
                }
            } catch (Exception ignored) {}
        }
        if (!imgSet) {
            try {
                URL res = getClass().getResource("/com/example/proyecto_final_prograiii/images/placeholder.png");
                if (res != null) iv.setImage(new Image(res.toString(), 360, 180, true, true));
            } catch (Exception ignored) {}
        }

        Label lblModelo = new Label("Modelo: " + modelo);
        Label lblPlaca = new Label("Placa: " + placa);
        Label lblTipo = new Label("Tipo: N/A"); // omitimos búsqueda de tipo para evitar columnas inexistentes
        Label lblYear = new Label("Año: " + (year == null ? "N/A" : year));
        Label lblColor = new Label("Color: " + (color == null ? "N/A" : color));
        Label lblInicio = new Label("Fecha inicio: " + fechaInicio);
        Label lblFin = new Label("Fecha final: " + fechaFin);
        Label lblCosto = new Label("Total: " + costo);
        Label lblPrecio = new Label("Precio/día: " + (precioDia == null ? "N/A" : "$" + precioDia.setScale(2, BigDecimal.ROUND_HALF_UP).toString()));

        VBox v = new VBox(10, iv, lblModelo, lblPlaca, lblTipo, lblYear, lblColor, lblPrecio, lblInicio, lblFin, lblCosto);
        v.setPadding(new Insets(12));

        Stage stage = new Stage();
        stage.setTitle("Detalle de la renta");
        stage.setScene(new Scene(v));
        stage.initModality(Modality.APPLICATION_MODAL);
        try { stage.initOwner(tblAlquileres.getScene().getWindow()); } catch (Exception ignore) {}
        stage.showAndWait();
    }

    /**
     * Si no hay ids asociados, mostramos un detalle simple de la Renta.
     */
    private void mostrarDetalleSimple(Renta renta, Integer vehiculoId, int alquilerId) {
        String texto = String.format("Vehículo: %s\nPlaca: %s\nInicio: %s\nFin: %s\nTotal: %s",
                renta.getModelo(), renta.getPlaca(), renta.getInicio(), renta.getFin(), renta.getTotal());
        alerta("Detalle renta", texto, Alert.AlertType.INFORMATION);
    }

    /**
     * Ajusta visibilidad/texto/handlers de botones según si existe usuario en sesión.
     * Llamar después de abrir login modal para refrescar estado.
     *
     * **NOTA IMPORTANTE**: para el caso invitado, el handler de "Iniciar sesión"
     * cierra/oculta la ventana actual y abre directamente la vista de login (todo
     * desde este controlador, sin depender de Main).
     */
    private void actualizarEstadoSesion() {
        Usuario usuario = null;
        try {
            usuario = Sesion.getUsuarioActual();
        } catch (Exception ex) {
            usuario = null;
        }

        if (usuario == null) {
            // Invitado: ocultar "Mis alquileres", mostrar botón para iniciar sesión
            if (misAlquileres != null) {
                misAlquileres.setVisible(false);
                misAlquileres.setManaged(false);
            }
            if (btnVolver != null) {
                btnVolver.setVisible(false);
                btnVolver.setManaged(false);
            }

            if (btnCerrarSesion != null) {
                btnCerrarSesion.setText("Iniciar sesión");
                btnCerrarSesion.setVisible(true);
                btnCerrarSesion.setManaged(true);

                // al click, CERRAR la ventana actual y abrir la pantalla de login
                btnCerrarSesion.setOnAction(evt -> {
                    // 1) ocultar / cerrar la ventana actual
                    try {
                        if (btnCerrarSesion.getScene() != null && btnCerrarSesion.getScene().getWindow() != null) {
                            btnCerrarSesion.getScene().getWindow().hide();
                        }
                    } catch (Exception hideEx) {
                        // no fatal, continuamos intentando abrir login
                    }

                    // 2) abrir la vista de login en una nueva Stage
                    try {
                        URL url = getClass().getResource("/com/example/proyecto_final_prograiii/login-view.fxml");
                        if (url == null) {
                            alerta("Error", "No se encontró la vista de login.", Alert.AlertType.ERROR);
                            return;
                        }
                        FXMLLoader loader = new FXMLLoader(url);
                        Parent root = loader.load();
                        Scene scene = new Scene(root, 589, 400);

                        // aplicar CSS si existe
                        try {
                            scene.getStylesheets().add(getClass().getResource("/com/example/proyecto_final_prograiii/css/login.css").toExternalForm());
                        } catch (Exception cssEx) {
                            // no crítico
                        }

                        Stage loginStage = new Stage();
                        loginStage.setTitle("INICIO DE SESION");
                        loginStage.setScene(scene);
                        loginStage.initModality(Modality.APPLICATION_MODAL);
                        loginStage.show();
                    } catch (IOException ioEx) {
                        ioEx.printStackTrace();
                        alerta("Error", "No se pudo abrir la ventana de login: " + ioEx.getMessage(), Alert.AlertType.ERROR);
                    }
                });
            }

            lblBienvenida.setText("Bienvenido/a — Vehículos disponibles");
        } else {
            // Usuario logueado: mostrar mis reservas y botón de cerrar sesión
            if (misAlquileres != null) {
                misAlquileres.setVisible(true);
                misAlquileres.setManaged(true);
            }

            if (btnVolver != null) {
                // mantener estado por defecto (se controla al mostrar reservas)
                btnVolver.setVisible(false);
                btnVolver.setManaged(false);
            }

            if (btnCerrarSesion != null) {
                btnCerrarSesion.setText("Cerrar sesión");
                btnCerrarSesion.setVisible(true);
                btnCerrarSesion.setManaged(true);

                // al click, ejecutar el método existente de logout
                btnCerrarSesion.setOnAction(this::cerrarSesion);
            }

            lblBienvenida.setText("Bienvenido, " + usuario.getNombreUsuario());
        }
    }

    // --------- Modificación principal en la carga de alquileres: ahora obtenemos vehiculo_id y alquiler_id
    private void cargarAlquileresCliente(int clienteId) {
        ObservableList<Renta> lista = FXCollections.observableArrayList();
        detallesMap.clear(); // limpiar mapa antes de llenar

        String sql = """
    SELECT 
        a.id AS alquiler_id,
        v.id AS vehiculo_id,
        v.modelo,
        v.placa,
        a.fecha_inicio,
        COALESCE(a.fecha_fin_real, a.fecha_fin_estimada) AS fecha_fin,
        a.costo_total
    FROM alquileres a
    INNER JOIN vehiculos v ON v.id = a.vehiculo_id
    WHERE a.cliente_id = ?
    ORDER BY a.fecha_inicio DESC
""";

        try (Connection cn = ConexionDB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, clienteId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int alquilerId = rs.getInt("alquiler_id");
                    int vehiculoId = rs.getInt("vehiculo_id");

                    String sInicio = "-";
                    java.sql.Date dInicio = rs.getDate("fecha_inicio");
                    if (dInicio != null) {
                        sInicio = dInicio.toString();
                    }

                    String sFin = "-";
                    java.sql.Date dFin = rs.getDate("fecha_fin");
                    if (dFin != null) {
                        sFin = dFin.toString();
                    }

                    String sTotal = "-";
                    java.math.BigDecimal monto = rs.getBigDecimal("costo_total");
                    if (monto != null) {
                        sTotal = "$" + monto.setScale(2, java.math.RoundingMode.HALF_UP).toString();
                    }

                    Renta r = new Renta(
                            rs.getString("modelo"),
                            rs.getString("placa"),
                            sInicio,
                            sFin,
                            sTotal
                    );

                    // almacenar en lista y map
                    lista.add(r);
                    detallesMap.put(r, new int[]{vehiculoId, alquilerId});
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        tblAlquileres.setItems(lista);
    }

    @FXML
    private void mostrarMisAlquileres() {
        Usuario usuario = Sesion.getUsuarioActual();
        if (usuario == null) {
            alerta("Error", "Debe iniciar sesión para ver sus alquileres.", Alert.AlertType.WARNING);
            return;
        }

        int clienteId = obtenerClienteIdPorUsuario(usuario.getId());

        if (clienteId == -1) {
            alerta("Error", "No se encontró el cliente asociado a este usuario.", Alert.AlertType.ERROR);
            return;
        }

        cargarAlquileresCliente(clienteId);

        scroll.setVisible(false);
        scroll.setManaged(false);

        btnVolver.setVisible(true);
        btnVolver.setManaged(true);

        tblAlquileres.setVisible(true);
        tblAlquileres.setManaged(true);
    }

    @FXML
    void volver(ActionEvent event) {
        scroll.setVisible(true);
        scroll.setManaged(true);

        btnVolver.setVisible(false);
        btnVolver.setManaged(false);

        tblAlquileres.setVisible(false);
        tblAlquileres.setManaged(false);
    }

    @FXML
    public void cerrarSesion(ActionEvent event) {
        // 1) Limpiar Sesion
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
        // 2) Cargar login-view.fxml y cerrar la ventana actual
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/proyecto_final_prograiii/login-view.fxml"));
        try {
            btnCerrarSesion.getScene().getWindow().hide();
        } catch (Exception ignore) {}
        try {
            Parent root = fxmlLoader.load();
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
        // aseguro que el diálogo se muestre correctamente aunque el contenido sea pequeño
        alert.getDialogPane().setMinHeight(javafx.scene.layout.Region.USE_PREF_SIZE);
        alert.show();
    }

    private void agregarAnimacionHover(VBox card) {
        // animación suave al pasar el mouse: escala + translate
        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(160), card);
        scaleUp.setToX(1.03);
        scaleUp.setToY(1.03);

        TranslateTransition liftUp = new TranslateTransition(Duration.millis(160), card);
        liftUp.setToY(-6);

        ParallelTransition enter = new ParallelTransition(scaleUp, liftUp);

        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(160), card);
        scaleDown.setToX(1.0);
        scaleDown.setToY(1.0);

        TranslateTransition liftDown = new TranslateTransition(Duration.millis(160), card);
        liftDown.setToY(0);

        ParallelTransition exit = new ParallelTransition(scaleDown, liftDown);

        card.setOnMouseEntered(ev -> {
            exit.stop();
            enter.playFromStart();
        });
        card.setOnMouseExited(ev -> {
            enter.stop();
            exit.playFromStart();
        });
    }

    public static int obtenerClienteIdPorUsuario(int usuarioId) {
        String sql = "SELECT id FROM clientes WHERE usuario_id = ?";
        try (Connection cn = ConexionDB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("id");
        } catch (Exception e) { e.printStackTrace(); }
        return -1;
    }
}
