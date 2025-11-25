package com.example.proyecto_final_prograiii.controllers;

import com.example.proyecto_final_prograiii.DAO.AlquilerDAO;
import com.example.proyecto_final_prograiii.DAO.VehiculosDAO;
import com.example.proyecto_final_prograiii.config.ConexionDB;
import com.example.proyecto_final_prograiii.models.Alquiler;
import com.example.proyecto_final_prograiii.models.Cliente;
import com.example.proyecto_final_prograiii.models.Usuario;
import com.example.proyecto_final_prograiii.models.Vehiculo;
import com.example.proyecto_final_prograiii.utils.Sesion;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.net.URL;
import java.math.BigDecimal;

/**
 * Controller para la vista de detalle de vehículo.
 * Usa VehiculosDAO.obtenerPorIdVehiculo(int)
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

    @FXML private ImageView imgCarro;
    @FXML private Button btnCerrar;
    @FXML private Button btnReservar;

    //nuevo boton que solo se les mostrara a los empleados y permitira confirmar la renta del vehiculo
    @FXML
    private Button btnConfirmarRenta;
    @FXML private   DatePicker fechaInicio;
    @FXML private DatePicker fechaFin;

    private Vehiculo vehiculo;

    /**
     * Método público: cargar vehículo por id (llamado desde PanelClienteController)
     */
    //este metodo solo configura la visualizacion de botones segun el rol del usuario
    private void ajustarInterfazPorRol() {

        Usuario usuario = Sesion.getUsuarioActual();

        if (usuario == null) {
            // Por seguridad: esconder ambos
            btnReservar.setVisible(false);
            btnReservar.setManaged(false);
            btnConfirmarRenta.setVisible(false);
            btnConfirmarRenta.setManaged(false);
            return;
        }

        int rol = Sesion.getUsuarioActual().getRolId();  // 3 es"Cliente" 2 es"Empleado"

        switch (rol) {
            case 3:
                // El cliente solo reserva
                btnConfirmarRenta.setVisible(false);
                btnConfirmarRenta.setManaged(false);
                break;

            case 2:
                // El empleado solo cambia estado
                btnReservar.setVisible(false);
                btnReservar.setManaged(false);
                break;

            default:
                // Roles desconocidos → oculta ambos por seguridad
                btnReservar.setVisible(false);
                btnReservar.setManaged(false);
                btnConfirmarRenta.setVisible(false);
                btnConfirmarRenta.setManaged(false);
        }
    }

    public void initialize(){
        ajustarInterfazPorRol();
        //en caso de usarse
        //Usuario usuario = Sesion.getUsuarioActual();
    }
    public void cargarVehiculo(int vehiculoId) {
        VehiculosDAO dao = new VehiculosDAO();
        Vehiculo v = dao.obtenerPorIdVehiculo(vehiculoId); // coincide con tu DAO

        if (v == null) {
            alerta("Vehículo no encontrado", "No se encontró el vehículo con id = " + vehiculoId, Alert.AlertType.WARNING);
            return;
        }

        this.vehiculo = v;
        llenarDatosEnVista();
    }

    private void llenarDatosEnVista() {
        lblModelo.setText("Modelo: " + safe(vehiculo.getModelo(), "N/A"));
        lblPlaca.setText("Placa: " + safe(vehiculo.getPlaca(), "N/A"));
        lblTipo.setText("Tipo: " + obtenerNombreTipo(vehiculo.getTipoVehiculoId()));
        lblAnio.setText("Año: " + (vehiculo.getYear() == 0 ? "N/A" : String.valueOf(vehiculo.getYear())));
        lblColor.setText("Color: " + safe(vehiculo.getColor(), "N/A"));
        lblKm.setText("Kilometraje: " + (vehiculo.getKilometraje() == 0 ? "N/A" : vehiculo.getKilometraje() + " km"));
        lblEstado.setText("Estado: " + safe(vehiculo.getEstado(), "N/A"));

        // Ocultar fecha
        lblFechaCreacion.setVisible(false);
        lblFechaCreacion.setManaged(false);

        // ==========================
        // CARGAR IMAGEN REAL
        // ==========================
        try {
            String ruta = vehiculo.getImagen();
            if (ruta != null && !ruta.isEmpty()) {
                File file = new File(ruta);
                if (file.exists()) {
                    imgCarro.setImage(new Image(file.toURI().toString()));
                    return; // evita que se coloque placeholder
                }
            }
        } catch (Exception e) {
            System.err.println("Error cargando imagen: " + e.getMessage());
        }

        // ==========================
        // PLACEHOLDER POR DEFECTO
        // ==========================
        try {
            URL placeholder = new URL("https://via.placeholder.com/260x160.png?text=Imagen");
            imgCarro.setImage(new Image(placeholder.toString()));
        } catch (Exception ex) { }
    }



    // Si deseas obtener el nombre del tipo  (tabla tipos_vehiculo)
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

    //boton para mandar solicitud de renta al empleado
    @FXML
    public void reservar(ActionEvent event) {
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

        // ---------------- VALIDACIONES ----------------

        // 1. Fecha inicio no puede ser menor al día actual
        if (inicio == null || inicio.isBefore(hoy)) {
            alerta("Fecha inválida", "La fecha de inicio no puede ser menor a hoy.", Alert.AlertType.WARNING);
            return;
        }

        // 2. Fecha fin debe ser mayor a fecha inicio
        if (fin == null || !fin.isAfter(inicio)) {
            alerta("Fecha inválida", "La fecha final debe ser mayor que la fecha inicial.", Alert.AlertType.WARNING);
            return;
        }

        // 3. No permitir alquileres mayores a 30 días
        if (inicio.plusDays(30).isBefore(fin)) {
            alerta("Límite excedido", "No puedes rentar un vehículo por más de 30 días.", Alert.AlertType.WARNING);
            return;
        }

        // -------------------------------------------------------

        int clienteId = cliente.getId();

        Alquiler alquiler = new Alquiler();
        alquiler.setVehiculoId(vehiculo.getId());
        alquiler.setClienteId(clienteId);
        alquiler.setFechaInicio(inicio);
        alquiler.setFechaFin(fin);  // <<<< IMPORTANTE
        alquiler.setPrecioDiario(vehiculo.getPrecioPorDia());
        alquiler.setEstado("EN CURSO");
        alquiler.setNotas("Solicitud enviada desde el cliente.");

        // Guardar en BD
        AlquilerDAO dao = new AlquilerDAO();
        boolean exito = dao.crearSolicitudAlquiler(alquiler);

        if (exito) {
            alerta("Solicitud enviada", "El empleado revisará tu solicitud de renta.", Alert.AlertType.INFORMATION);
        } else {
            alerta("Error", "No se pudo completar la solicitud.", Alert.AlertType.ERROR);
        }
    }


    //metodo que confirma la renta
    @FXML
    void ConfirmarRenta(ActionEvent event) {
        if (vehiculo == null) {
            alerta("Error", "No hay vehículo cargado.", Alert.AlertType.ERROR);
            return;
        }

        if (!vehiculo.getEstado().equalsIgnoreCase("DISPONIBLE")) {
            alerta("No disponible", "El vehículo no está disponible para renta.", Alert.AlertType.WARNING);
            return;
        }

        Usuario empleado = Sesion.getUsuarioActual();
        if (empleado == null || empleado.getRolId() != 2) {
            alerta("Permiso denegado", "Solo un empleado puede confirmar la renta.", Alert.AlertType.ERROR);
            return;
        }

        AlquilerDAO alquilerDAO = new AlquilerDAO();
        Alquiler solicitud = alquilerDAO.obtenerSolicitudEnCursoPorVehiculo(vehiculo.getId());

        if (solicitud == null) {
            alerta("Sin solicitud", "No existe una solicitud activa para este vehículo.", Alert.AlertType.WARNING);
            return;
        }

        boolean ok = alquilerDAO.confirmarRenta(solicitud.getId(), empleado.getId());

        if (!ok) {
            alerta("Error", "No se pudo confirmar la renta.", Alert.AlertType.ERROR);
            return;
        }

        // Cambiar estado vehículo
        VehiculosDAO vdao = new VehiculosDAO();
        vdao.actualizarEstadoVehiculo(vehiculo.getId(), "ALQUILADO");

        vehiculo.setEstado("ALQUILADO");
        lblEstado.setText("Estado: ALQUILADO");

        alerta("Renta confirmada", "La renta ha sido confirmada correctamente.", Alert.AlertType.INFORMATION);

        // Cerrar ventana
        ((Stage) btnCerrar.getScene().getWindow()).close();

    }


    // helper alert
    private void alerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert a = new Alert(tipo);
        a.setTitle(titulo);
        a.setContentText(mensaje);
        a.showAndWait();
    }
}
