package com.example.proyecto_final_prograiii.DAO;

import com.example.proyecto_final_prograiii.config.ConexionDB;
import com.example.proyecto_final_prograiii.models.Usuario;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    private Connection conexion;

    public UsuarioDAO() {
        conexion = ConexionDB.getConnection();
    }

    // mapeo de usuario
    private Usuario mapUsuario(ResultSet resultSet) throws SQLException {
        Usuario u = new Usuario();
        u.setId(resultSet.getInt("id"));
        u.setNombreUsuario(resultSet.getString("nombre_usuario"));
        u.setClaveHash(resultSet.getString("clave_hash"));
        u.setRolId(resultSet.getInt("rol_id"));
        Timestamp timestamp = resultSet.getTimestamp("fecha_creacion");
        u.setFechaCreacion(timestamp != null ? timestamp.toLocalDateTime() : null);
        return u;
    }

    // -------------------- CREATE --------------------
    // Crear usuario(para admin y para cliente)
    public int crearUsuario(Usuario usuario) {
        String sql = "INSERT INTO usuarios (nombre_usuario, clave_hash, rol_id) VALUES (?, ?, ?)";
        try (PreparedStatement preparedStatement = conexion.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, usuario.getNombreUsuario());
            preparedStatement.setString(2, usuario.getClaveHash());
            preparedStatement.setInt(3, usuario.getRolId());
            int filas = preparedStatement.executeUpdate();
            if (filas > 0) {
                ResultSet resultSet = preparedStatement.getGeneratedKeys();
                if (resultSet.next()) {
                    return resultSet.getInt(1); // id generado por postgres
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al crear usuario: " + e.getMessage());
        }
        return -1; // si falla
    }
    //crear usuario para empleados
    public boolean crearEmpleado(String nombreUsuario, String claveHash) {
        String sql = "INSERT INTO usuarios (nombre_usuario, clave_hash, rol_id) VALUES (?, ?, 2)";

        try (PreparedStatement preparedStatement = conexion.prepareStatement(sql)) {
            preparedStatement.setString(1, nombreUsuario);
            preparedStatement.setString(2, claveHash);
            return preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    // -------------------- READ --------------------
    // Buscar usuario por nombre (para login)
    public Usuario obtenerPorNombreUsuario(String nombre) {
        String sql = "SELECT * FROM usuarios WHERE nombre_usuario = ?";
        Usuario usuario = null;

        try (PreparedStatement preparedStatement = conexion.prepareStatement(sql)) {
            preparedStatement.setString(1, nombre);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                usuario = mapUsuario(resultSet);
            }
        } catch (SQLException e) {
            System.err.println("Error, usuario no encontrado: " + e.getMessage());
        }

        return usuario;
    }

    // Listar todos los usuarios tipo empleado
    public List<Usuario> obtenerEmpleados() {
        List<Usuario> empleados = new ArrayList<>();
        String sql = "SELECT id, nombre_usuario, rol_id, fecha_creacion FROM usuarios WHERE rol_id = 2";
        try (PreparedStatement preparedStatement = conexion.prepareStatement(sql)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                empleados.add(new Usuario(
                        resultSet.getInt("id"),
                        resultSet.getString("nombre_usuario"),
                        resultSet.getInt("rol_id"),
                        resultSet.getTimestamp("fecha_creacion").toLocalDateTime()
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return empleados;
    }

    // -------------------- UPDATE --------------------
    //permite que un usuario tipo emplado permita almacenar el mismo nombre de usuario
    public String validarNombreUsuarioUnico(String nombreUsuario, int usuarioId) {
        String sql = "SELECT id FROM usuarios WHERE nombre_usuario = ? AND id <> ?";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, nombreUsuario);
            ps.setInt(2, usuarioId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return "El nombre de usuario ya esta en uso";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Error al validar nombre de usuario";
        }

        return null;
    }
    public boolean actualizarEmpleado(int id, String nombreUsuario, String claveHash) {
        String error = validarNombreUsuarioUnico(nombreUsuario, id);
        if (error != null) {
            System.out.println(error);
            return false;
        }
        try {
            if (claveHash == null || claveHash.isEmpty()) {
                String sql = "UPDATE usuarios SET nombre_usuario = ? WHERE id = ? AND rol_id = 2";
                try (PreparedStatement preparedStatement = conexion.prepareStatement(sql)) {
                    preparedStatement.setString(1, nombreUsuario);
                    preparedStatement.setInt(2, id);
                    return preparedStatement.executeUpdate() > 0;
                }
            } else {
                String sql = "UPDATE usuarios SET nombre_usuario = ?, clave_hash = ? WHERE id = ? AND rol_id = 2";
                try (PreparedStatement preparedStatement = conexion.prepareStatement(sql)) {
                    preparedStatement.setString(1, nombreUsuario);
                    preparedStatement.setString(2, claveHash);
                    preparedStatement.setInt(3, id);
                    return preparedStatement.executeUpdate() > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }



    // -------------------- DELETE --------------------
    // Eliminar usuario
    public boolean eliminarUsuario(int id) {
        String sql = "DELETE FROM usuarios WHERE id = ?";

        try (PreparedStatement preparedStatement = conexion.prepareStatement(sql)) {
            preparedStatement.setInt(1, id);
            return preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    // -------------------- VALIDACION --------------------
    // Validar para que solo se permitan nombre de usuarios diferentes
    public boolean existeNombreUsuario(String nombreUsuario) {
        String sql = "SELECT id FROM usuarios WHERE nombre_usuario = ? LIMIT 1";

        try (PreparedStatement preparedStatement = conexion.prepareStatement(sql)) {
            preparedStatement.setString(1, nombreUsuario);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next(); // true si existe, false si no
        } catch (SQLException e) {
            System.err.println("Error al verificar nombre_usuario: " + e.getMessage());
            return true;
        }
    }
}

