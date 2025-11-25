package com.example.proyecto_final_prograiii.DAO;

import com.example.proyecto_final_prograiii.config.ConexionDB;
import com.example.proyecto_final_prograiii.models.TipoVehiculo;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TipoVehiculosDAO {

    private final Connection conexion;

    public TipoVehiculosDAO() {
        conexion = ConexionDB.getConnection();
    }

    private TipoVehiculo mapTipo(ResultSet rs) throws SQLException {
        TipoVehiculo t = new TipoVehiculo();
        t.setId(rs.getInt("id"));
        t.setNombre(rs.getString("nombre"));
        t.setDescripcion(rs.getString("descripcion"));

        Timestamp ts = rs.getTimestamp("fecha_creacion");
        if (ts != null) {
            t.setFechaCreacion(ts.toLocalDateTime());
        }

        return t;
    }

    public List<TipoVehiculo> listarTipos() {
        List<TipoVehiculo> lista = new ArrayList<>();

        String sql = "SELECT id, nombre, descripcion, fecha_creacion FROM tipos_vehiculo ORDER BY id";

        try (Statement st = conexion.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(mapTipo(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error al listar los tipos de vehículos: " + e.getMessage());
        }

        return lista;
    }

    public TipoVehiculo obtenerPorId(int id) {
        String sql = "SELECT id, nombre, descripcion, fecha_creacion FROM tipos_vehiculo WHERE id = ?";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapTipo(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener tipo de vehículo: " + e.getMessage());
        }

        return null;
    }
}
