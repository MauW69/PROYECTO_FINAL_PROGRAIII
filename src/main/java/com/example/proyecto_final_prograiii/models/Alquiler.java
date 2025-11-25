package com.example.proyecto_final_prograiii.models;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;

public class Alquiler {
    private int id;
    private int vehiculoId;
    private int clienteId;
    //aqui Integer para poder asignar un valor null ya que con int se tendria que inicializar en 0 y 0 no es nualidad
    private Integer empleadoInicioId;
    private Integer empleadoFinId;

    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    private BigDecimal precioDiario;
    private BigDecimal costoTotal;

    private String estado; // EN CURSO, FINALIZADO, CANCELADO
    private String notas;
    private LocalDateTime fechaCreacion;

    public Alquiler() {

    }

    public Alquiler(int id, int vehiculoId, int clienteId, Integer empleadoInicioId, Integer empleadoFinId, LocalDate fechaInicio, LocalDate fechaFin, BigDecimal precioDiario, BigDecimal costoTotal, String estado, String notas, LocalDateTime fechaCreacion) {
        this.id = id;
        this.vehiculoId = vehiculoId;
        this.clienteId = clienteId;
        this.empleadoInicioId = empleadoInicioId;
        this.empleadoFinId = empleadoFinId;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.precioDiario = precioDiario;
        this.costoTotal = costoTotal;
        this.estado = estado;
        this.notas = notas;
        this.fechaCreacion = fechaCreacion;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getVehiculoId() {
        return vehiculoId;
    }

    public void setVehiculoId(int vehiculoId) {
        this.vehiculoId = vehiculoId;
    }

    public int getClienteId() {
        return clienteId;
    }

    public void setClienteId(int clienteId) {
        this.clienteId = clienteId;
    }

    public Integer getEmpleadoInicioId() {
        return empleadoInicioId;
    }

    public void setEmpleadoInicioId(Integer empleadoInicioId) {
        this.empleadoInicioId = empleadoInicioId;
    }

    public Integer getEmpleadoFinId() {
        return empleadoFinId;
    }

    public void setEmpleadoFinId(Integer empleadoFinId) {
        this.empleadoFinId = empleadoFinId;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public BigDecimal getPrecioDiario() {
        return precioDiario;
    }

    public void setPrecioDiario(BigDecimal precioDiario) {
        this.precioDiario = precioDiario;
    }

    public BigDecimal getCostoTotal() {
        return costoTotal;
    }

    public void setCostoTotal(BigDecimal costoTotal) {
        this.costoTotal = costoTotal;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public void calcularCostoTotal() {
        if (fechaInicio != null && fechaFin != null && precioDiario != null) {
            long dias = fechaInicio.until(fechaFin).getDays();
            if (dias <= 0) dias = 1; // mínimo 1 día
            this.costoTotal = precioDiario.multiply(new BigDecimal(dias));
        }
    }

    public void finalizar(LocalDate fechaFin, int empleadoFinId) {
        this.fechaFin = fechaFin;
        this.empleadoFinId = empleadoFinId;
        this.estado = "FINALIZADO";
        calcularCostoTotal();
    }

    //testeo
    @Override
    public String toString() {
        return "Alquiler{" +
                "id=" + id +
                ", vehiculoId=" + vehiculoId +
                ", clienteId=" + clienteId +
                ", empleadoInicioId=" + empleadoInicioId +
                ", empleadoFinId=" + empleadoFinId +
                ", fechaInicio=" + fechaInicio +
                ", fechaFin=" + fechaFin +
                ", precioDiario=" + precioDiario +
                ", costoTotal=" + costoTotal +
                ", estado='" + estado + '\'' +
                ", notas='" + notas + '\'' +
                ", fechaCreacion=" + fechaCreacion +
                '}';
    }
}