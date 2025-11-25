package com.example.proyecto_final_prograiii;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 589, 400);
        scene.getStylesheets().add(getClass().getResource("css/login.css").toExternalForm());
        stage.setTitle("INICIO DE SESION");
        stage.setScene(scene);
        stage.show();
    }
}
