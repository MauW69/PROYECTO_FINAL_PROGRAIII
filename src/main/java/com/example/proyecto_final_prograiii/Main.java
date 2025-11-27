package com.example.proyecto_final_prograiii;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("panel-cliente-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 900, 600);
        scene.getStylesheets().add(getClass().getResource("css/panelCliente.css").toExternalForm());
        stage.setTitle("INICIO DE SESION");
        stage.setScene(scene);
        stage.show();
    }
}
