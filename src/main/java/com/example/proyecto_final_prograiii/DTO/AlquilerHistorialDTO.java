package com.example.proyecto_final_prograiii.DTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class AlquilerHistorialDTO {

    private int alquilerId;
    private int vehiculoId;
    private String nombreVehiculo; // "Corolla (ABC123)"
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private long diasTotales;
    private BigDecimal montoPagado;
    private String metodoPago;
    private String estado;
    private String cliente;

    public AlquilerHistorialDTO(
            int alquilerId,
            int vehiculoId,
            String nombreVehiculo,
            String cliente,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            BigDecimal montoPagado,
            String metodoPago,
            String estado
    ) {
        this.alquilerId = alquilerId;
        this.vehiculoId = vehiculoId;
        this.nombreVehiculo = nombreVehiculo;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.montoPagado = montoPagado;
        this.metodoPago = metodoPago;
        this.estado = estado;
        this.cliente = cliente;

        this.diasTotales = (fechaInicio != null && fechaFin != null)
                ? ChronoUnit.DAYS.between(fechaInicio, fechaFin) + 1 : 0;
    }
    public int getAlquilerId() {
        return alquilerId;
    }

    public int getVehiculoId() {
        return vehiculoId;
    }

    public String getNombreVehiculo() {
        return nombreVehiculo;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public long getDiasTotales() {
        return diasTotales;
    }

    public BigDecimal getMontoPagado() {
        return montoPagado;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public String getEstado() {
        return estado;
    }

    public String getCliente() {
        return cliente;
    }
}


