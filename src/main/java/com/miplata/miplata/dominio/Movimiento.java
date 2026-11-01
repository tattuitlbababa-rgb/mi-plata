package com.miplata.miplata.dominio;

import java.time.LocalDate;

public class Movimiento {

    private int idMovimiento;
    private String descripcion;
    private double monto;
    private LocalDate fecha;
    private String tipo;
    private int idUsuario;
    private int idCategoria;

    public Movimiento(String descripcion, double monto, LocalDate fecha, String tipo, Categoria categoria) {
        this.descripcion = descripcion;
        this.monto = monto;
        this.fecha = fecha;
        this.tipo = tipo;
        this.idCategoria = categoria.getIdCategoria();
        this.idUsuario = categoria.getIdUsuario();
    }

    public int getIdMovimiento() {
        return idMovimiento;
    }

    public void setIdMovimiento(int idMovimiento) {
        this.idMovimiento = idMovimiento;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public double getMonto() {
        return monto;
    }

    public void setMonto(double monto) {
        this.monto = monto;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public int getIdCategoria() {
        return idCategoria;
    }

    // Valida que el monto sea mayor a cero, la descripción no esté vacía y el tipo sea Ingreso o Gasto
    public boolean esValido() {
        return monto > 0
                && descripcion != null && !descripcion.trim().isEmpty() && descripcion.length() <= 150
                && fecha != null
                && ("Ingreso".equals(tipo) || "Gasto".equals(tipo));
    }

    public void registrarMovimiento() {
        if (!esValido()) {
            throw new IllegalArgumentException("Datos de movimiento inválidos");
        }
    }

    public void modificarMovimiento(String descripcion, double monto, LocalDate fecha) {
        this.descripcion = descripcion;
        this.monto = monto;
        this.fecha = fecha;
        registrarMovimiento();
    }

    public void eliminarMovimiento() {
        this.idMovimiento = 0;
    }

    // Suma los ingresos y resta los gastos de una lista de movimientos
    public static double calcularSaldo(java.util.List<Movimiento> movimientos) {
        double saldo = 0;
        for (Movimiento m : movimientos) {
            saldo += "Ingreso".equals(m.getTipo()) ? m.getMonto() : -m.getMonto();
        }
        return saldo;
    }
}