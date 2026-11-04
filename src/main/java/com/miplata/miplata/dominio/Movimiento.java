package com.miplata.miplata.dominio;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "movimientos")
public class Movimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int idMovimiento;

    @Column(name = "descripcion", length = 150, nullable = false)
    private String descripcion;

    @Column(name = "importe", nullable = false)
    private double monto;

    // Solo fecha, sin hora: DATE en MySQL
    @Temporal(TemporalType.DATE)
    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "tipo", length = 10, nullable = false)
    private String tipo;

    // Muchos movimientos pertenecen a un usuario
    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    // En la base la categoría se guarda como texto (nombre de la categoría)
    @Column(name = "categoria", length = 60, nullable = false)
    private String categoriaNombre;

    // Campo calculado: no se guarda en la base
    @Transient
    private double saldoTemporal;

    protected Movimiento() {
    }

    public Movimiento(String descripcion, double monto, LocalDate fecha, String tipo, Categoria categoria) {
        this.descripcion = descripcion;
        this.monto = monto;
        this.fecha = fecha;
        this.tipo = tipo;
        this.categoriaNombre = categoria.getNombre();
        this.usuario = categoria.getUsuario();
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

    public Usuario getUsuario() {
        return usuario;
    }

    public int getIdUsuario() {
        return usuario.getIdUsuario();
    }

    public String getCategoriaNombre() {
        return categoriaNombre;
    }

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

    // Suma los ingresos y resta los gastos de una lista de movimientos
    public static double calcularSaldo(List<Movimiento> movimientos) {
        double saldo = 0;
        for (Movimiento m : movimientos) {
            saldo += "Ingreso".equals(m.getTipo()) ? m.getMonto() : -m.getMonto();
        }
        return saldo;
    }
}