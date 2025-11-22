package com.example.proyecto_final_prograiii.DAO;

import com.example.proyecto_final_prograiii.config.ConexionDB;
import com.example.proyecto_final_prograiii.models.Vehiculo;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class VehiculosDAO {
    private Connection conexion;

    public VehiculosDAO() {
        conexion = ConexionDB.getConnection();
    }

    // -------------------- MAPEO --------------------
    private Vehiculo mapVehiculo(ResultSet rs) throws SQLException {
        Vehiculo v = new Vehiculo();

        v.setId(rs.getInt("id"));
        v.setTipoVehiculoId(rs.getInt("tipo_id"));
        v.setPlaca(rs.getString("placa"));
        v.setModelo(rs.getString("modelo"));
        v.setYear(rs.getInt("year"));
        v.setColor(rs.getString("color"));
        v.setKilometraje(rs.getInt("kilometraje"));
        v.setEstado(rs.getString("estado"));

        Timestamp ts = rs.getTimestamp("fecha_creacion");
        v.setFechaCreacion(ts != null ? ts.toLocalDateTime() : null);

        // NUEVO: obtener precio_por_dia
        v.setPrecioPorDia(rs.getBigDecimal("precio_por_dia"));

        return v;
    }

    // -------------------- CREATE --------------------
    public boolean crearVehiculo(Vehiculo v) {
        String sql = "INSERT INTO vehiculos (tipo_id, placa, modelo, year, color, kilometraje, estado, precio_por_dia) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {

            if (v.getTipoVehiculoId() != 0)
                ps.setInt(1, v.getTipoVehiculoId());
            else
                ps.setNull(1, Types.INTEGER);

            ps.setString(2, v.getPlaca());
            ps.setString(3, v.getModelo());
            ps.setInt(4, v.getYear());
            ps.setString(5, v.getColor());
            ps.setInt(6, v.getKilometraje());
            ps.setString(7, v.getEstado());

            // NUEVO: precio_por_dia
            ps.setBigDecimal(8, v.getPrecioPorDia());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al crear vehículo: " + e.getMessage());
            return false;
        }
    }

    // -------------------- READ --------------------
    public Vehiculo obtenerPorIdVehiculo(int id) {
        String sql = "SELECT * FROM vehiculos WHERE id = ?";
        Vehiculo v = null;

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) v = mapVehiculo(rs);
        } catch (SQLException e) {
            System.err.println("Error al obtener vehículo: " + e.getMessage());
        }

        return v;
    }

    public List<Vehiculo> obtenerTodosVehiculos() {
        List<Vehiculo> lista = new ArrayList<>();
        String sql = "SELECT * FROM vehiculos ORDER BY id";

        try (Statement st = conexion.createStatement()) {
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) lista.add(mapVehiculo(rs));
        } catch (SQLException e) {
            System.err.println("Error al listar vehículos: " + e.getMessage());
        }

        return lista;
    }

    public List<Vehiculo> listarVehiculos(int limit) {
        List<Vehiculo> lista = new ArrayList<>();
        String sql = "SELECT * FROM vehiculos ORDER BY id LIMIT ?";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) lista.add(mapVehiculo(rs));
        } catch (SQLException e) {
            System.err.println("Error al listar vehículos: " + e.getMessage());
        }
        return lista;
    }

    // -------------------- UPDATE --------------------
    public boolean actualizarVehiculo(Vehiculo v) {
        String sql = "UPDATE vehiculos SET tipo_id = ?, placa = ?, modelo = ?, year = ?, color = ?, kilometraje = ?, estado = ?, precio_por_dia = ? " +
                "WHERE id = ?";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {

            if (v.getTipoVehiculoId() != 0)
                ps.setInt(1, v.getTipoVehiculoId());
            else
                ps.setNull(1, Types.INTEGER);

            ps.setString(2, v.getPlaca());
            ps.setString(3, v.getModelo());
            ps.setInt(4, v.getYear());
            ps.setString(5, v.getColor());
            ps.setInt(6, v.getKilometraje());
            ps.setString(7, v.getEstado());

            // NUEVO: actualizar precio
            ps.setBigDecimal(8, v.getPrecioPorDia());

            ps.setInt(9, v.getId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar vehículo: " + e.getMessage());
            return false;
        }
    }

    // -------------------- DELETE --------------------
    public boolean eliminarVehiculo(int id) {
        String sql = "DELETE FROM vehiculos WHERE id = ?";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar vehículo: " + e.getMessage());
            return false;
        }
    }
}
