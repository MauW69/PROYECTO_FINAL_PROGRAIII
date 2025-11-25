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

        // -------------------------------------------------------
        // Obtener cliente desde la sesion
        // -------------------------------------------------------
        Cliente cliente = Sesion.getClienteActual();

        if (cliente == null) {
            alerta("Error", "Debe iniciar sesión como cliente.", Alert.AlertType.ERROR);
            return;
        }

        int clienteId = cliente.getId();

        // -------------------------------------------------------
        // instanciar objeto alquiler
        // -------------------------------------------------------
        Alquiler alquiler = new Alquiler();
        alquiler.setVehiculoId(vehiculo.getId());
        alquiler.setClienteId(clienteId);
        alquiler.setFechaInicio(LocalDate.now());
        alquiler.setPrecioDiario(vehiculo.getPrecioPorDia());
        alquiler.setEstado("EN CURSO");
        alquiler.setNotas("Solicitud enviada desde el cliente.");

        // Guardar en la BD
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


    }


    // helper alert
    private void alerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert a = new Alert(tipo);
        a.setTitle(titulo);
        a.setContentText(mensaje);
        a.showAndWait();
    }
}
