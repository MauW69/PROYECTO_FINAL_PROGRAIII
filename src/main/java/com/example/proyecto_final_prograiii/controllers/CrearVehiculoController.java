package com.example.proyecto_final_prograiii.controllers;

import com.example.proyecto_final_prograiii.DAO.TipoVehiculosDAO;
import com.example.proyecto_final_prograiii.DAO.VehiculosDAO;
import com.example.proyecto_final_prograiii.models.TipoVehiculo;
import com.example.proyecto_final_prograiii.models.Vehiculo;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxListCell;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.regex.Pattern;

public class CrearVehiculoController {

    @FXML private ComboBox<TipoVehiculo> cbTipoVehiculo; // reemplazo del tfTipoId
    @FXML private TextField tfModelo;
    @FXML private TextField tfPlaca;
    @FXML private TextField tfYear;
    @FXML private TextField tfColor;
    @FXML private TextField tfKm;
    @FXML private ComboBox<String> cbEstado;
    @FXML private TextField tfPrecio;
    @FXML private Button btnAceptar;
    @FXML private Button btnCancelar;
    @FXML private Button btnSeleccionarImagen;
    @FXML private Label lblImagenSeleccionada;

    private final VehiculosDAO dao = new VehiculosDAO();
    private final TipoVehiculosDAO tipoDao = new TipoVehiculosDAO();
    private File archivoImagen;

    // Validaciones
    // ahora permite letras y números, exactamente 7 caracteres
    private final Pattern placaPattern = Pattern.compile("^[A-Za-z0-9]{7}$");

    @FXML
    public void initialize() {
        // estilos por defecto limpios
        clearFieldStyle(tfModelo, tfPlaca, tfYear, tfKm, tfPrecio, cbTipoVehiculo);

        // llenar estados
        cbEstado.getItems().addAll("disponible", "mantenimiento", "fuera de servicio");
        cbEstado.setValue("disponible");

        // POBLAR TIPOS (desde DB)
        try {
            List<TipoVehiculo> tipos = tipoDao.listarTipos();
            if (tipos != null && !tipos.isEmpty()) {
                cbTipoVehiculo.getItems().addAll(tipos);
                // prompt (placeholder)
                cbTipoVehiculo.setPromptText("Seleccione un tipo...");
                // mostrar solo el nombre en la UI
                cbTipoVehiculo.setConverter(new StringConverter<>() {
                    @Override
                    public String toString(TipoVehiculo object) {
                        return object == null ? "" : object.getNombre();
                    }
                    @Override
                    public TipoVehiculo fromString(String string) {
                        return null; // no se usa edición por texto
                    }
                });

                // cellFactory para mostrar tooltip con descripción en cada item
                cbTipoVehiculo.setCellFactory(new Callback<javafx.scene.control.ListView<TipoVehiculo>, javafx.scene.control.ListCell<TipoVehiculo>>() {
                    @Override
                    public javafx.scene.control.ListCell<TipoVehiculo> call(javafx.scene.control.ListView<TipoVehiculo> lv) {
                        return new javafx.scene.control.ListCell<>() {
                            @Override
                            protected void updateItem(TipoVehiculo item, boolean empty) {
                                super.updateItem(item, empty);
                                if (empty || item == null) {
                                    setText(null);
                                    setTooltip(null);
                                } else {
                                    setText(item.getNombre());
                                    if (item.getDescripcion() != null && !item.getDescripcion().isBlank()) {
                                        Tooltip t = new Tooltip(item.getDescripcion());
                                        setTooltip(t);
                                    } else {
                                        setTooltip(null);
                                    }
                                }
                            }
                        };
                    }
                });

                // botón del combo
                cbTipoVehiculo.setButtonCell(new javafx.scene.control.ListCell<>() {
                    @Override
                    protected void updateItem(TipoVehiculo item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(empty || item == null ? "" : item.getNombre());
                    }
                });
            } else {
                cbTipoVehiculo.setPromptText("No hay tipos disponibles");
            }
        } catch (Exception e) {
            e.printStackTrace();
            cbTipoVehiculo.setPromptText("Error al cargar tipos");
        }

        // Evitar que en placa se ingresen caracteres no alfanuméricos: solo letras y dígitos
        // además convertir a mayúsculas y limitar longitud a 7.
        tfPlaca.textProperty().addListener((obs, oldV, newV) -> {
            if (newV == null) return;

            // filtrar solo alfanuméricos
            String filtered = newV.replaceAll("[^A-Za-z0-9]", "");

            // convertir a mayúsculas para uniformidad (opcional)
            filtered = filtered.toUpperCase();

            if (filtered.length() > 7) {
                // evita que supere 7: restaurar al valor anterior válido
                tfPlaca.setText(oldV); // restaurar
                Platform.runLater(() -> {
                    Alert a = new Alert(Alert.AlertType.INFORMATION, "Máximo 7 caracteres para la placa.", ButtonType.OK);
                    a.setHeaderText(null);
                    a.showAndWait();
                });
                return;
            }

            // si el filtrado cambió el texto, lo aplicamos
            if (!filtered.equals(newV)) {
                tfPlaca.setText(filtered);
            }
        });
    }

    @FXML
    public void onSeleccionarImagen() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Seleccionar imagen del vehículo");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg"));

        File archivo = fc.showOpenDialog(btnSeleccionarImagen.getScene().getWindow());
        if (archivo != null) {
            archivoImagen = archivo;
            lblImagenSeleccionada.setText(archivo.getName());
        }
    }

    @FXML
    public void onAceptar() {
        try {
            // LIMPIAR estilos previos
            clearFieldStyle(tfModelo, tfPlaca, tfYear, tfKm, tfPrecio, cbTipoVehiculo);

            // Validaciones principales
            if (!validarCamposBasicos()) return;

            // Obtener tipo seleccionado y su id
            TipoVehiculo tipoSeleccionado = cbTipoVehiculo.getValue();
            if (tipoSeleccionado == null) {
                markFieldInvalid(cbTipoVehiculo);
                alerta("Debe seleccionar un tipo de vehículo");
                return;
            }
            int tipoId = tipoSeleccionado.getId();

            // Valores ya validados por validarCamposBasicos()
            String modelo = tfModelo.getText().trim();
            String placa = tfPlaca.getText().trim();
            int year = tfYear.getText().trim().isEmpty() ? 0 : Integer.parseInt(tfYear.getText().trim());
            String color = tfColor.getText().trim();
            int km = tfKm.getText().trim().isEmpty() ? 0 : Integer.parseInt(tfKm.getText().trim());
            String estado = cbEstado.getValue();
            String precioStr = tfPrecio.getText().trim();

            BigDecimal precio = null;
            if (!precioStr.isEmpty()) {
                precio = new BigDecimal(precioStr);
            }

            // Construir Vehiculo
            Vehiculo v = new Vehiculo();
            v.setTipoVehiculoId(tipoId);
            v.setModelo(modelo);
            v.setPlaca(placa);
            v.setYear(year);
            v.setColor(color);
            v.setKilometraje(km);
            v.setEstado(estado == null ? "disponible" : estado);
            v.setFechaCreacion(LocalDateTime.now());
            v.setPrecioPorDia(precio);

            // --- COPIAR IMAGEN Y GUARDAR RUTA (DEBUG) ---
            if (archivoImagen != null) {
                try {
                    File carpeta = new File("imagenesVehiculos");
                    if (!carpeta.exists()) carpeta.mkdirs();

                    String nuevoNombre = System.currentTimeMillis() + "_" + archivoImagen.getName();
                    File destino = new File(carpeta, nuevoNombre);

                    // copiar archivo a la carpeta local de la app
                    Files.copy(archivoImagen.toPath(), destino.toPath(), StandardCopyOption.REPLACE_EXISTING);

                    // DEBUG: ruta absoluta que se va a guardar en el objeto Vehiculo y BD
                    System.out.println("[DEBUG] Imagen copiada a: " + destino.getAbsolutePath());

                    // guardar ruta absoluta
                    v.setImagen(destino.getAbsolutePath());

                    // DEBUG: confirmar valor final en el objeto antes de insertar en BD
                    System.out.println("[DEBUG] Vehiculo preparado - imagen: " + v.getImagen());

                } catch (Exception e) {
                    e.printStackTrace();
                    alerta("No se pudo guardar la imagen: " + e.getMessage());
                    return;
                }
            } else {
                System.out.println("[DEBUG] No se seleccionó imagen para este vehículo (archivoImagen == null).");
            }

            boolean ok = dao.crearVehiculo(v);
            if (ok) {
                new Alert(Alert.AlertType.INFORMATION, "Vehículo creado correctamente").showAndWait();
                cerrarVentana();
            } else {
                alerta("No se pudo crear el vehículo (revisa la conexión/DAO)");
            }
        } catch (NumberFormatException nfe) {
            alerta("Asegúrate de que los campos numéricos sean válidos");
        } catch (Exception ex) {
            ex.printStackTrace();
            alerta("Error: " + ex.getMessage());
        }
    }

    @FXML
    public void onCancelar() {
        cerrarVentana();
    }

    /* ----------------------- Helpers de validación ----------------------- */

    /** valida todos los campos y marca los que fallen. devuelve true si todo OK. */
    private boolean validarCamposBasicos() {
        StringBuilder errores = new StringBuilder();

        String modelo = tfModelo.getText().trim();
        String placa = tfPlaca.getText().trim();
        String yearStr = tfYear.getText().trim();
        String kmStr = tfKm.getText().trim();
        String precioStr = tfPrecio.getText().trim();

        boolean ok = true;

        // modelo obligatorio
        if (modelo.isEmpty()) {
            errores.append("- Modelo es obligatorio.\n");
            markFieldInvalid(tfModelo);
            ok = false;
        }

        // placa: exactamente 7 caracteres alfanuméricos
        if (!placaPattern.matcher(placa).matches()) {
            errores.append("- Placa inválida: debe contener exactamente 7 caracteres (letras y/o números).\n");
            markFieldInvalid(tfPlaca);
            ok = false;
        }

        // year: opcional, pero si se ingresa debe ser un entero razonable (por ejemplo >1900 y <= currentYear+1)
        if (!yearStr.isEmpty()) {
            try {
                int y = Integer.parseInt(yearStr);
                int cy = LocalDateTime.now().getYear();
                if (y < 1900 || y > cy + 1) {
                    errores.append("- Año inválido.\n");
                    markFieldInvalid(tfYear);
                    ok = false;
                }
            } catch (NumberFormatException e) {
                errores.append("- Año debe ser número.\n");
                markFieldInvalid(tfYear);
                ok = false;
            }
        }

        // kilometraje: no negativo
        if (!kmStr.isEmpty()) {
            try {
                int km = Integer.parseInt(kmStr);
                if (km < 0) {
                    errores.append("- Kilometraje no puede ser negativo.\n");
                    markFieldInvalid(tfKm);
                    ok = false;
                }
            } catch (NumberFormatException e) {
                errores.append("- Kilometraje debe ser un número entero.\n");
                markFieldInvalid(tfKm);
                ok = false;
            }
        }

        // precio: no negativo (puede ser decimal)
        if (!precioStr.isEmpty()) {
            try {
                BigDecimal p = new BigDecimal(precioStr);
                if (p.compareTo(BigDecimal.ZERO) < 0) {
                    errores.append("- Precio no puede ser negativo.\n");
                    markFieldInvalid(tfPrecio);
                    ok = false;
                }
            } catch (NumberFormatException e) {
                errores.append("- Precio inválido (use punto decimal si aplica).\n");
                markFieldInvalid(tfPrecio);
                ok = false;
            }
        }

        if (!ok) {
            alerta(errores.toString());
        }
        return ok;
    }

    private void markFieldInvalid(Control c) {
        c.setStyle("-fx-border-color: #e53935; -fx-border-width: 1.5;"); // rojo
    }

    private void clearFieldStyle(Control... controls) {
        for (Control c : controls) {
            c.setStyle(null);
        }
    }

    /* -------------------------------------------------------------------- */

    private void cerrarVentana() {
        Stage st = (Stage) btnCancelar.getScene().getWindow();
        st.close();
    }

    private void alerta(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING, msg);
        a.setHeaderText(null);
        a.showAndWait();
    }
}
