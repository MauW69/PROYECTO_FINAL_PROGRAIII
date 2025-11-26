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

            Cliente cliente = Sesion.getClienteActual();
            if (cliente == null) {
                alerta("Error", "Debe iniciar sesión como cliente.", Alert.AlertType.ERROR);
                return;
            }

            LocalDate inicio = (fechaInicio == null) ? null : fechaInicio.getValue();
            LocalDate fin = (fechaFin == null) ? null : fechaFin.getValue();
            LocalDate hoy = LocalDate.now();

            if (inicio == null) { alerta("Fecha inicio requerida", "Selecciona la fecha de inicio.", Alert.AlertType.WARNING); return; }
            if (inicio.isBefore(hoy)) { alerta("Fecha inválida", "La fecha de inicio no puede ser menor a hoy.", Alert.AlertType.WARNING); return; }
            if (fin == null) { alerta("Fecha fin requerida", "Selecciona la fecha estimada de fin.", Alert.AlertType.WARNING); return; }
            if (!fin.isAfter(inicio)) { alerta("Fecha inválida", "La fecha final debe ser mayor que la fecha inicial.", Alert.AlertType.WARNING); return; }

            long diasLong = ChronoUnit.DAYS.between(inicio, fin) + 1;
            int dias = (int) diasLong;
            if (dias <= 0) { alerta("Fechas inválidas", "Revisa las fechas ingresadas.", Alert.AlertType.WARNING); return; }
            if (dias > 30) { alerta("Límite excedido", "No puedes rentar un vehículo por más de 30 días.", Alert.AlertType.WARNING); return; }

            int clienteId = cliente.getId();

            // calcula precios
            BigDecimal precioPorDia = vehiculo.getPrecioPorDia() == null ? BigDecimal.ZERO : vehiculo.getPrecioPorDia();
            BigDecimal precioTotal = precioPorDia.multiply(BigDecimal.valueOf(dias));

            // Construir Alquiler y asignar total mediante reflexión si es necesario
            Alquiler alquiler = new Alquiler();
            alquiler.setVehiculoId(vehiculo.getId());
            alquiler.setClienteId(clienteId);
            alquiler.setFechaInicio(inicio);
            alquiler.setFechaFin(fin);
            // intenta setPrecioDiario si existe
            try {
                alquiler.getClass().getMethod("setPrecioDiario", BigDecimal.class).invoke(alquiler, precioPorDia);
            } catch (NoSuchMethodException nsmd) {
                // ignora si no existe
            } catch (Exception e) { e.printStackTrace(); }

            // asignar el total con varios nombres posibles
            try {
                alquiler.getClass().getMethod("setTotal", BigDecimal.class).invoke(alquiler, precioTotal);
            } catch (NoSuchMethodException nsme) {
                try {
                    alquiler.getClass().getMethod("setPrecioTotal", BigDecimal.class).invoke(alquiler, precioTotal);
                } catch (NoSuchMethodException nsme2) {
                    try {
                        alquiler.getClass().getMethod("setPrecio", BigDecimal.class).invoke(alquiler, precioTotal);
                    } catch (Exception ignored) { /* no setter de total disponible */ }
                } catch (Exception e) { e.printStackTrace(); }
            } catch (Exception e) { e.printStackTrace(); }

            alquiler.setEstado("EN CURSO");
            // intenta setNotas / setObservaciones
            try {
                alquiler.getClass().getMethod("setNotas", String.class).invoke(alquiler, "Solicitud enviada desde el cliente.");
            } catch (NoSuchMethodException nm) {
                try { alquiler.getClass().getMethod("setObservaciones", String.class).invoke(alquiler, "Solicitud enviada desde el cliente."); }
                catch (Exception ignored) {}
            } catch (Exception e) { e.printStackTrace(); }

            // Mostrar confirmación con monto calculado
            String montoMostrar = (precioTotal != null) ? ("$" + precioTotal.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString()) : "N/A";
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmar reserva");
            confirm.setHeaderText("Confirma tu reserva");
            confirm.setContentText("Días: " + dias + "\nPrecio/día: " + (precioPorDia != null ? "$" + precioPorDia.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() : "N/A") +
                    "\n\nTotal a pagar: " + montoMostrar + "\n\n¿Deseas confirmar la reserva?");
            java.util.Optional<javafx.scene.control.ButtonType> resp = confirm.showAndWait();
            if (!resp.isPresent() || resp.get() != javafx.scene.control.ButtonType.OK) {
                alerta("Cancelado", "Reserva cancelada por el usuario.", Alert.AlertType.INFORMATION);
                return;
            }

            // 1) Intentar DAO
            AlquilerDAO dao = new AlquilerDAO();
            boolean exito = false;
            try {
                exito = dao.crearSolicitudAlquiler(alquiler);
                System.out.println("[VehiculosDetallerController] llamada a AlquilerDAO.crearSolicitudAlquiler result: " + exito);
            } catch (Exception ex) {
                System.err.println("[VehiculosDetallerController] AlquilerDAO.crearSolicitudAlquiler lanzó excepción: " + ex.getMessage());
                ex.printStackTrace();
                exito = false;
            }

            // 2) Si DAO falla, fallback robusto: construyo INSERT según columnas reales
            if (!exito) {
                System.out.println("[VehiculosDetallerController] Fallback: intento insertar directamente consultando columnas reales.");

                try (Connection cn = ConexionDB.getConnection()) {

                    List<String> cols = new ArrayList<>();
                    List<Object> values = new ArrayList<>();

                    // columnas básicas
                    if (hasColumn(cn, "alquileres", "vehiculo_id")) { cols.add("vehiculo_id"); values.add(vehiculo.getId()); }
                    if (hasColumn(cn, "alquileres", "cliente_id"))  { cols.add("cliente_id"); values.add(clienteId); }
                    else if (hasColumn(cn, "alquileres", "usuario_id")) { cols.add("usuario_id"); values.add(clienteId); }

                    // fechas (intento nombres comunes)
                    if (hasColumn(cn, "alquileres", "fecha_inicio")) { cols.add("fecha_inicio"); values.add(java.sql.Date.valueOf(inicio)); }
                    else if (hasColumn(cn, "alquileres", "fecha_inicio_renta")) { cols.add("fecha_inicio_renta"); values.add(java.sql.Date.valueOf(inicio)); }
                    else if (hasColumn(cn, "alquileres", "fecha_reserva")) { cols.add("fecha_reserva"); values.add(java.sql.Date.valueOf(inicio)); }

                    if (hasColumn(cn, "alquileres", "fecha_fin_estimada")) { cols.add("fecha_fin_estimada"); values.add(java.sql.Date.valueOf(fin)); }
                    else if (hasColumn(cn, "alquileres", "fecha_fin_renta")) { cols.add("fecha_fin_renta"); values.add(java.sql.Date.valueOf(fin)); }
                    else if (hasColumn(cn, "alquileres", "fecha_fin")) { cols.add("fecha_fin"); values.add(java.sql.Date.valueOf(fin)); }

                    // precios
                    if (hasColumn(cn, "alquileres", "precio_diario")) { cols.add("precio_diario"); values.add(precioPorDia); }
                    else if (hasColumn(cn, "alquileres", "precio_por_dia")) { cols.add("precio_por_dia"); values.add(precioPorDia); }

                    if (hasColumn(cn, "alquileres", "precio_total"))  { cols.add("precio_total"); values.add(precioTotal); }
                    else if (hasColumn(cn, "alquileres", "precio"))        { cols.add("precio"); values.add(precioTotal); }
                    else if (hasColumn(cn, "alquileres", "total"))        { cols.add("total"); values.add(precioTotal); }

                    // estado
                    if (hasColumn(cn, "alquileres", "estado")) { cols.add("estado"); values.add("EN CURSO"); }

                    // notas/observaciones
                    if (hasColumn(cn, "alquileres", "observaciones")) { cols.add("observaciones"); values.add("Solicitud enviada desde el cliente."); }
                    else if (hasColumn(cn, "alquileres", "notas")) { cols.add("notas"); values.add("Solicitud enviada desde el cliente."); }
                    else if (hasColumn(cn, "alquileres", "comentarios")) { cols.add("comentarios"); values.add("Solicitud enviada desde el cliente."); }

                    // fecha_creacion: si existe, lo marcamos con current_timestamp en SQL (no como parámetro)
                    boolean tieneFechaCreacion = hasColumn(cn, "alquileres", "fecha_creacion") || hasColumn(cn, "alquileres", "created_at");

                    if (cols.isEmpty()) {
                        System.err.println("[VehiculosDetallerController] No se identificaron columnas válidas para insertar en alquileres. Abortando fallback.");
                    } else {
                        StringBuilder sbCols = new StringBuilder();
                        StringBuilder sbVals = new StringBuilder();
                        for (int i = 0; i < cols.size(); i++) {
                            if (i > 0) { sbCols.append(", "); sbVals.append(", "); }
                            sbCols.append(cols.get(i));
                            sbVals.append("?");
                        }
                        String sql = "INSERT INTO alquileres (" + sbCols.toString() + (tieneFechaCreacion ? ", fecha_creacion" : "") + ") VALUES (" + sbVals.toString() + (tieneFechaCreacion ? ", current_timestamp" : "") + ")";
                        System.out.println("[VehiculosDetallerController] SQL fallback: " + sql);

                        try (PreparedStatement ps = cn.prepareStatement(sql)) {
                            for (int i = 0; i < values.size(); i++) {
                                Object val = values.get(i);
                                int idx = i + 1;
                                if (val instanceof Integer) ps.setInt(idx, (Integer) val);
                                else if (val instanceof BigDecimal) ps.setBigDecimal(idx, (BigDecimal) val);
                                else if (val instanceof java.sql.Date) ps.setDate(idx, (java.sql.Date) val);
                                else if (val instanceof java.sql.Timestamp) ps.setTimestamp(idx, (java.sql.Timestamp) val);
                                else if (val instanceof String) ps.setString(idx, (String) val);
                                else ps.setObject(idx, val);
                            }
                            int rows = ps.executeUpdate();
                            if (rows > 0) {
                                exito = true;
                                System.out.println("[VehiculosDetallerController] Insert directo OK, filas=" + rows);
                            } else {
                                System.err.println("[VehiculosDetallerController] Insert directo devolvió 0 filas afectadas.");
                            }
                        }
                    }
                } catch (SQLException sqe) {
                    sqe.printStackTrace();
                    System.err.println("[VehiculosDetallerController] Error en fallback insert: " + sqe.getMessage());
                    exito = false;
                }
            }

            if (exito) {
                alerta("Solicitud enviada", "El empleado revisará tu solicitud de renta.", Alert.AlertType.INFORMATION);
            } else {
                alerta("Error", "No se pudo completar la solicitud. Revisa la consola para más detalles.", Alert.AlertType.ERROR);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            alerta("Error", "Error al procesar la solicitud: " + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    void ConfirmarRenta(ActionEvent event) {
        if (vehiculo == null) { alerta("Error", "No hay vehículo cargado.", Alert.AlertType.ERROR); return; }
        if (!"DISPONIBLE".equalsIgnoreCase(vehiculo.getEstado())) { alerta("No disponible", "El vehículo no está disponible para renta.", Alert.AlertType.WARNING); return; }
        Usuario empleado = Sesion.getUsuarioActual();
        if (empleado == null || empleado.getRolId() != 2) { alerta("Permiso denegado", "Solo un empleado puede confirmar la renta.", Alert.AlertType.ERROR); return; }

        AlquilerDAO alquilerDAO = new AlquilerDAO();
        Alquiler solicitud = alquilerDAO.obtenerSolicitudEnCursoPorVehiculo(vehiculo.getId());

        if (solicitud == null) { alerta("Sin solicitud", "No existe una solicitud activa para este vehículo.", Alert.AlertType.WARNING); return; }

        boolean ok = alquilerDAO.confirmarRenta(solicitud.getId(), empleado.getId());
        if (!ok) { alerta("Error", "No se pudo confirmar la renta.", Alert.AlertType.ERROR); return; }

        VehiculosDAO vdao = new VehiculosDAO();
        vdao.actualizarEstadoVehiculo(vehiculo.getId(), "ALQUILADO");
        vehiculo.setEstado("ALQUILADO");
        lblEstado.setText("Estado: ALQUILADO");
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
