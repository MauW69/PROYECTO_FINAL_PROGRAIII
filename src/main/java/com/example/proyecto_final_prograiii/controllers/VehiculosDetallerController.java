package com.example.proyecto_final_prograiii.controllers;

import com.example.proyecto_final_prograiii.DAO.AlquilerDAO;
import com.example.proyecto_final_prograiii.DAO.PagoDAO;
import com.example.proyecto_final_prograiii.DAO.VehiculosDAO;
import com.example.proyecto_final_prograiii.config.ConexionDB;
import com.example.proyecto_final_prograiii.models.Alquiler;
import com.example.proyecto_final_prograiii.models.Cliente;
import com.example.proyecto_final_prograiii.models.Usuario;
import com.example.proyecto_final_prograiii.models.Vehiculo;
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

    //nuevo metodo de pago
    @FXML
    private ComboBox<String> cmbMetodoPagoCliente;

    @FXML private ImageView imgCarro;
    @FXML private Button btnCerrar;
    @FXML private Button btnReservar;

    @FXML private Button btnConfirmarRenta;
    @FXML private DatePicker fechaInicio;
    @FXML private DatePicker fechaFin;

    private Vehiculo vehiculo;
    private int vehiculoId = 0;

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
            ocultar(btnReservar);
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
                ocultar(btnReservar);
                ocultar(cmbMetodoPagoCliente);

                if (fechaInicio != null) fechaInicio.setDisable(true);
                if (fechaFin != null) fechaFin.setDisable(true);
                break;

            default:
                //Cualquier otro rol
                ocultar(btnReservar);
                ocultar(btnConfirmarRenta);
                ocultar(cmbMetodoPagoCliente);
                if (fechaInicio != null) fechaInicio.setDisable(true);
                if (fechaFin != null) fechaFin.setDisable(true);
                break;
        }
    }

    public void initialize(){

        if (cmbMetodoPagoCliente != null) {
            cmbMetodoPagoCliente.getItems().addAll("Efectivo", "Tarjeta");
        }

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
        //nuevo
        //cargar las fechas para que sean visibles para el empleado
        Usuario usuario = Sesion.getUsuarioActual();

        if (usuario != null && usuario.getRolId() == 2) { //empleado
            AlquilerDAO aDao = new AlquilerDAO();
            Alquiler solicitud = aDao.obtenerSolicitudEnCursoPorVehiculo(vehiculoId);

            if (solicitud != null) {
                fechaInicio.setValue(solicitud.getFechaInicio());
                fechaFin.setValue(solicitud.getFechaFin());
            }
        }
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
                alerta("No disponible", "Este vehículo ya está " + estado + " y no puede reservarse.", Alert.AlertType.WARNING);
                return;
            }

            Cliente cliente = Sesion.getClienteActual();
            if (cliente == null) {
                alerta("Error", "Debe iniciar sesión como cliente.", Alert.AlertType.ERROR);
                return;
            }

            //validacion de fechas
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

            //metodo de pago del cliente
            String metodoPago = cmbMetodoPagoCliente != null ? cmbMetodoPagoCliente.getValue() : null;
            if (metodoPago == null) {
                alerta("Método de pago", "Debes seleccionar un método de pago.", Alert.AlertType.WARNING);
                return;
            }

            int clienteId = cliente.getId();
            BigDecimal precioPorDia = vehiculo.getPrecioPorDia() != null ? vehiculo.getPrecioPorDia() : BigDecimal.ZERO;
            BigDecimal precioTotal = precioPorDia.multiply(BigDecimal.valueOf(dias));

            // Crear objeto alquiler
            Alquiler alquiler = new Alquiler();
            alquiler.setVehiculoId(vehiculo.getId());
            alquiler.setClienteId(clienteId);
            alquiler.setFechaInicio(inicio);
            alquiler.setFechaFin(fin);
            alquiler.setPrecioDiario(precioPorDia);
            alquiler.setEstado("EN CURSO");

            //guardamos el metodo de pago elegido en notas
            alquiler.setNotas("Método de pago: " + metodoPago);

            // Confirmación
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmar reserva");
            confirm.setHeaderText("Confirma tu reserva");
            confirm.setContentText("Días: " + dias +
                    "\nPrecio/dia: $" + precioPorDia +
                    "\nTotal estimado: $" + precioTotal +
                    "\nMétodo de pago: " + metodoPago +
                    "\n\n¿Deseas confirmar la reserva?");

            if (confirm.showAndWait().orElse(null) != ButtonType.OK) {
                alerta("Cancelado", "Reserva cancelada por el usuario.", Alert.AlertType.INFORMATION);
                return;
            }

            // Insertar alquiler y obtener ID
            AlquilerDAO dao = new AlquilerDAO();
            int alquilerId = dao.crearSolicitudAlquiler(alquiler);

            if (alquilerId <= 0) {
                alerta("Error", "No se pudo crear la solicitud.", Alert.AlertType.ERROR);
                return;
            }

            // Registrar pago inicial (solo el método, sin monto)
            PagoDAO pagoDAO = new PagoDAO();
            pagoDAO.registrarPagoInicial(alquilerId, metodoPago);

            // Marcar vehículo como reservado inmediatamente
            VehiculosDAO vdao = new VehiculosDAO();
            vdao.actualizarEstadoVehiculo(vehiculo.getId(), "RESERVADO");
            vehiculo.setEstado("RESERVADO");
            lblEstado.setText("Estado: RESERVADO");

            alerta("Solicitud enviada", "Tu solicitud fue enviada y está en revisión.", Alert.AlertType.INFORMATION);

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

        // El empleado solo puede confirmar si el vehículo está RESERVADO
        if (!"RESERVADO".equalsIgnoreCase(vehiculo.getEstado())) {
            alerta("No disponible", "El vehiculo debe estar RESERVADO para confirmar la renta.", Alert.AlertType.WARNING);
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
