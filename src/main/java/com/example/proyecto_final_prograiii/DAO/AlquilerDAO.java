package com.example.proyecto_final_prograiii.DAO;

import com.example.proyecto_final_prograiii.DTO.AlquilerHistorialDTO;
import com.example.proyecto_final_prograiii.DTO.AlquilerSolicitudDTO;
import com.example.proyecto_final_prograiii.config.ConexionDB;
import com.example.proyecto_final_prograiii.models.Alquiler;
import com.example.proyecto_final_prograiii.models.Pago;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    public int crearSolicitudAlquiler(Alquiler a) {
        String sql = """
            INSERT INTO alquileres
            (vehiculo_id, cliente_id, fecha_inicio, fecha_fin_estimada, precio_diario, estado, notas)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            RETURNING id
        """;

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, a.getVehiculoId());
            ps.setInt(2, a.getClienteId());
            ps.setDate(3, Date.valueOf(a.getFechaInicio()));
            ps.setDate(4, a.getFechaFin() != null ? Date.valueOf(a.getFechaFin()) : null); // fecha_fin_estimada
            ps.setBigDecimal(5, a.getPrecioDiario());
            ps.setString(6, a.getEstado());
            ps.setString(7, a.getNotas());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1); // retorna el ID del alquiler
            }

        } catch (SQLException e) {
            System.out.println("Error al crear la solicitud: " + e.getMessage());
        }

        return -1; // fallo
    }

    // -------------------- CHECK AVAILABILITY --------------------
    /**
     * Comprueba disponibilidad (usa la conexión compartida).
     * Devuelve true si no hay solapamientos con estados EN CURSO, RESERVADO o ALQUILADO.
     *
     * Usa fecha_fin_estimada para el chequeo (la columna que tienes en la BD).
     */
    public boolean isVehiculoDisponible(int vehiculoId, LocalDate nuevoInicio, LocalDate nuevoFin) {
        String sql = """
            SELECT COUNT(1) AS cnt FROM alquileres
            WHERE vehiculo_id = ?
              AND estado IN ('EN CURSO','RESERVADO','ALQUILADO')
              AND NOT (fecha_fin_estimada < ? OR fecha_inicio > ?)
        """;

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, vehiculoId);
            ps.setDate(2, Date.valueOf(nuevoInicio));
            ps.setDate(3, Date.valueOf(nuevoFin));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int cnt = rs.getInt("cnt");
                    return cnt == 0;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error isVehiculoDisponible: " + e.getMessage());
        }
        return false;
    }

    // -------------------- READ / LIST --------------------
    public List<AlquilerSolicitudDTO> obtenerSolicitudesActivas() {
        List<AlquilerSolicitudDTO> lista = new ArrayList<>();

        String sql = """
            SELECT a.id,
                   a.vehiculo_id,
                   c.nombre AS cliente,
                   CONCAT(v.modelo, ' (', v.placa, ')') AS vehiculo,
                   a.fecha_inicio,
                   a.precio_diario,
                   a.estado
            FROM alquileres a
            INNER JOIN clientes c ON a.cliente_id = c.id
            INNER JOIN vehiculos v ON a.vehiculo_id = v.id
            WHERE a.estado = 'EN CURSO'
            ORDER BY a.fecha_inicio DESC
        """;

        try (PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new AlquilerSolicitudDTO(
                        rs.getInt("id"),
                        rs.getInt("vehiculo_id"),
                        rs.getString("cliente"),
                        rs.getString("vehiculo"),
                        rs.getDate("fecha_inicio").toLocalDate(),
                        rs.getBigDecimal("precio_diario"),
                        rs.getString("estado")
                ));
            }

        } catch (SQLException e) {
            System.out.println("Error obtenerSolicitudesActivas: " + e.getMessage());
        }

        return lista;
    }

    public List<AlquilerHistorialDTO> obtenerHistorial() {

        String sql = """
        SELECT a.id,
               CONCAT(v.modelo, ' (', v.placa, ')') AS vehiculo,
               a.fecha_inicio,
               -- exponer fecha_fin_estimada como fecha_fin para compatibilidad con DTO
               a.fecha_fin_estimada AS fecha_fin,
               COALESCE(p.monto, 0) AS total_pagado,
               p.metodo,
               a.estado
        FROM alquileres a
        INNER JOIN vehiculos v ON a.vehiculo_id = v.id
        LEFT JOIN pagos p ON p.alquiler_id = a.id
        WHERE a.estado IN ('ALQUILADO', 'FINALIZADO', 'CANCELADO')
        ORDER BY a.fecha_inicio DESC
    """;

        List<AlquilerHistorialDTO> lista = new ArrayList<>();

        try (PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new AlquilerHistorialDTO(
                        rs.getInt("id"),
                        rs.getString("vehiculo"),
                        rs.getDate("fecha_inicio") != null ? rs.getDate("fecha_inicio").toLocalDate() : null,
                        rs.getDate("fecha_fin") != null ? rs.getDate("fecha_fin").toLocalDate() : null,
                        rs.getBigDecimal("total_pagado"),
                        rs.getString("metodo"),
                        rs.getString("estado")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return lista;
    }


    public List<Alquiler> obtenerSolicitudesFinalizadas() {
        List<Alquiler> lista = new ArrayList<>();
        String sql = "SELECT * FROM alquileres WHERE estado IN ('FINALIZADO','CANCELADO') ORDER BY fecha_creacion DESC";
        try (PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Alquiler a = mapResultSetToAlquiler(rs);
                lista.add(a);
            }
        } catch (SQLException e) {
            System.out.println("Error obtenerSolicitudesFinalizadas: " + e.getMessage());
        }
        return lista;
    }

    // -------------------- UPDATE / CANCEL / FINISH --------------------
    public boolean cancelarSolicitud(int alquilerId) {

        Integer vehiculoId = null;

        //obtener ID del vehiculo asociado
        try (PreparedStatement ps = conexion.prepareStatement("SELECT vehiculo_id FROM alquileres WHERE id = ?")) {
            ps.setInt(1, alquilerId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                vehiculoId = rs.getInt("vehiculo_id");
            }
        } catch (Exception e) {
            System.out.println("Error obteniendo vehiculo_id: " + e.getMessage());
        }

        // Eliminar pago asociado
        try (PreparedStatement ps = conexion.prepareStatement("DELETE FROM pagos WHERE alquiler_id = ?")) {
            ps.setInt(1, alquilerId);
            ps.executeUpdate();
        } catch (Exception e) {
            System.out.println("Error eliminando pago asociado: " + e.getMessage());
        }

        // Cambiar estado del alquiler
        boolean ok = false;
        try (PreparedStatement ps = conexion.prepareStatement("UPDATE alquileres SET estado = 'CANCELADO' WHERE id = ?")) {
            ps.setInt(1, alquilerId);
            ok = ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }

        //cambiar el vehículo a DISPONIBLE
        if (ok && vehiculoId != null) {
            new VehiculosDAO().actualizarEstadoVehiculo(vehiculoId, "DISPONIBLE");
        }
        return ok;
    }

    /**
     * Finaliza un alquiler: guarda fecha_fin_real y costo_total.
     * Antes usaba fecha_fin que no existe; ahora se usa fecha_fin_real.
     */
    public boolean finalizar(int id, LocalDate fechaFinReal, BigDecimal costoTotal) {
        String sql = "UPDATE alquileres SET estado = 'FINALIZADO', fecha_fin_real = ?, costo_total = ? WHERE id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(fechaFinReal));
            ps.setBigDecimal(2, costoTotal);
            ps.setInt(3, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error finalizar alquiler: " + e.getMessage());
            return false;
        }
    }

    // -------------------- HELPERS --------------------
    private Alquiler mapResultSetToAlquiler(ResultSet rs) throws SQLException {
        Alquiler a = new Alquiler();
        a.setId(rs.getInt("id"));
        a.setVehiculoId(rs.getInt("vehiculo_id"));
        a.setClienteId(rs.getInt("cliente_id"));
        a.setEmpleadoInicioId(rs.getObject("empleado_inicio_id") != null ? rs.getInt("empleado_inicio_id") : null);
        a.setEmpleadoFinId(rs.getObject("empleado_fin_id") != null ? rs.getInt("empleado_fin_id") : null);
        a.setFechaInicio(rs.getDate("fecha_inicio") != null ? rs.getDate("fecha_inicio").toLocalDate() : null);

        // leer fecha_fin_real si existe y no es null; si no, usar fecha_fin_estimada
        LocalDate fechaFin = null;
        try {
            if (hasColumn(rs, "fecha_fin_real") && rs.getDate("fecha_fin_real") != null)
                fechaFin = rs.getDate("fecha_fin_real").toLocalDate();
            else if (hasColumn(rs, "fecha_fin_estimada") && rs.getDate("fecha_fin_estimada") != null)
                fechaFin = rs.getDate("fecha_fin_estimada").toLocalDate();
        } catch (SQLException ignored) { /* defensivo */ }
        a.setFechaFin(fechaFin);

        a.setPrecioDiario(rs.getBigDecimal("precio_diario"));
        a.setCostoTotal(rs.getBigDecimal("costo_total"));
        a.setEstado(rs.getString("estado"));
        a.setNotas(rs.getString("notas"));
        Timestamp ts = null;
        try { ts = rs.getTimestamp("fecha_creacion"); } catch (SQLException ignore) {}
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

    public Optional<String> obtenerNombreClientePorId(int clienteId) {
        String sql = "SELECT nombre_completo FROM clientes WHERE usuario_id = ?"; // ajusta según tu modelo
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, clienteId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.ofNullable(rs.getString(1));
            }
        } catch (SQLException e) { }
        return Optional.empty();
    }

    public Optional<String> obtenerPlacaVehiculoPorId(int vehiculoId) {
        String sql = "SELECT placa FROM vehiculos WHERE id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, vehiculoId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.ofNullable(rs.getString(1));
            }
        } catch (SQLException e) { }
        return Optional.empty();
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
    public boolean confirmarRenta(int idAlquiler, int idEmpleado) {
        String sql = """
            UPDATE alquileres
            SET estado = 'ALQUILADO',
                empleado_inicio_id = ?,
                costo_total = (
                    SELECT precio_diario * (COALESCE(fecha_fin_real, fecha_fin_estimada) - fecha_inicio + 1)
                    FROM alquileres WHERE id = ?
                )
            WHERE id = ?
        """;

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idEmpleado);
            ps.setInt(2, idAlquiler);
            ps.setInt(3, idAlquiler);

            int updated = ps.executeUpdate();

            if (updated == 0) return false;

            //Obtener costo total ya calculado
            BigDecimal costoTotal = null;
            try (PreparedStatement ps2 = conexion.prepareStatement(
                    "SELECT costo_total FROM alquileres WHERE id = ?"
            )) {
                ps2.setInt(1, idAlquiler);
                var rs = ps2.executeQuery();
                if (rs.next()) costoTotal = rs.getBigDecimal("costo_total");
            }

            // ACTUALIZAR pago existente (ya creado por el cliente)
            String sqlPago = "UPDATE pagos SET monto = ? WHERE alquiler_id = ?";
            try (PreparedStatement ps3 = conexion.prepareStatement(sqlPago)) {
                ps3.setBigDecimal(1, costoTotal);
                ps3.setInt(2, idAlquiler);
                ps3.executeUpdate();
            }

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // -------------------- NUEVO: reservarYAlquilar (transaccional) --------------------
    /**
     * Crea el alquiler de forma inmediata (reserva -> alquiler) en una transacción aislada.
     * Devuelve el id del alquiler insertado o -1 si hubo conflicto/error.
     *
     * Usa fecha_fin_estimada para la comprobación e inserción.
     */
    public int reservarYAlquilar(Alquiler a) {
        String sqlLockVeh = "SELECT estado FROM vehiculos WHERE id = ? FOR UPDATE";
        String sqlCheck = """
            SELECT COUNT(1) AS cnt FROM alquileres
            WHERE vehiculo_id = ?
              AND estado IN ('EN CURSO','RESERVADO','ALQUILADO')
              AND NOT (fecha_fin_estimada < ? OR fecha_inicio > ?)
        """;
        String sqlInsert = """
            INSERT INTO alquileres
            (vehiculo_id, cliente_id, fecha_inicio, fecha_fin_estimada, precio_diario, estado, notas)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            RETURNING id
        """;
        String sqlUpdateVeh = "UPDATE vehiculos SET estado = ? WHERE id = ?";

        LocalDate inicio = a.getFechaInicio();
        LocalDate fin = a.getFechaFin() != null ? a.getFechaFin() : a.getFechaInicio();

        try (Connection conn = ConexionDB.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psLock = conn.prepareStatement(sqlLockVeh);
                 PreparedStatement psCheck = conn.prepareStatement(sqlCheck);
                 PreparedStatement psInsert = conn.prepareStatement(sqlInsert);
                 PreparedStatement psUpdateVeh = conn.prepareStatement(sqlUpdateVeh)) {

                // 1) Bloquear vehículo
                psLock.setInt(1, a.getVehiculoId());
                try (ResultSet rsLock = psLock.executeQuery()) {
                    if (!rsLock.next()) {
                        conn.rollback();
                        return -1; // vehículo no existe
                    }
                    String estadoVeh = rsLock.getString("estado");
                    if (estadoVeh != null && estadoVeh.equalsIgnoreCase("inactivo")) {
                        conn.rollback();
                        return -1; // vehículo inactivo
                    }
                }

                // 2) comprobar solapamiento (fecha_fin_estimada)
                psCheck.setInt(1, a.getVehiculoId());
                psCheck.setDate(2, Date.valueOf(inicio));
                psCheck.setDate(3, Date.valueOf(fin));
                try (ResultSet rsCheck = psCheck.executeQuery()) {
                    if (rsCheck.next()) {
                        int cnt = rsCheck.getInt("cnt");
                        if (cnt > 0) {
                            conn.rollback();
                            return -1; // conflicto
                        }
                    } else {
                        conn.rollback();
                        return -1;
                    }
                }

                // 3) insertar alquiler como ALQUILADO (tu requisito: reservar -> alquilar de inmediato)
                psInsert.setInt(1, a.getVehiculoId());
                psInsert.setInt(2, a.getClienteId());
                psInsert.setDate(3, Date.valueOf(inicio));
                psInsert.setDate(4, Date.valueOf(fin)); // fecha_fin_estimada
                psInsert.setBigDecimal(5, a.getPrecioDiario() != null ? a.getPrecioDiario() : BigDecimal.ZERO);
                psInsert.setString(6, "ALQUILADO");
                psInsert.setString(7, a.getNotas());

                try (ResultSet rsIns = psInsert.executeQuery()) {
                    if (rsIns.next()) {
                        int nuevoId = rsIns.getInt(1);

                        // 4) actualizar estado del vehiculo
                        psUpdateVeh.setString(1, "ALQUILADO");
                        psUpdateVeh.setInt(2, a.getVehiculoId());
                        psUpdateVeh.executeUpdate();

                        conn.commit();
                        return nuevoId;
                    } else {
                        conn.rollback();
                        return -1;
                    }
                }

            } catch (SQLException ex) {
                try { conn.rollback(); } catch (SQLException ignore) {}
                ex.printStackTrace();
                return -1;
            } finally {
                try { conn.setAutoCommit(true); } catch (SQLException ignore) {}
            }
        } catch (SQLException exOuter) {
            exOuter.printStackTrace();
            return -1;
        }
    }

    // -------------------- FIN CLASS --------------------
}
