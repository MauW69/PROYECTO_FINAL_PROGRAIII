package com.example.proyecto_final_prograiii.DTO;

import java.math.BigDecimal;

public class ClienteEstadisticaDTO {

    private String nombreCompleto;
    private String nombreUsuario;
    private int cantidadRentas;
    private BigDecimal importeTotal;

    public ClienteEstadisticaDTO(String nombreCompleto, String nombreUsuario, int cantidadRentas, BigDecimal importeTotal) {
        this.nombreCompleto = nombreCompleto;
        this.nombreUsuario = nombreUsuario;
        this.cantidadRentas = cantidadRentas;
        this.importeTotal = importeTotal;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public int getCantidadRentas() {
        return cantidadRentas;
    }

    public BigDecimal getImporteTotal() {
        return importeTotal;
    }
}
