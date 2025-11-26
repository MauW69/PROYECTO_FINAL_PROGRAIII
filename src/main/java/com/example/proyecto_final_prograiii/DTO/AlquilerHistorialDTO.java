package com.example.proyecto_final_prograiii.DTO;

import java.math.BigDecimal;
import java.time.LocalDate;

public class AlquilerHistorialDTO {
    private int id;
    private String vehiculo;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private BigDecimal totalPagado;
    private String metodoPago;
    private String estado;

    public AlquilerHistorialDTO(int id, String vehiculo, LocalDate fechaInicio, LocalDate fechaFin,
                                BigDecimal totalPagado, String metodoPago, String estado) {
        this.id = id;
        this.vehiculo = vehiculo;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.totalPagado = totalPagado;
        this.metodoPago = metodoPago;
        this.estado = estado;
    }

    public int getId() { return id; }
    public String getVehiculo() { return vehiculo; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public LocalDate getFechaFin() { return fechaFin; }
    public BigDecimal getTotalPagado() { return totalPagado; }
    public String getMetodoPago() { return metodoPago; }
    public String getEstado() { return estado; }
}
