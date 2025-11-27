package com.example.proyecto_final_prograiii.DAO;

import com.example.proyecto_final_prograiii.DTO.AlquilerHistorialDTO;
import com.example.proyecto_final_prograiii.DTO.ClienteEstadisticaDTO;
import com.example.proyecto_final_prograiii.DTO.VehiculoEstadisticaDTO;
import com.example.proyecto_final_prograiii.config.ConexionDB;
import com.example.proyecto_final_prograiii.models.Alquiler;
import com.example.proyecto_final_prograiii.models.Pago;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AlquilerDAO {
    private Connection conexion;

    public AlquilerDAO() {
        conexion = ConexionDB.getConnection();
    }

    // -------------------- CREATE --------------------
    /**
     * Crea una solicitud / alquiler con pago en una transacción.
     * NOTA: NO modifica el estado del vehículo (no escribe 'ALQUILADO' en la tabla vehiculos).
     * Devuelve el id del alquiler creado (>0) o -1 en caso de error.
     */
    public int crearAlquilerConPago(Alquiler a, Pago p) {
        String sqlInsertAlquiler = """
            INSERT INTO alquileres
            (vehiculo_id, cliente_id, fecha_inicio, fecha_fin_estimada, precio_diario, costo_total, estado, notas)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            RETURNING id
        """;
        String sqlInsertPago = "INSERT INTO pagos (alquiler_id, monto, metodo) VALUES (?, ?, ?)";
        int alquilerId = -1;
        try {
            conexion.setAutoCommit(false);

            // 1) calcular costo_total si no viene
            BigDecimal costoTotal = a.getCostoTotal();
            if (costoTotal == null) {
                LocalDate inicio = a.getFechaInicio();
                LocalDate fin = a.getFechaFin();
                long dias = ChronoUnit.DAYS.between(inicio, fin) + 1;
                BigDecimal precioDiario = a.getPrecioDiario() != null ? a.getPrecioDiario() : BigDecimal.ZERO;
                costoTotal = precioDiario.multiply(BigDecimal.valueOf(dias));
            }

            // 2) Insertar alquiler (no tocamos vehiculos.estado aquí)
            try (PreparedStatement ps = conexion.prepareStatement(sqlInsertAlquiler)) {
                ps.setInt(1, a.getVehiculoId());
                ps.setInt(2, a.getClienteId());
                ps.setDate(3, Date.valueOf(a.getFechaInicio()));
                ps.setDate(4, a.getFechaFin() != null ? Date.valueOf(a.getFechaFin()) : null);
                ps.setBigDecimal(5, a.getPrecioDiario() != null ? a.getPrecioDiario() : BigDecimal.ZERO);
                ps.setBigDecimal(6, costoTotal);
                ps.setString(7, a.getEstado() != null ? a.getEstado() : "EN CURSO");
                ps.setString(8, a.getNotas());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        alquilerId = rs.getInt(1);
                    } else {
                        throw new SQLException("No se devolvió ID al insertar alquiler");
                    }
                }
            }

            // 3) Insertar pago
            try (PreparedStatement ps2 = conexion.prepareStatement(sqlInsertPago)) {
                ps2.setInt(1, alquilerId);
                ps2.setBigDecimal(2, p.getMonto());
                ps2.setString(3, p.getMetodo());
                ps2.executeUpdate();
            }

            // 4) NO actualizamos tabla vehiculos automáticamente aquí.
            // Si quieres marcar vehículo manualmente, existe el método marcarVehiculoEstado below.

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

    /**
     * Método opcional: marcar el estado del vehículo (uso administrativo).
     */
    public boolean marcarVehiculoEstado(int vehiculoId, String estado) {
        String sql = "UPDATE vehiculos SET estado = ? WHERE id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, estado);
            ps.setInt(2, vehiculoId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // -------------------- CHECK AVAILABILITY --------------------
    public boolean existeTraslape(int vehiculoId, LocalDate inicioNuevo, LocalDate finNuevo) {
        String sql = """
        SELECT COUNT(*) FROM alquileres
        WHERE vehiculo_id = ?
          AND estado IN ('EN CURSO','ALQUILADO')
          AND ( ? <= COALESCE(fecha_fin_real, fecha_fin_estimada) AND ? >= fecha_inicio )
        """;

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, vehiculoId);
            ps.setDate(2, Date.valueOf(inicioNuevo));
            ps.setDate(3, Date.valueOf(finNuevo));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true; // si hay error, mejor no permitir
    }

    // recupera las fechas ocupadas
    public List<LocalDate[]> obtenerRangosOcupados(int vehiculoId) {
        List<LocalDate[]> lista = new ArrayList<>();

        String sql = """
        SELECT fecha_inicio,
               COALESCE(fecha_fin_real, fecha_fin_estimada) AS fecha_fin
        FROM alquileres
        WHERE vehiculo_id = ?
          AND estado IN ('EN CURSO', 'ALQUILADO')
        """;

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, vehiculoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    java.sql.Date dInicio = rs.getDate("fecha_inicio");
                    java.sql.Date dFin = rs.getDate("fecha_fin");
                    java.time.LocalDate inicio = dInicio != null ? dInicio.toLocalDate() : null;
                    java.time.LocalDate fin = dFin != null ? dFin.toLocalDate() : null;
                    lista.add(new LocalDate[]{inicio, fin});
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return lista;
    }

    public Alquiler obtenerAlquilerActivoPorVehiculo(int vehiculoId) {
        String sql = """
        SELECT a.*, COALESCE(a.fecha_fin_real, a.fecha_fin_estimada) AS fecha_fin
        FROM alquileres a
        WHERE vehiculo_id = ?
          AND estado IN ('ALQUILADO', 'EN CURSO')
        ORDER BY fecha_inicio DESC
        LIMIT 1
        """;

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, vehiculoId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapResultSetToAlquiler(rs);
            }
        } catch (Exception e) { e.printStackTrace(); }
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
            COALESCE(a.fecha_fin_real, a.fecha_fin_estimada) AS fecha_fin,
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
                        rs.getDate("fecha_inicio") != null ? rs.getDate("fecha_inicio").toLocalDate() : null,
                        rs.getDate("fecha_fin") != null ? rs.getDate("fecha_fin").toLocalDate() : null,
                        rs.getBigDecimal("monto"),
                        rs.getString("metodo"),
                        rs.getString("estado")
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return lista;
    }

    public Alquiler obtenerAlquilerPorId(int alquilerId) {
        String sql = "SELECT * FROM alquileres WHERE id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, alquilerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapResultSetToAlquiler(rs);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public List<Map<String, Object>> obtenerEventosCalendario() {
        List<Map<String, Object>> lista = new ArrayList<>();

        String sql = """
        SELECT 
            a.id,
            CONCAT(v.modelo, ' (', v.placa, ')') AS vehiculo_nombre,
            c.nombre AS cliente_nombre,
            a.fecha_inicio,
            COALESCE(a.fecha_fin_real, a.fecha_fin_estimada) AS fecha_fin
        FROM alquileres a
        INNER JOIN vehiculos v ON v.id = a.vehiculo_id
        INNER JOIN clientes c ON c.id = a.cliente_id
        WHERE a.estado IN ('ALQUILADO', 'EN CURSO')
        ORDER BY a.fecha_inicio
    """;

        try (PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> evento = new HashMap<>();
                evento.put("titulo", rs.getString("vehiculo_nombre") + " — " + rs.getString("cliente_nombre"));
                evento.put("inicio", rs.getDate("fecha_inicio").toLocalDate());
                evento.put("fin", rs.getDate("fecha_fin").toLocalDate());

                lista.add(evento);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return lista;
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

        a.setFechaInicio(rs.getDate("fecha_inicio") != null ? rs.getDate("fecha_inicio").toLocalDate() : null);

        // prefer alias 'fecha_fin' si viene; si no, las columnas reales
        java.sql.Date df = null;
        try {
            df = rs.getDate("fecha_fin");
        } catch (SQLException ignored) {}
        if (df == null) {
            if (hasColumn(rs, "fecha_fin_real")) df = rs.getDate("fecha_fin_real");
            else if (hasColumn(rs, "fecha_fin_estimada")) df = rs.getDate("fecha_fin_estimada");
        }
        a.setFechaFin(df != null ? df.toLocalDate() : null);

        if (hasColumn(rs, "precio_diario")) a.setPrecioDiario(rs.getBigDecimal("precio_diario"));
        if (hasColumn(rs, "costo_total")) a.setCostoTotal(rs.getBigDecimal("costo_total"));
        if (hasColumn(rs, "estado")) a.setEstado(rs.getString("estado"));
        if (hasColumn(rs, "notas")) a.setNotas(rs.getString("notas"));

        Timestamp ts = hasColumn(rs, "fecha_creacion") ? rs.getTimestamp("fecha_creacion") : null;
        a.setFechaCreacion(ts != null ? ts.toLocalDateTime() : null);

        return a;
    }

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
    public List<VehiculoEstadisticaDTO> obtenerEstadisticasVehiculos() {
        List<VehiculoEstadisticaDTO> lista = new ArrayList<>();

        String sql = """
        SELECT 
            v.modelo AS nombreVehiculo,
            v.placa,
            v.estado,
            COUNT(a.id) AS cantidadRentas,
            COALESCE(SUM(a.costo_total), 0) AS ganancias
        FROM vehiculos v
        LEFT JOIN alquileres a ON a.vehiculo_id = v.id
        GROUP BY v.id
        ORDER BY cantidadRentas DESC;
    """;

        try (PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                VehiculoEstadisticaDTO dto = new VehiculoEstadisticaDTO(
                        rs.getString("nombreVehiculo"),
                        rs.getString("placa"),
                        rs.getString("estado"),
                        rs.getInt("cantidadRentas"),
                        rs.getBigDecimal("ganancias")
                );

                lista.add(dto);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return lista;
    }
    public List<ClienteEstadisticaDTO> obtenerEstadisticasClientes() {
        List<ClienteEstadisticaDTO> lista = new ArrayList<>();

        String sql = """
        SELECT 
            CONCAT(c.nombre, ' ', c.apellido) AS nombreCompleto,
            u.nombre_usuario AS nombreUsuario,
            COUNT(a.id) AS cantidadRentas,
            COALESCE(SUM(a.costo_total), 0) AS importeTotal
        FROM clientes c
        INNER JOIN usuarios u ON u.id = c.usuario_id
        LEFT JOIN alquileres a ON a.cliente_id = c.id
        GROUP BY c.id, u.nombre_usuario
        ORDER BY cantidadRentas DESC
    """;

        try (PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new ClienteEstadisticaDTO(
                        rs.getString("nombreCompleto"),
                        rs.getString("nombreUsuario"),
                        rs.getInt("cantidadRentas"),
                        rs.getBigDecimal("importeTotal")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return lista;
    }
}
