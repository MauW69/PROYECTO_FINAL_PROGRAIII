package com.example.proyecto_final_prograiii.DAO;

import com.example.proyecto_final_prograiii.config.ConexionDB;
import com.example.proyecto_final_prograiii.models.Pago;

import java.math.BigDecimal;
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


}
