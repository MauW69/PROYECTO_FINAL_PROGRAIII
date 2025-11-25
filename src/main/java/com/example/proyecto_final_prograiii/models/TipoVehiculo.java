package com.example.proyecto_final_prograiii.models;

import java.time.LocalDateTime;

public class TipoVehiculo {
    private int id;
    private String nombre;
    private String descripcion;
    private LocalDateTime fechaCreacion;

    public TipoVehiculo() {
    }

    public TipoVehiculo(int id, String nombre, String descripcion, LocalDateTime fechaCreacion) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.fechaCreacion = fechaCreacion;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    //para testeo
    @Override
    public String toString() {
        return nombre;
    }

}
