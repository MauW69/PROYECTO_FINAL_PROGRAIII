package com.example.proyecto_final_prograiii.DAO;

import com.example.proyecto_final_prograiii.DTO.ClienteDTO;
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

    // Obtener cliente por usuario_id (opcional, util para login de clientes)
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

    //lectura para cargar la informacion de los clientes
    public List<ClienteDTO> obtenerClientesConUsuario() {
        List<ClienteDTO> lista = new ArrayList<>();

        String sql = """
        SELECT 
            u.nombre_usuario,
            c.id,
            c.usuario_id,
            c.nombre,
            c.apellido,
            c.edad,
            c.telefono,
            c.email,
            c.direccion
        FROM clientes c
        INNER JOIN usuarios u ON u.id = c.usuario_id
    """;

        try (PreparedStatement preparedStatement = conexion.prepareStatement(sql)) {
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                ClienteDTO dto = new ClienteDTO();
                dto.setNombreUsuario(resultSet.getString("nombre_usuario"));
                dto.setId(resultSet.getInt("id"));
                dto.setUsuarioId(resultSet.getInt("usuario_id"));
                dto.setNombre(resultSet.getString("nombre"));
                dto.setApellido(resultSet.getString("apellido"));
                dto.setEdad(resultSet.getInt("edad"));
                dto.setTelefono(resultSet.getString("telefono"));
                dto.setEmail(resultSet.getString("email"));
                dto.setDireccion(resultSet.getString("direccion"));

                //campo proveniente de Usuario
                lista.add(dto);
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener clientes con usuario: " + e.getMessage());
        }
        return lista;
    }




    // -------------------- UPDATE --------------------
    public String validarCamposUnicos(String email, String nombreUsuario, int clienteId) {

        //validar email en clientes
        String sqlEmail = "SELECT 1 FROM clientes WHERE email = ? AND id <> ?";
        try (PreparedStatement st = conexion.prepareStatement(sqlEmail)) {
            st.setString(1, email);
            st.setInt(2, clienteId);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                return "El correo ya está en uso por otro cliente.";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Error verificando correo.";
        }

        // validar nombre_usuario en usuarios
        String sqlUsuario = """
            SELECT 1 
            FROM usuarios u
            INNER JOIN clientes c ON c.usuario_id = u.id
            WHERE u.nombre_usuario = ? AND c.id <> ?
    """;

        try (PreparedStatement preparedStatement = conexion.prepareStatement(sqlUsuario)) {
            preparedStatement.setString(1, nombreUsuario);
            preparedStatement.setInt(2, clienteId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return "El nombre de usuario ya está en uso por otro cliente.";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Error verificando nombre de usuario.";
        }

        return null;
    }

    public boolean actualizarClienteYUsuario(int clienteId, String nombre, String apellido, int edad, String telefono, String email, String direccion, String nombreUsuario) {

        String sqlCliente = """
        UPDATE clientes
        SET nombre = ?, apellido = ?, edad = ?, telefono = ?, email = ?, direccion = ?
        WHERE id = ?
    """;

        String sqlUsuario = """
        UPDATE usuarios
        SET nombre_usuario = ?
        WHERE id = (SELECT usuario_id FROM clientes WHERE id = ?)
    """;

        try {

            //actualizar cliente
            try (PreparedStatement preparedStatement = conexion.prepareStatement(sqlCliente)) {
                preparedStatement.setString(1, nombre);
                preparedStatement.setString(2, apellido);
                preparedStatement.setInt(3, edad);
                preparedStatement.setString(4, telefono);
                preparedStatement.setString(5, email);
                preparedStatement.setString(6, direccion);
                preparedStatement.setInt(7, clienteId);
                preparedStatement.executeUpdate();
            }

            //actualizar usuario
            try (PreparedStatement preparedStatement = conexion.prepareStatement(sqlUsuario)) {
                preparedStatement.setString(1, nombreUsuario);
                preparedStatement.setInt(2, clienteId);
                preparedStatement.executeUpdate();
            }

            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    // -------------------- DELETE --------------------

    public boolean eliminarCliente(int idUsuario) {
        String sql = "DELETE FROM usuarios WHERE id = ?";
        try (PreparedStatement preparedStatement = conexion.prepareStatement(sql)) {
            preparedStatement.setInt(1, idUsuario);
            return preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


}
