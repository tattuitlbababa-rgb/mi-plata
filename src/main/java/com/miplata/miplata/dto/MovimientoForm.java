package com.miplata.miplata.dto;

import java.time.LocalDate;

// DTO: lleva los datos del formulario de ingreso/gasto desde la página hasta el servicio,
// sin exponer la entidad Movimiento directamente.
public class MovimientoForm {

    private String fecha = LocalDate.now().toString(); // por defecto, hoy (HU3)
    private String descripcion;
    private String categoria;
    private Double importe;

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public Double getImporte() { return importe; }
    public void setImporte(Double importe) { this.importe = importe; }
}