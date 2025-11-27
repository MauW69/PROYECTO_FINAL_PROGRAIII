package com.example.proyecto_final_prograiii.DTO;

import java.math.BigDecimal;


public class VehiculoEstadisticaDTO {

    private String nombreVehiculo;

    private String placa;

    private String estado;

    private int cantidadRentas;

    private BigDecimal ganancias;

    public VehiculoEstadisticaDTO(String nombreVehiculo, String placa, String estado, int cantidadRentas, BigDecimal ganancias) {
        this.nombreVehiculo = nombreVehiculo;
        this.placa = placa;
        this.estado = estado;
        this.cantidadRentas = cantidadRentas;
        this.ganancias = ganancias;
    }

    public String getNombreVehiculo() {
        return nombreVehiculo;
    }
    public void setNombreVehiculo(String nombreVehiculo) {
        this.nombreVehiculo = nombreVehiculo;
    }
    public String getPlaca() {
        return placa;
    }
    public void setPlaca(String placa) {
        this.placa = placa;
    }
    public String getEstado() {
        return estado;
    }
    public void setEstado(String estado) {
        this.estado = estado;
    }
    public int getCantidadRentas() {
        return cantidadRentas;
    }
    public void setCantidadRentas(int cantidadRentas) {
        this.cantidadRentas = cantidadRentas;
    }
    public BigDecimal getGanancias() {
        return ganancias;
    }
    public void setGanancias(BigDecimal ganancias) {
        this.ganancias = ganancias;
    }
}