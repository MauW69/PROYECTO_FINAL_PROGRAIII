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

    //se utiliza al realizar la reserva
    public boolean registrarPagoInicial(int alquilerId, String metodo) {
        String sql = "INSERT INTO pagos (alquiler_id, monto, metodo) VALUES (?, ?, ?)";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, alquilerId);
            ps.setBigDecimal(2, BigDecimal.ZERO); // 0.00 temporal
            ps.setString(3, metodo);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


}
