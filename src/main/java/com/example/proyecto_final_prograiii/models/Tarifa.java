package com.example.proyecto_final_prograiii.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

//uso de bigDecimal para evitar errores con el calculo

public class Tarifa {
    private int id;
    private int tipoVehiculoId;
    private BigDecimal precioDiario;
    private boolean vigente;
    private LocalDateTime fechaCreacion;

    public Tarifa() {

    }

    public Tarifa(int id, int tipoVehiculoId, BigDecimal precioDiario, boolean vigente, LocalDateTime fechaCreacion) {
        this.id = id;
        this.tipoVehiculoId = tipoVehiculoId;
        this.precioDiario = precioDiario;
        this.vigente = vigente;
        this.fechaCreacion = fechaCreacion;
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

    public BigDecimal getPrecioDiario() {
        return precioDiario;
    }

    public void setPrecioDiario(BigDecimal precioDiario) {
        this.precioDiario = precioDiario;
    }

    public boolean isVigente() {
        return vigente;
    }

    public void setVigente(boolean vigente) {
        this.vigente = vigente;
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
        return "Tarifa{" +
                "id=" + id +
                ", tipoVehiculoId=" + tipoVehiculoId +
                ", precioDiario=" + precioDiario +
                ", vigente=" + vigente +
                ", fechaCreacion=" + fechaCreacion +
                '}';
    }
}
