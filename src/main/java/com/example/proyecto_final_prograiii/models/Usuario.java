package com.example.proyecto_final_prograiii.models;

import java.sql.Date;
import java.time.LocalDateTime;



public class Usuario {
    private int id;
    private String nombreUsuario;
    private String claveHash;
    private int rolId;
    private LocalDateTime fechaCreacion;

    public Usuario() {
    }

    //constructor que se puede usar para crear nuevos usuarios
    public Usuario(String nombreUsuario, String claveHash, int rolId) {
        this.nombreUsuario = nombreUsuario;
        this.claveHash = claveHash;
        this.rolId = rolId;
        this.fechaCreacion = LocalDateTime.now();
    }

    //constructor general
    public Usuario(int id, String nombreUsuario, String claveHash, int rolId, LocalDateTime fechaCreacion) {
        this.id = id;
        this.nombreUsuario = nombreUsuario;
        this.claveHash = claveHash;
        this.rolId = rolId;
        this.fechaCreacion = fechaCreacion;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getClaveHash() {
        return claveHash;
    }

    public void setClaveHash(String claveHash) {
        this.claveHash = claveHash;
    }

    public int getRolId() {
        return rolId;
    }

    public void setRolId(int rolId) {
        this.rolId = rolId;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    // solo para el testeo, DEGUB de informacion de los usuarios
    @Override
    public String toString() {
        return "DATO :" +
                "id :" + id + " User name : " + nombreUsuario + " claveHash : " + claveHash + " rol Id : " + rolId + " creacion : " + fechaCreacion;
    }
}
