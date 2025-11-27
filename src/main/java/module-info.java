module com.example.proyecto_final_prograiii {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires java.sql;
    requires jbcrypt;
    requires java.desktop;
    requires jakarta.mail;
    requires com.calendarfx.view;

    opens com.example.proyecto_final_prograiii to javafx.fxml;
    exports com.example.proyecto_final_prograiii;
    exports com.example.proyecto_final_prograiii.config;
    exports com.example.proyecto_final_prograiii.controllers;
    exports com.example.proyecto_final_prograiii.DAO;
    exports com.example.proyecto_final_prograiii.DTO;
    exports com.example.proyecto_final_prograiii.models;
    exports com.example.proyecto_final_prograiii.utils;



    opens com.example.proyecto_final_prograiii.controllers to javafx.fxml;
}