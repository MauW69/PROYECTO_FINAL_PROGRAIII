package com.example.proyecto_final_prograiii.config;

import javafx.scene.control.Alert;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionDB {
    private static final  String url="jdbc:postgresql://localhost:5432/db_rentacar";
    private static final String user="postgres";
    private static final String pass="admin123";

    //metodo de conexion
    public static Connection getConnection(){
        try {
            return DriverManager.getConnection(url, user, pass);
        } catch (SQLException e) {
            alerta("Fallo en la conexion", "Ocurrio un error en la execcion", e);
            System.out.println("Error en la conexion " +e.getMessage());
            return null;
        }
    }

    public static void alerta(String titulo, String mensaje, SQLException exepcion){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setContentText(mensaje + exepcion.getMessage());
        alert.show();
    }
}
