package com.example.proyecto_final_prograiii.DAO;

import com.example.proyecto_final_prograiii.config.ConexionDB;
import com.example.proyecto_final_prograiii.models.Cliente;

import java.sql.*;

public class ClienteDAO {
    private Connection conexion;

    public ClienteDAO() {
        conexion = ConexionDB.getConnection();
    }
    //mapeo de Cliente
    private Cliente mapCliente(ResultSet resultSet) throws SQLException {
        Cliente cliente = new Cliente();
        cliente.setId(resultSet.getInt("id"));
        cliente.setUsuarioId(resultSet.getInt("usuario_id"));
        cliente.setNombre(resultSet.getString("nombre"));
        cliente.setApellido(resultSet.getString("apellido"));
        cliente.setEdad(resultSet.getInt("edad"));
        cliente.setEmail(resultSet.getString("email"));
        cliente.setTelefono(resultSet.getString("telefono"));
        cliente.setDireccion(resultSet.getString("direccion"));

        Timestamp timestamp = resultSet.getTimestamp("fecha_creacion");
        cliente.setFechaCreacion(timestamp != null ? timestamp.toLocalDateTime() : null);

        return cliente;
    }


    public boolean crearCliente(Cliente cliente) {
        String sql = "INSERT INTO clientes (usuario_id, nombre, apellido, email, telefono, direccion, edad) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try(PreparedStatement preparedStatement = conexion.prepareStatement(sql)) {
            preparedStatement.setInt(1, cliente.getUsuarioId());
            preparedStatement.setString(2, cliente.getNombre());
            preparedStatement.setString(3, cliente.getApellido());
            preparedStatement.setString(4, cliente.getEmail());
            preparedStatement.setString(5, cliente.getTelefono());
            preparedStatement.setString(6, cliente.getDireccion());
            preparedStatement.setInt(7, cliente.getEdad());

            return preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al crear cliente: " + e.getMessage());
            return false;
        }
    }
}
