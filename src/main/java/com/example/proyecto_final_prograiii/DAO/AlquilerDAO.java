package com.example.proyecto_final_prograiii.DAO;

import com.example.proyecto_final_prograiii.DTO.AlquilerHistorialDTO;
import com.example.proyecto_final_prograiii.DTO.AlquilerSolicitudDTO;
import com.example.proyecto_final_prograiii.config.ConexionDB;
import com.example.proyecto_final_prograiii.models.Alquiler;
import com.example.proyecto_final_prograiii.models.Pago;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AlquilerDAO {
    private Connection conexion;
    public AlquilerDAO() {
        conexion = ConexionDB.getConnection();
    }

    //metodo para crear solicitudes de alquiler por parte del cliente
    public int crearSolicitudAlquiler(Alquiler a) {

        String sql = """
        INSERT INTO alquileres
        (vehiculo_id, cliente_id, fecha_inicio, fecha_fin, precio_diario, estado, notas)
        VALUES (?, ?, ?, ?, ?, ?, ?)
        RETURNING id
    """;

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, a.getVehiculoId());
            ps.setInt(2, a.getClienteId());
            ps.setDate(3, Date.valueOf(a.getFechaInicio()));
            ps.setDate(4, a.getFechaFin() != null ? Date.valueOf(a.getFechaFin()) : null);
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

    //metodo para llenar la tabla de solicitudes de reserva para el empleado
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
                        rs.getInt("vehiculo_id"),          // <-- aquí
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
    //metodo para llenar la tabla de historial de rentas para el empleado
    public List<AlquilerHistorialDTO> obtenerHistorial() {

        String sql = """
        SELECT a.id,
               CONCAT(v.modelo, ' (', v.placa, ')') AS vehiculo,
               a.fecha_inicio,
               a.fecha_fin,
               COALESCE(p.monto, 0) AS total_pagado,
               p.metodo,
               a.estado
        FROM alquileres a
        INNER JOIN vehiculos v ON a.vehiculo_id = v.id
        LEFT JOIN pagos p ON p.alquiler_id = a.id
        WHERE a.estado IN ('FINALIZADO', 'CANCELADO')
        ORDER BY a.fecha_inicio DESC
    """;

        List<AlquilerHistorialDTO> lista = new ArrayList<>();

        try (PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new AlquilerHistorialDTO(
                        rs.getInt("id"),
                        rs.getString("vehiculo"),
                        rs.getDate("fecha_inicio").toLocalDate(),
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



    public boolean finalizar(int id, LocalDate fechaFin, BigDecimal costoTotal) {
        String sql = "UPDATE alquileres SET estado = 'FINALIZADO', fecha_fin = ?, costo_total = ? WHERE id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(fechaFin));
            ps.setBigDecimal(2, costoTotal);
            ps.setInt(3, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error finalizar alquiler: " + e.getMessage());
            return false;
        }
    }

    // metodo de ayuda para mapear ResultSet -> Alquiler
    private Alquiler mapResultSetToAlquiler(ResultSet rs) throws SQLException {
        Alquiler a = new Alquiler();
        a.setId(rs.getInt("id"));
        a.setVehiculoId(rs.getInt("vehiculo_id"));
        a.setClienteId(rs.getInt("cliente_id"));
        a.setEmpleadoInicioId(rs.getObject("empleado_inicio_id") != null ? rs.getInt("empleado_inicio_id") : null);
        a.setEmpleadoFinId(rs.getObject("empleado_fin_id") != null ? rs.getInt("empleado_fin_id") : null);
        a.setFechaInicio(rs.getDate("fecha_inicio") != null ? rs.getDate("fecha_inicio").toLocalDate() : null);
        a.setFechaFin(rs.getDate("fecha_fin") != null ? rs.getDate("fecha_fin").toLocalDate() : null);
        a.setPrecioDiario(rs.getBigDecimal("precio_diario"));
        a.setCostoTotal(rs.getBigDecimal("costo_total"));
        a.setEstado(rs.getString("estado"));
        a.setNotas(rs.getString("notas"));
        a.setFechaCreacion(rs.getTimestamp("fecha_creacion").toLocalDateTime());
        return a;
    }

    // Opcional: obtener datos extra (vehículo, cliente) con JOIN si quieres mostrar nombre o placa directamente
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


    public boolean confirmarRenta(int idAlquiler, int idEmpleado) {
        String sql = """
        UPDATE alquileres
        SET estado = 'ALQUILADO',
            empleado_inicio_id = ?,
            costo_total = (
                SELECT precio_diario * 
                (DATE_PART('day', fecha_fin - fecha_inicio) + 1)
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


}


