package com.example.proyecto_final_prograiii.models;

import java.time.LocalDateTime;

public class Auditoria {
    private int id;
    private int usuarioId;
    private String accion;
    private String tablaAfectada;
    private String detalle;
    private LocalDateTime fecha;

    public Auditoria() {}

    public Auditoria(int id, int usuarioId, String accion, String tablaAfectada, String detalle,LocalDateTime fecha) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.accion = accion;
        this.tablaAfectada = tablaAfectada;
        this.detalle = detalle;
        this.fecha = fecha;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(int usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }

    public String getTablaAfectada() {
        return tablaAfectada;
    }

    public void setTablaAfectada(String tablaAfectada) {
        this.tablaAfectada = tablaAfectada;
    }

    public String getDetalle() {
        return detalle;
    }

    public void setDetalle(String detalle) {
        this.detalle = detalle;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }
    //testeo
    @Override
    public String toString() {
        return "Auditoria{" +
                "id=" + id +
                ", usuarioId=" + usuarioId +
                ", accion='" + accion + '\'' +
                ", tablaAfectada='" + tablaAfectada + '\'' +
                ", detalle='" + detalle + '\'' +
                ", fecha=" + fecha +
                '}';
    }
}
