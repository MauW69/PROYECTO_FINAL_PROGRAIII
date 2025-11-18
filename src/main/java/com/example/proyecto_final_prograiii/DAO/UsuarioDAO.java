package com.example.proyecto_final_prograiii.DAO;

import com.example.proyecto_final_prograiii.config.ConexionDB;
import com.example.proyecto_final_prograiii.models.Usuario;
import javafx.scene.control.Alert;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UsuarioDAO {
    public static void alerta(String titulo, String mensaje){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setContentText(mensaje);
        alert.show();
    }

    private Connection conexion;

    public UsuarioDAO() {
        conexion = ConexionDB.getConnection();
    }

    // Crear usuario
    public boolean crear(Usuario usuario) {
        String sql = "INSERT INTO usuarios (nombre_usuario, clave_hash, rol_id) VALUES (?, ?, ?)";
        try(PreparedStatement preparedStatement = conexion.prepareStatement(sql)){
            preparedStatement.setString(1, usuario.getNombreUsuario());
            preparedStatement.setString(2, usuario.getClaveHash());
            preparedStatement.setInt(3, usuario.getRolId());
            return preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            alerta("Error", "Error al Crear Usuario : "+e);
            return false;
        }
    }

}
