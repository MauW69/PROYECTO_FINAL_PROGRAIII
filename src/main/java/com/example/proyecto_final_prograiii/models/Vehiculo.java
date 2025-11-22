package com.example.proyecto_final_prograiii.models;

import java.time.LocalDateTime;
import java.math.BigDecimal;

public class Vehiculo {
    private int id;
    private int tipoVehiculoId;
    private String placa;
    private String modelo;
    private int year;
    private String color;
    private int kilometraje;
    private String estado; // DISPONIBLE, RENTADO, MANTENIMIENTO
    private LocalDateTime fechaCreacion;
    private BigDecimal precioPorDia; // nuevo campo

    public Vehiculo() {

    }

    public Vehiculo(int id, int tipoVehiculoId, String placa, String modelo, int year, String color, int kilometraje, String estado, LocalDateTime fechaCreacion) {
        this.id = id;
        this.tipoVehiculoId = tipoVehiculoId;
        this.placa = placa;
        this.modelo = modelo;
        this.year = year;
        this.color = color;
        this.kilometraje = kilometraje;
        this.estado = estado;
        this.fechaCreacion = fechaCreacion;
        this.precioPorDia = null;
    }

    // constructor con precioPorDia
    public Vehiculo(int id, int tipoVehiculoId, String placa, String modelo, int year, String color, int kilometraje, String estado, LocalDateTime fechaCreacion, BigDecimal precioPorDia) {
        this.id = id;
        this.tipoVehiculoId = tipoVehiculoId;
        this.placa = placa;
        this.modelo = modelo;
        this.year = year;
        this.color = color;
        this.kilometraje = kilometraje;
        this.estado = estado;
        this.fechaCreacion = fechaCreacion;
        this.precioPorDia = precioPorDia;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTipoVehiculoId() {
        return tipoVehiculoId;
    }

    public void setTipoVehiculoId(int tipoVehiculoId) {
        this.tipoVehiculoId = tipoVehiculoId;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getKilometraje() {
        return kilometraje;
    }

    public void setKilometraje(int kilometraje) {
        this.kilometraje = kilometraje;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public BigDecimal getPrecioPorDia() {
        return precioPorDia;
    }

    public void setPrecioPorDia(BigDecimal precioPorDia) {
        this.precioPorDia = precioPorDia;
    }

    // testeo
    @Override
    public String toString() {
        return "Vehiculo{" +
                "id=" + id +
                ", tipoVehiculoId=" + tipoVehiculoId +
                ", placa='" + placa + '\'' +
                ", modelo='" + modelo + '\'' +
                ", year=" + year +
                ", color='" + color + '\'' +
                ", kilometraje=" + kilometraje +
                ", estado='" + estado + '\'' +
                ", fechaCreacion=" + fechaCreacion +
                ", precioPorDia=" + precioPorDia +
                '}';
    }
}
