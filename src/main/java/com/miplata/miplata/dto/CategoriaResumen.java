package com.miplata.miplata.dto;

// Una fila de la pantalla de categorías: nombre, cuántos movimientos tiene y su total
public class CategoriaResumen {

    private final Integer id;      // null si es una categoría predefinida
    private final String nombre;
    private final long cantidad;
    private final double total;
    private final boolean propia;

    public CategoriaResumen(Integer id, String nombre, long cantidad, double total, boolean propia) {
        this.id = id;
        this.nombre = nombre;
        this.cantidad = cantidad;
        this.total = total;
        this.propia = propia;
    }

    public Integer getId() { return id; }
    public String getNombre() { return nombre; }
    public long getCantidad() { return cantidad; }
    public double getTotal() { return total; }
    public boolean isPropia() { return propia; }
}