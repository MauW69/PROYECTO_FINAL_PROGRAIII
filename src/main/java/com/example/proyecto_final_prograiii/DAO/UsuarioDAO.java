package com.example.proyecto_final_prograiii.DAO;

import com.example.proyecto_final_prograiii.config.ConexionDB;
import com.example.proyecto_final_prograiii.models.Usuario;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UsuarioDAO {
//    public Usuario login(String nombreUsuario, String claveHash){
//        String query = "SELECT * FROM usuarios WHERE nombre_usuario = ?";
//
//        try {
//            Connection conn = ConexionDB.getConnection();
//            PreparedStatement preparedStatement = conn.prepareStatement(query);
//
//            preparedStatement.setString(1, nombreUsuario);
//            ResultSet resultSet = preparedStatement.executeQuery();
//
//            if (resultSet.next()){
//                String clave_hash = resultSet.getString("clave_hash");
//            }
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//
//    }

}
