package com.example.proyecto_final_prograiii.DAO;


import com.example.proyecto_final_prograiii.DTO.AlquilerHistorialDTO;
import com.example.proyecto_final_prograiii.config.ConexionDB;
import com.example.proyecto_final_prograiii.models.Alquiler;
import com.example.proyecto_final_prograiii.models.Pago;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class AlquilerDAO {
    private Connection conexion;

    public AlquilerDAO() {
        conexion = ConexionDB.getConnection();
    }

    // -------------------- CREATE --------------------
    /**
     * Crea una solicitud (sin convertirla automáticamente a alquiler).
     * Retorna id generado o -1 en caso de error.
     *
     * Usa fecha_fin_estimada (columna que existe en tu tabla).
     */


    // ---------------------
    // Método transaccional: crear alquiler + pago + marcar vehículo alquilado
    // Devuelve el id del alquiler creado (>0) o -1 en caso de error.
    // ---------------------
    public int crearAlquilerConPago(Alquiler a, Pago p) {
        String sqlInsertAlquiler = """
            INSERT INTO alquileres
            (vehiculo_id, cliente_id, fecha_inicio, fecha_fin, precio_diario, costo_total, estado, notas)
            VALUES (?, ?, ?, ?, ?, ?, 'ALQUILADO', ?)
            RETURNING id
        """;
        String sqlInsertPago = "INSERT INTO pagos (alquiler_id, monto, metodo) VALUES (?, ?, ?)";
        int alquilerId = -1;
        try {
            // Iniciamos transacción
            conexion.setAutoCommit(false);

            // 1) calcular costo_total si no viene en el objeto Alquiler
            BigDecimal costoTotal = a.getCostoTotal();
            if (costoTotal == null) {
                LocalDate inicio = a.getFechaInicio();
                LocalDate fin = a.getFechaFin();
                long dias = ChronoUnit.DAYS.between(inicio, fin) + 1;
                BigDecimal precioDiario = a.getPrecioDiario() != null ? a.getPrecioDiario() : BigDecimal.ZERO;
                costoTotal = precioDiario.multiply(BigDecimal.valueOf(dias));
            }

            // 2) Insertar alquiler (estado ALQUILADO directamente)
            try (PreparedStatement ps = conexion.prepareStatement(sqlInsertAlquiler)) {
                ps.setInt(1, a.getVehiculoId());
                ps.setInt(2, a.getClienteId());
                ps.setDate(3, Date.valueOf(a.getFechaInicio()));
                ps.setDate(4, a.getFechaFin() != null ? Date.valueOf(a.getFechaFin()) : null);
                ps.setBigDecimal(5, a.getPrecioDiario() != null ? a.getPrecioDiario() : BigDecimal.ZERO);
                ps.setBigDecimal(6, costoTotal);
                ps.setString(7, a.getNotas());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        alquilerId = rs.getInt(1);
                    } else {
                        throw new SQLException("No se devolvió ID al insertar alquiler");
                    }
                }
            }

            // 3) Insertar pago (monto y metodo obligatorios para este flujo)
            try (PreparedStatement ps2 = conexion.prepareStatement(sqlInsertPago)) {
                ps2.setInt(1, alquilerId);
                ps2.setBigDecimal(2, p.getMonto());
                ps2.setString(3, p.getMetodo());
                ps2.executeUpdate();
            }

            // 4) Actualizar estado del vehículo a ALQUILADO
            String sqlUpdateVeh = "UPDATE vehiculos SET estado = 'ALQUILADO' WHERE id = ?";
            try (PreparedStatement ps3 = conexion.prepareStatement(sqlUpdateVeh)) {
                ps3.setInt(1, a.getVehiculoId());
                ps3.executeUpdate();
            }

            // 5) commit
            conexion.commit();
            conexion.setAutoCommit(true);
            return alquilerId;

        } catch (Exception e) {
            e.printStackTrace();
            try {
                conexion.rollback();
                conexion.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return -1;
        }
    }
    // -------------------- CHECK AVAILABILITY --------------------
    /**
     * Devuelve true si existe algún alquiler activo (EN CURSO o ALQUILADO)
     * para el mismo vehículo que solapa con el rango [inicioNuevo, finNuevo].
     */
    public boolean existeTraslape(int vehiculoId, LocalDate inicioNuevo, LocalDate finNuevo) {
        String sql = """
        SELECT COUNT(*) FROM alquileres
        WHERE vehiculo_id = ?
          AND estado IN ('EN CURSO','ALQUILADO')
          AND ( ? <= fecha_fin AND ? >= fecha_inicio )
    """;

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, vehiculoId);
            // condicion: inicioNuevo <= fecha_fin AND finNuevo >= fecha_inicio
            ps.setDate(2, Date.valueOf(finNuevo));   // ? <= fecha_fin  -> finNuevo <= fecha_fin  (we'll use symmetrical pass)
            ps.setDate(3, Date.valueOf(inicioNuevo)); // ? >= fecha_inicio -> inicioNuevo >= fecha_inicio
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // si hay error, por seguridad impedir la reserva
        return true;
    }
    //recupera las fechas en las que estan rentados los carros
    public List<LocalDate[]> obtenerRangosOcupados(int vehiculoId) {
        List<LocalDate[]> lista = new ArrayList<>();

        String sql = """
        SELECT fecha_inicio, fecha_fin
        FROM alquileres
        WHERE vehiculo_id = ?
        AND estado IN ('EN CURSO', 'ALQUILADO')
    """;

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, vehiculoId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                LocalDate inicio = rs.getDate("fecha_inicio").toLocalDate();
                LocalDate fin = rs.getDate("fecha_fin").toLocalDate();
                lista.add(new LocalDate[]{inicio, fin});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return lista;
    }
    public Alquiler obtenerAlquilerActivoPorVehiculo(int vehiculoId) {
        String sql = """
        SELECT *
        FROM alquileres
        WHERE vehiculo_id = ?
        AND estado IN ('ALQUILADO', 'EN CURSO')
        ORDER BY fecha_inicio DESC
        LIMIT 1
    """;

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, vehiculoId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapResultSetToAlquiler(rs);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }



    // -------------------- READ / LIST --------------------
    public List<AlquilerHistorialDTO> obtenerHistorial() {
        List<AlquilerHistorialDTO> lista = new ArrayList<>();

        String sql = """
        SELECT
            a.id AS alquiler_id,
            a.vehiculo_id,
            CONCAT(v.modelo, ' (', v.placa, ')') AS vehiculo,
            c.nombre AS cliente,
            a.fecha_inicio,
            a.fecha_fin,
            COALESCE(p.monto, 0) AS monto,
            p.metodo,
            a.estado
        FROM alquileres a
        INNER JOIN vehiculos v ON v.id = a.vehiculo_id
        INNER JOIN clientes c ON c.id = a.cliente_id
        LEFT JOIN pagos p ON p.alquiler_id = a.id
        WHERE a.estado IN ('ALQUILADO', 'FINALIZADO')
        ORDER BY a.fecha_inicio DESC
    """;

        try (PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                lista.add(new AlquilerHistorialDTO(
                        rs.getInt("alquiler_id"),
                        rs.getInt("vehiculo_id"),
                        rs.getString("vehiculo"),
                        rs.getString("cliente"),
                        rs.getDate("fecha_inicio").toLocalDate(),
                        rs.getDate("fecha_fin").toLocalDate(),
                        rs.getBigDecimal("monto"),
                        rs.getString("metodo"),
                        rs.getString("estado")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return lista;
    }

    public Alquiler obtenerAlquilerPorId(int alquilerId) {
        String sql = "SELECT * FROM alquileres WHERE id = ?";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, alquilerId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapResultSetToAlquiler(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    // -------------------- DELETE --------------------------
    public boolean eliminarAlquilerCompleto(int alquilerId) {
        String delPagos = "DELETE FROM pagos WHERE alquiler_id = ?";
        String delAlquiler = "DELETE FROM alquileres WHERE id = ?";

        try {
            conexion.setAutoCommit(false);

            try (PreparedStatement ps1 = conexion.prepareStatement(delPagos)) {
                ps1.setInt(1, alquilerId);
                ps1.executeUpdate();
            }

            try (PreparedStatement ps2 = conexion.prepareStatement(delAlquiler)) {
                ps2.setInt(1, alquilerId);
                ps2.executeUpdate();
            }

            conexion.commit();
            conexion.setAutoCommit(true);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            try { conexion.rollback(); } catch (Exception ignored) {}
            return false;
        }
    }


    // -------------------- HELPERS --------------------
    private Alquiler mapResultSetToAlquiler(ResultSet rs) throws SQLException {

        Alquiler a = new Alquiler();

        a.setId(rs.getInt("id"));
        a.setVehiculoId(rs.getInt("vehiculo_id"));
        a.setClienteId(rs.getInt("cliente_id"));

        // ✔ fecha inicio
        a.setFechaInicio(
                rs.getDate("fecha_inicio") != null
                        ? rs.getDate("fecha_inicio").toLocalDate()
                        : null
        );

        // ✔ fecha fin (LA ÚNICA QUE EXISTE AHORA)
        a.setFechaFin(
                rs.getDate("fecha_fin") != null
                        ? rs.getDate("fecha_fin").toLocalDate()
                        : null
        );

        a.setPrecioDiario(rs.getBigDecimal("precio_diario"));
        a.setCostoTotal(rs.getBigDecimal("costo_total"));
        a.setEstado(rs.getString("estado"));
        a.setNotas(rs.getString("notas"));

        Timestamp ts = rs.getTimestamp("fecha_creacion");
        a.setFechaCreacion(ts != null ? ts.toLocalDateTime() : null);

        return a;
    }


    // helper para saber si ResultSet tiene la columna (evita SQLException)
    private boolean hasColumn(ResultSet rs, String columnName) {
        try {
            ResultSetMetaData md = rs.getMetaData();
            int cols = md.getColumnCount();
            for (int i = 1; i <= cols; i++) {
                if (md.getColumnLabel(i).equalsIgnoreCase(columnName) ||
                        md.getColumnName(i).equalsIgnoreCase(columnName)) return true;
            }
        } catch (SQLException e) {
            return false;
        }
        return false;
    }


    public Alquiler obtenerSolicitudEnCursoPorVehiculo(int vehiculoId) {
        String sql = """
            SELECT * FROM alquileres 
            WHERE vehiculo_id = ? 
            AND estado = 'EN CURSO'
            LIMIT 1
        """;

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, vehiculoId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAlquiler(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error en obtenerSolicitudEnCursoPorVehiculo: " + e.getMessage());
        }

        return null;
    }

    /**
     * Confirma una renta (método administrativo).
     * Calcula costo_total usando COALESCE(fecha_fin_real, fecha_fin_estimada) para soportar ambos campos.
     *
     * CORRECCIÓN: ya no usamos DATE_PART sobre un integer — usamos la resta directa de dates.
     */


    // -------------------- FIN CLASS --------------------
}
