package com.example.proyecto_final_prograiii.DAO;

import com.example.proyecto_final_prograiii.config.ConexionDB;
import com.example.proyecto_final_prograiii.models.Pago;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class PagoDAO {
    private Connection conexion;

    public PagoDAO() {
        conexion = ConexionDB.getConnection();
    }

    public boolean registrarPago(Pago pago) {
        String sql = "INSERT INTO pagos (alquiler_id, monto, metodo) VALUES (?, ?, ?)";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, pago.getAlquilerId());
            ps.setBigDecimal(2, pago.getMonto());
            ps.setString(3, pago.getMetodo());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error registrarPago: " + e.getMessage());
            return false;
        }
    }

    public Optional<Pago> obtenerPagoPorAlquiler(int alquilerId) {
        String sql = "SELECT * FROM pagos WHERE alquiler_id = ? ORDER BY fecha_creacion DESC LIMIT 1";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, alquilerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Pago p = new Pago();
                    p.setId(rs.getInt("id"));
                    p.setAlquilerId(rs.getInt("alquiler_id"));
                    p.setMonto(rs.getBigDecimal("monto"));
                    p.setMetodo(rs.getString("metodo"));
                    p.setFechaCreacion(rs.getTimestamp("fecha_creacion"));
                    return Optional.of(p);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error obtenerPagoPorAlquiler: " + e.getMessage());
        }
        return Optional.empty();
    }


}
