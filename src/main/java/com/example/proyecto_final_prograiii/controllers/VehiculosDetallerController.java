package com.example.proyecto_final_prograiii.controllers;

import com.example.proyecto_final_prograiii.DAO.AlquilerDAO;
import com.example.proyecto_final_prograiii.DAO.PagoDAO;
import com.example.proyecto_final_prograiii.DAO.VehiculosDAO;
import com.example.proyecto_final_prograiii.config.ConexionDB;
import com.example.proyecto_final_prograiii.models.*;
import com.example.proyecto_final_prograiii.utils.Sesion;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

import java.io.File;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.math.BigDecimal;
import java.util.List;

/**
 * Controller para la vista de detalle de vehículo.
 */
public class VehiculosDetallerController {
    @FXML private Label lblModelo;
    @FXML private Label lblPlaca;
    @FXML private Label lblTipo;
    @FXML private Label lblAnio;
    @FXML private Label lblColor;
    @FXML private Label lblKm;
    @FXML private Label lblEstado;
    @FXML private Label lblFechaCreacion;

    //nuevo metodo de pago
    @FXML
    private ComboBox<String> cmbMetodoPagoCliente;

    @FXML private ImageView imgCarro;
    @FXML private Button btnCerrar;
    @FXML private Button btnAlquilar;

    @FXML private Button btnConfirmarRenta;
    @FXML private DatePicker fechaInicio;
    @FXML private DatePicker fechaFin;

    private Vehiculo vehiculo;
    private int vehiculoId = 0;
    private int alquilerId;


    //cambios en el ajuste de de interfaz(para dejarlo mas limpio)
    private void ocultar(Node n) {
        if (n != null) {
            n.setVisible(false);
            n.setManaged(false);
        }
    }

    private void mostrar(Node n) {
        if (n != null) {
            n.setVisible(true);
            n.setManaged(true);
        }
    }

    private void ajustarInterfazPorRol() {

        Usuario usuario = Sesion.getUsuarioActual();

        //si no hay usuario
        if (usuario == null) {
            ocultar(btnAlquilar);
            ocultar(btnConfirmarRenta);
            ocultar(cmbMetodoPagoCliente);
            return;
        }

        int rol = usuario.getRolId();

        switch (rol) {

            case 3://cliente
                //Cliente: solo reservar y metodo de pago visibles
                ocultar(btnConfirmarRenta);
                mostrar(cmbMetodoPagoCliente);

                if (fechaInicio != null) fechaInicio.setDisable(false);
                if (fechaFin != null) fechaFin.setDisable(false);
                break;

            case 2: //empleado
                //empleado: no muestra boton reservar ni metodo de pago
                ocultar(btnAlquilar);
                ocultar(cmbMetodoPagoCliente);

                if (fechaInicio != null) fechaInicio.setDisable(true);
                if (fechaFin != null) fechaFin.setDisable(true);
                break;

            default:
                //Cualquier otro rol
                ocultar(btnAlquilar);
                ocultar(btnConfirmarRenta);
                ocultar(cmbMetodoPagoCliente);
                if (fechaInicio != null) fechaInicio.setDisable(true);
                if (fechaFin != null) fechaFin.setDisable(true);
                break;
        }
    }
    //inicializador
    public void initialize(){

        if (cmbMetodoPagoCliente != null) {
            cmbMetodoPagoCliente.getItems().addAll("Efectivo", "Tarjeta");
        }

        ajustarInterfazPorRol();
    }
    //metodo que marca los dias en los que los vehiculos estan rentados
    private void marcarFechasOcupadas(DatePicker picker, List<LocalDate[]> rangos) {
        picker.setDayCellFactory(dp -> new DateCell() {

            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);

                if (empty || date == null) return;

                setDisable(false);
                setStyle("");

                for (LocalDate[] r : rangos) {
                    LocalDate ini = r[0];
                    LocalDate fin = r[1];

                    if ((date.isEqual(ini) || date.isAfter(ini)) &&
                            (date.isEqual(fin) || date.isBefore(fin))) {

                        setDisable(true);
                        setStyle("-fx-background-color: #ff9999; -fx-text-fill: black;");
                    }
                }
            }
        });
    }


    public void cargarVehiculo(int vehiculoId, int alquilerId) {
        this.vehiculoId = vehiculoId;
        this.alquilerId = alquilerId;

        // cargar datos del vehículo
        VehiculosDAO dao = new VehiculosDAO();
        this.vehiculo = dao.obtenerPorIdVehiculo(vehiculoId);

        if (vehiculo == null) {
            alerta("Error", "No se encontró el vehículo con ID = " + vehiculoId, Alert.AlertType.ERROR);
            return;
        }

        llenarDatosEnVista();
        ajustarInterfazPorRol();

        AlquilerDAO aDao = new AlquilerDAO();
        Alquiler alquiler = null;

        // ================================
        // 1 SI VIENE ALQUILER DESDE HISTORIAL
        // ================================
        if (alquilerId > 0) {
            alquiler = aDao.obtenerAlquilerPorId(alquilerId);
        }

        // ================================
        // 2 SI NO VIENE ALQUILER → BUSCAR ALQUILER ACTIVO
        // ================================
        if (alquiler == null) {
            alquiler = aDao.obtenerAlquilerActivoPorVehiculo(vehiculoId);
        }

        // ================================
        // 3 Mostrar fecha inicio/fin en los DatePicker
        // ================================
        if (alquiler != null) {
            System.out.println("→ Fecha que llega desde BD = " + alquiler.getFechaFin());

            if (fechaInicio != null) fechaInicio.setValue(alquiler.getFechaInicio());
            if (fechaFin != null && alquiler.getFechaFin() != null)
                fechaFin.setValue(alquiler.getFechaFin());
        }

        // ================================
        // 4 Marcar dias ocupados en rojo
        // ================================
        List<LocalDate[]> rangosOcupados = aDao.obtenerRangosOcupados(vehiculoId);

        if (fechaInicio != null) marcarFechasOcupadas(fechaInicio, rangosOcupados);
        if (fechaFin != null)    marcarFechasOcupadas(fechaFin, rangosOcupados);
    }



    private void llenarDatosEnVista() {
        lblModelo.setText("Modelo: " + safe(vehiculo.getModelo(), "N/A"));
        lblPlaca.setText("Placa: " + safe(vehiculo.getPlaca(), "N/A"));
        lblTipo.setText("Tipo: " + obtenerNombreTipo(vehiculo.getTipoVehiculoId()));
        lblAnio.setText("Año: " + (vehiculo.getYear() == 0 ? "N/A" : String.valueOf(vehiculo.getYear())));
        lblColor.setText("Color: " + safe(vehiculo.getColor(), "N/A"));
        lblKm.setText("Kilometraje: " + (vehiculo.getKilometraje() == 0 ? "N/A" : vehiculo.getKilometraje() + " km"));
        lblEstado.setText("Estado: " + safe(vehiculo.getEstado(), "N/A"));

        if (lblFechaCreacion != null) {
            lblFechaCreacion.setVisible(false);
            lblFechaCreacion.setManaged(false);
        }

        // Imagen
        try {
            String ruta = vehiculo.getImagen();
            if (ruta != null && !ruta.isEmpty()) {
                File file = new File(ruta);
                if (file.exists()) {
                    imgCarro.setImage(new Image(file.toURI().toString()));
                    return;
                } else {
                    System.out.println("[VehiculosDetallerController] Imagen referenciada pero no encontrada: " + file.getAbsolutePath());
                }
            }
        } catch (Exception e) {
            System.err.println("[VehiculosDetallerController] Error cargando imagen: " + e.getMessage());
        }

        // placeholder
        try {
            URL placeholder = new URL("https://via.placeholder.com/260x160.png?text=Imagen");
            imgCarro.setImage(new Image(placeholder.toString()));
        } catch (Exception ex) { ex.printStackTrace(); }
    }

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
            System.err.println("[VehiculosDetallerController] Error obtenerNombreTipo: " + ex.getMessage());
        }
        return "N/A";
    }

    private String safe(String s, String def) { return (s == null || s.isBlank()) ? def : s; }

    @FXML
    public void cerrar(ActionEvent event) {
        Stage st = (Stage) btnCerrar.getScene().getWindow();
        st.close();
    }

    // Helper: comprueba si la tabla tiene la columna dada (case-insensitive)
    private boolean hasColumn(Connection cn, String tableName, String columnName) throws SQLException {
        DatabaseMetaData meta = cn.getMetaData();
        try (ResultSet rs = meta.getColumns(null, null, tableName, columnName)) {
            if (rs.next()) return true;
        }
        try (ResultSet rs2 = meta.getColumns(null, null, tableName, columnName.toLowerCase())) {
            return rs2.next();
        }
    }

    //metodo que registra el alquiler y el pago por parte de los clientes
    @FXML
    public void Alquilar(ActionEvent event) {
        try {

            if (vehiculo == null) {
                alerta("Acción inválida", "No hay vehículo cargado.", Alert.AlertType.WARNING);
                return;
            }

            Cliente cliente = Sesion.getClienteActual();
            if (cliente == null) {
                alerta("Error", "Debe iniciar sesión como cliente.", Alert.AlertType.ERROR);
                return;
            }

            LocalDate inicio = fechaInicio.getValue();
            LocalDate fin = fechaFin.getValue();
            LocalDate hoy = LocalDate.now();

            if (inicio == null) { alerta("Fecha inicio requerida", "Selecciona la fecha de inicio.", Alert.AlertType.WARNING); return; }
            if (inicio.isBefore(hoy)) { alerta("Fecha inválida", "La fecha de inicio no puede ser menor a hoy.", Alert.AlertType.WARNING); return; }
            if (fin == null) { alerta("Fecha fin requerida", "Selecciona la fecha estimada de fin.", Alert.AlertType.WARNING); return; }
            if (!fin.isAfter(inicio)) { alerta("Fecha inválida", "La fecha final debe ser mayor que la fecha inicial.", Alert.AlertType.WARNING); return; }

            long dias = ChronoUnit.DAYS.between(inicio, fin) + 1;
            if (dias <= 0) { alerta("Fechas inválidas", "Revisa las fechas ingresadas.", Alert.AlertType.WARNING); return; }
            if (dias > 30) { alerta("Límite excedido", "No puedes rentar un vehículo por más de 30 días.", Alert.AlertType.WARNING); return; }

            String metodoPago = cmbMetodoPagoCliente.getValue();
            if (metodoPago == null) {
                alerta("Método de pago", "Debes seleccionar un método de pago.", Alert.AlertType.WARNING);
                return;
            }

            // Validacion de traslape
            AlquilerDAO alquilerDAO = new AlquilerDAO();
            boolean hayTraslape = alquilerDAO.existeTraslape(vehiculo.getId(), inicio, fin);
            if (hayTraslape) {
                alerta("Fechas no disponibles", "El vehículo ya tiene renta en ese rango.", Alert.AlertType.WARNING);
                return;
            }

            BigDecimal precioPorDia = vehiculo.getPrecioPorDia() != null ? vehiculo.getPrecioPorDia() : BigDecimal.ZERO;
            BigDecimal costoTotal = precioPorDia.multiply(BigDecimal.valueOf(dias));

            // Construir objetos
            Alquiler alquiler = new Alquiler();
            alquiler.setVehiculoId(vehiculo.getId());
            alquiler.setClienteId(cliente.getId());
            alquiler.setFechaInicio(inicio);
            alquiler.setFechaFin(fin);
            alquiler.setPrecioDiario(precioPorDia);
            alquiler.setCostoTotal(costoTotal);
            alquiler.setEstado("ALQUILADO"); // directo
            alquiler.setNotas("Pago: " + metodoPago);

            Pago pago = new Pago();
            // pago.setId(...) // no hace falta
            pago.setAlquilerId(0); // se reemplaza por el DAO al insertar
            pago.setMonto(costoTotal);
            pago.setMetodo(metodoPago);

            // Confirmación al cliente (opcional)
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmar alquiler");
            confirm.setHeaderText("Confirma tu alquiler");
            confirm.setContentText("Días: " + dias +
                    "\nPrecio/día: $" + precioPorDia +
                    "\nTotal a pagar ahora: $" + costoTotal +
                    "\nMétodo de pago: " + metodoPago +
                    "\n\n¿Deseas confirmar el alquiler y pagar ahora?");
            if (confirm.showAndWait().orElse(null) != ButtonType.OK) {
                alerta("Cancelado", "El alquiler fue cancelado por el usuario.", Alert.AlertType.INFORMATION);
                return;
            }

            // Llamada transaccional: inserta alquiler, inserta pago y marca vehículo ALQUILADO
            int alquilerId = alquilerDAO.crearAlquilerConPago(alquiler, pago);
            if (alquilerId <= 0) {
                alerta("Error", "No se pudo crear el alquiler. Intenta nuevamente.", Alert.AlertType.ERROR);
                return;
            }

            // Actualizar UI local
            vehiculo.setEstado("ALQUILADO");
            lblEstado.setText("Estado: ALQUILADO");

            alerta("Alquiler completado", "El alquiler y pago han sido registrados correctamente.", Alert.AlertType.INFORMATION);

        } catch (Exception ex) {
            ex.printStackTrace();
            alerta("Error", "Ocurrió un error al procesar la solicitud: " + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void alerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert a = new Alert(tipo);
        a.setTitle(titulo);
        a.setContentText(mensaje);
        a.showAndWait();
    }
}
