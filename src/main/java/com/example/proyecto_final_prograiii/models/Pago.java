package com.example.proyecto_final_prograiii.models;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class Pago {
    private int id;
    private int alquilerId;
    private BigDecimal monto;
    private String metodo;
    private Timestamp fechaCreacion;


    public Pago() {}



    public Pago(int id, int alquilerId, BigDecimal monto, String metodo, Timestamp fechaCreacion ) {
        this.id = id;
        this.alquilerId = alquilerId;
        this.monto = monto;

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

    public String getMetodo() {
        return metodo;
    }

    public void setMetodo(String metodo) {
        this.metodo = metodo;
    }

    public Timestamp getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Timestamp fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}
