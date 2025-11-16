module com.example.proyecto_final_prograiii {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires java.sql;

    opens com.example.proyecto_final_prograiii to javafx.fxml;
    exports com.example.proyecto_final_prograiii;
    exports com.example.proyecto_final_prograiii.controllers;
    //exports com.example.proyecto_final_prograiii.models;

    opens com.example.proyecto_final_prograiii.controllers to javafx.fxml;
}