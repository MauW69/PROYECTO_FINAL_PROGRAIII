package com.example.proyecto_final_prograiii.DAO;

import com.example.proyecto_final_prograiii.config.ConexionDB;
import com.example.proyecto_final_prograiii.models.Cliente;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAO {
    private Connection conexion;

    public ClienteDAO() {
        conexion = ConexionDB.getConnection();
    }

    // mapeo de Cliente
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

    // -------------------- CREATE --------------------
    public boolean crearCliente(Cliente cliente) {
        String sql = "INSERT INTO clientes (usuario_id, nombre, apellido, email, telefono, direccion, edad) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement preparedStatement = conexion.prepareStatement(sql)) {
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

    // -------------------- READ --------------------
    // Obtener cliente por id
    public Cliente obtenerPorIdCliente(int id) {
        String sql = "SELECT * FROM clientes WHERE id = ?";
        Cliente cliente = null;

        try (PreparedStatement preparedStatement = conexion.prepareStatement(sql)) {
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                cliente = mapCliente(resultSet);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener cliente: " + e.getMessage());
        }

        return cliente;
    }

    // Listar todos los clientes
    public List<Cliente> obtenerTodosClientes() {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT * FROM clientes";

        try (Statement statement = conexion.createStatement()) {
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                lista.add(mapCliente(resultSet));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar clientes: " + e.getMessage());
        }

        return lista;
    }

    // Obtener cliente por usuario_id (opcional, útil para login de clientes)
    public Cliente obtenerPorUsuarioId(int usuarioId) {
        String sql = "SELECT * FROM clientes WHERE usuario_id = ?";
        Cliente cliente = null;

        try (PreparedStatement preparedStatement = conexion.prepareStatement(sql)) {
            preparedStatement.setInt(1, usuarioId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                cliente = mapCliente(resultSet);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener cliente por usuario_id: " + e.getMessage());
        }

        return cliente;
    }

    // -------------------- UPDATE --------------------
    public boolean actualizarCliente(Cliente cliente) {
        String sql = "UPDATE clientes SET nombre = ?, apellido = ?, email = ?, telefono = ?, direccion = ?, edad = ? WHERE id = ?";

        try (PreparedStatement preparedStatement = conexion.prepareStatement(sql)) {
            preparedStatement.setString(1, cliente.getNombre());
            preparedStatement.setString(2, cliente.getApellido());
            preparedStatement.setString(3, cliente.getEmail());
            preparedStatement.setString(4, cliente.getTelefono());
            preparedStatement.setString(5, cliente.getDireccion());
            preparedStatement.setInt(6, cliente.getEdad());
            preparedStatement.setInt(7, cliente.getId());

            return preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar cliente: " + e.getMessage());
            return false;
        }
    }

    // -------------------- DELETE --------------------
    public boolean eliminarCliente(int id) {
        String sql = "DELETE FROM clientes WHERE id = ?";

        try (PreparedStatement preparedStatement = conexion.prepareStatement(sql)) {
            preparedStatement.setInt(1, id);
            return preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }
}
