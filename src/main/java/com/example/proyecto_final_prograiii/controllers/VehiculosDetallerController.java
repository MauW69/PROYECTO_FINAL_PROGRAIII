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
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.math.BigDecimal;
import java.util.ArrayList;
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

    @FXML private ImageView imgCarro;
    @FXML private Button btnCerrar;
    @FXML private Button btnReservar;

    @FXML private Button btnConfirmarRenta;
    @FXML private DatePicker fechaInicio;
    @FXML private DatePicker fechaFin;

    private Vehiculo vehiculo;
    private int vehiculoId = 0;

    private void ajustarInterfazPorRol() {
        Usuario usuario = Sesion.getUsuarioActual();
        if (usuario == null) {
            if (btnReservar != null) { btnReservar.setVisible(false); btnReservar.setManaged(false); }
            if (btnConfirmarRenta != null) { btnConfirmarRenta.setVisible(false); btnConfirmarRenta.setManaged(false); }
            return;
        }
        int rol = usuario.getRolId();
        switch (rol) {
            case 3: // cliente
                if (btnConfirmarRenta != null) { btnConfirmarRenta.setVisible(false); btnConfirmarRenta.setManaged(false); }
                break;
            case 2: // empleado
                if (btnReservar != null) { btnReservar.setVisible(false); btnReservar.setManaged(false); }
                break;
            default:
                if (btnReservar != null) { btnReservar.setVisible(false); btnReservar.setManaged(false); }
                if (btnConfirmarRenta != null) { btnConfirmarRenta.setVisible(false); btnConfirmarRenta.setManaged(false); }
        }
    }

    public void initialize(){
        ajustarInterfazPorRol();
    }

    public void cargarVehiculo(int vehiculoId) {
        VehiculosDAO dao = new VehiculosDAO();
        Vehiculo v = dao.obtenerPorIdVehiculo(vehiculoId);
        if (v == null) {
            alerta("Vehículo no encontrado", "No se encontró el vehículo con id = " + vehiculoId, Alert.AlertType.WARNING);
            return;
        }
        this.vehiculo = v;
        this.vehiculoId = vehiculoId;
        llenarDatosEnVista();
        ajustarInterfazPorRol();
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

    @FXML
    public void reservar(ActionEvent event) {
        try {
            if (vehiculo == null) {
                alerta("Acción inválida", "No hay vehículo cargado.", Alert.AlertType.WARNING);
                return;
            }
            String estado = vehiculo.getEstado();
            if (estado != null && (estado.equalsIgnoreCase("RESERVADO") || estado.equalsIgnoreCase("ALQUILADO"))) {
                alerta(
                        "No disponible",
                        "Este vehículo ya está " + estado + " y no puede reservarse.",
                        Alert.AlertType.WARNING
                );
                return;
            }
            Cliente cliente = Sesion.getClienteActual();
            if (cliente == null) {
                alerta("Error", "Debe iniciar sesión como cliente.", Alert.AlertType.ERROR);
                return;
            }

            LocalDate inicio = fechaInicio != null ? fechaInicio.getValue() : null;
            LocalDate fin = fechaFin != null ? fechaFin.getValue() : null;
            LocalDate hoy = LocalDate.now();

            if (inicio == null) { alerta("Fecha inicio requerida", "Selecciona la fecha de inicio.", Alert.AlertType.WARNING); return; }
            if (inicio.isBefore(hoy)) { alerta("Fecha inválida", "La fecha de inicio no puede ser menor a hoy.", Alert.AlertType.WARNING); return; }
            if (fin == null) { alerta("Fecha fin requerida", "Selecciona la fecha estimada de fin.", Alert.AlertType.WARNING); return; }
            if (!fin.isAfter(inicio)) { alerta("Fecha inválida", "La fecha final debe ser mayor que la fecha inicial.", Alert.AlertType.WARNING); return; }

            long dias = ChronoUnit.DAYS.between(inicio, fin) + 1;
            if (dias <= 0) { alerta("Fechas inválidas", "Revisa las fechas ingresadas.", Alert.AlertType.WARNING); return; }
            if (dias > 30) { alerta("Límite excedido", "No puedes rentar un vehículo por más de 30 días.", Alert.AlertType.WARNING); return; }

            int clienteId = cliente.getId();
            BigDecimal precioPorDia = vehiculo.getPrecioPorDia() != null ? vehiculo.getPrecioPorDia() : BigDecimal.ZERO;
            BigDecimal precioTotal = precioPorDia.multiply(BigDecimal.valueOf(dias));

            // Crear objeto Alquiler
            Alquiler alquiler = new Alquiler();
            alquiler.setVehiculoId(vehiculo.getId());
            alquiler.setClienteId(clienteId);
            alquiler.setFechaInicio(inicio);
            alquiler.setFechaFin(fin);
            alquiler.setPrecioDiario(precioPorDia);
            alquiler.setEstado("EN CURSO");
            alquiler.setNotas("Solicitud enviada desde el cliente.");

            // Confirmación
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmar reserva");
            confirm.setHeaderText("Confirma tu reserva");
            confirm.setContentText("Días: " + dias +
                    "\nPrecio/día: $" + precioPorDia +
                    "\n\nTotal a pagar: $" + precioTotal +
                    "\n\n¿Deseas confirmar la reserva?");
            if (confirm.showAndWait().orElse(null) != javafx.scene.control.ButtonType.OK) {
                alerta("Cancelado", "Reserva cancelada por el usuario.", Alert.AlertType.INFORMATION);
                return;
            }

            // Intentar INSERT normal
            AlquilerDAO dao = new AlquilerDAO();
            boolean exito = false;
            try {
                exito = dao.crearSolicitudAlquiler(alquiler);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Marcar vehículo como RESERVADO si funcionó
            if (exito) {
                VehiculosDAO vdao = new VehiculosDAO();
                vdao.actualizarEstadoVehiculo(vehiculo.getId(), "RESERVADO");
                vehiculo.setEstado("RESERVADO");
                lblEstado.setText("Estado: RESERVADO");

                alerta("Solicitud enviada", "El empleado revisará tu solicitud.", Alert.AlertType.INFORMATION);
                return;
            }

            // Fallback en caso de fallo del DAO
            try (Connection cn = ConexionDB.getConnection()) {

                String sql = """
                INSERT INTO alquileres (vehiculo_id, cliente_id, fecha_inicio, fecha_fin, precio_diario, estado, notas)
                VALUES (?, ?, ?, ?, ?, 'EN CURSO', ?)
            """;

                try (PreparedStatement ps = cn.prepareStatement(sql)) {
                    ps.setInt(1, vehiculo.getId());
                    ps.setInt(2, clienteId);
                    ps.setDate(3, Date.valueOf(inicio));
                    ps.setDate(4, Date.valueOf(fin));
                    ps.setBigDecimal(5, precioPorDia);
                    ps.setString(6, "Solicitud enviada desde el cliente.");

                    int rows = ps.executeUpdate();
                    if (rows > 0) {
                        exito = true;

                        // También marcar vehículo como RESERVADO en fallback
                        VehiculosDAO vdao = new VehiculosDAO();
                        vdao.actualizarEstadoVehiculo(vehiculo.getId(), "RESERVADO");
                        vehiculo.setEstado("RESERVADO");
                        lblEstado.setText("Estado: RESERVADO");

                        alerta("Solicitud enviada", "El empleado revisará tu solicitud.", Alert.AlertType.INFORMATION);
                        return;
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            alerta("Error", "No se pudo completar la solicitud.", Alert.AlertType.ERROR);

        } catch (Exception ex) {
            ex.printStackTrace();
            alerta("Error", "Error al procesar la solicitud: " + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    void ConfirmarRenta(ActionEvent event) {
        if (vehiculo == null) {
            alerta("Error", "No hay vehículo cargado.", Alert.AlertType.ERROR);
            return;
        }

        if (!"DISPONIBLE".equalsIgnoreCase(vehiculo.getEstado())) {
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
        VehiculosDAO vdao = new VehiculosDAO();
        vdao.actualizarEstadoVehiculo(vehiculo.getId(), "ALQUILADO");
        vehiculo.setEstado("ALQUILADO");
        lblEstado.setText("Estado: ALQUILADO");
        ajustarInterfazPorRol();
        alerta("Renta confirmada", "La renta ha sido confirmada correctamente.", Alert.AlertType.INFORMATION);
        ((Stage) btnCerrar.getScene().getWindow()).close();
    }


    private void alerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert a = new Alert(tipo);
        a.setTitle(titulo);
        a.setContentText(mensaje);
        a.showAndWait();
    }
}
