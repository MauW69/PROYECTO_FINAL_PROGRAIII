package com.example.proyecto_final_prograiii.models;

public class Renta {
    private String modelo;
    private String placa;
    private String inicio;
    private String fin;
    private String total;

    public Renta(String modelo, String placa, String inicio, String fin, String total) {
        this.modelo = modelo;
        this.placa = placa;
        this.inicio = inicio;
        this.fin = fin;
        this.total = total;
    }

    public String getModelo() { return modelo; }
    public String getPlaca() { return placa; }
    public String getInicio() { return inicio; }
    public String getFin() { return fin; }
    public String getTotal() { return total; }
}
