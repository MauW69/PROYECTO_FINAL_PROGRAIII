package com.example.proyecto_final_prograiii.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Pago {
    private int id;
    private int alquilerId;
    private BigDecimal monto;
    private LocalDateTime fechaPago;
    private String metodo;
    private LocalDateTime fechaCreacion;


    public Pago() {}



    public Pago(int id, int alquilerId, BigDecimal monto, LocalDateTime fechaPago, String metodo, LocalDateTime fechaCreacion ) {
        this.id = id;
        this.alquilerId = alquilerId;
        this.monto = monto;
        this.fechaPago = fechaPago;
        this.metodo = metodo;
        this.fechaCreacion = fechaCreacion;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAlquilerId() {
        return alquilerId;
    }

    public void setAlquilerId(int alquilerId) {
        this.alquilerId = alquilerId;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public LocalDateTime getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(LocalDateTime fechaPago) {
        this.fechaPago = fechaPago;
    }

    public String getMetodo() {
        return metodo;
    }

    public void setMetodo(String metodo) {
        this.metodo = metodo;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
    //testeo
    @Override
    public String toString() {
        return "Pago{" +
                "id=" + id +
                ", alquilerId=" + alquilerId +
                ", monto=" + monto +
                ", fechaPago=" + fechaPago +
                '}';
    }

}
