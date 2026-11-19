package com.miplata.miplata.dto;

import com.miplata.miplata.dominio.Movimiento;

import java.util.List;

// Todo lo que muestra la pantalla de estadística, calculado por EstadisticaService
public class EstadisticaResumen {

    // Una fila de "gastos por categoría": nombre, total y porcentaje sobre el total de gastos
    public static class Fila {
        private final String nombre;
        private final double total;
        private final double porcentaje;

        public Fila(String nombre, double total, double porcentaje) {
            this.nombre = nombre;
            this.total = total;
            this.porcentaje = porcentaje;
        }

        public String getNombre() { return nombre; }
        public double getTotal() { return total; }
        public double getPorcentaje() { return porcentaje; }
    }

    private final String mes;               // "" = todos los meses
    private final double ingresos;
    private final double gastos;
    private final double saldo;             // siempre del historial completo
    private final long cantidad;            // movimientos del período
    private final List<Fila> gastosPorCategoria;
    private final List<Movimiento> mayoresGastos;

    public EstadisticaResumen(String mes, double ingresos, double gastos, double saldo, long cantidad,
                              List<Fila> gastosPorCategoria, List<Movimiento> mayoresGastos) {
        this.mes = mes;
        this.ingresos = ingresos;
        this.gastos = gastos;
        this.saldo = saldo;
        this.cantidad = cantidad;
        this.gastosPorCategoria = gastosPorCategoria;
        this.mayoresGastos = mayoresGastos;
    }

    public String getMes() { return mes; }
    public double getIngresos() { return ingresos; }
    public double getGastos() { return gastos; }
    public double getSaldo() { return saldo; }
    public long getCantidad() { return cantidad; }
    public List<Fila> getGastosPorCategoria() { return gastosPorCategoria; }
    public List<Movimiento> getMayoresGastos() { return mayoresGastos; }
}