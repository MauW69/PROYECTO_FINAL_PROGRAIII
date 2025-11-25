package com.example.proyecto_final_prograiii.DTO;

import java.math.BigDecimal;
import java.time.LocalDate;

public class AlquilerSolicitudDTO {
    private int id;
    private int vehiculoId;           // <--- nuevo
    private String nombreCliente;
    private String nombreVehiculo;
    private LocalDate fechaInicio;
    private BigDecimal precioDiario;
    private String estado;

    public AlquilerSolicitudDTO(int id, int vehiculoId, String nombreCliente, String nombreVehiculo,
                                LocalDate fechaInicio, BigDecimal precioDiario, String estado) {
        this.id = id;
        this.vehiculoId = vehiculoId;
        this.nombreCliente = nombreCliente;
        this.nombreVehiculo = nombreVehiculo;
        this.fechaInicio = fechaInicio;
        this.precioDiario = precioDiario;
        this.estado = estado;
    }

    // getters
    public int getId() { return id; }
    public int getVehiculoId() { return vehiculoId; } // <--- getter nuevo
    public String getNombreCliente() { return nombreCliente; }
    public String getNombreVehiculo() { return nombreVehiculo; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public BigDecimal getPrecioDiario() { return precioDiario; }
    public String getEstado() { return estado; }
}

