package com.miplata.miplata.dominio;

import jakarta.persistence.*;

@Entity
@Table(name = "categorias")
public class Categoria {

       @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int idCategoria;

    @Column(name = "nombre", length = 60, nullable = false)
    private String nombre;

    @Column(name = "tipo", length = 10, nullable = false)
    private String tipo;

    // Muchas categorías pueden pertenecer a un mismo usuario
    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    protected Categoria() {
    }

    public Categoria(String nombre, String tipo, Usuario usuario) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.usuario = usuario;
    }

    public int getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(int idCategoria) {
        this.idCategoria = idCategoria;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTipo() {
        return tipo;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public int getIdUsuario() {
        return usuario.getIdUsuario();
    }

    public boolean nombreValido() {
        return nombre != null && nombre.trim().length() >= 2 && nombre.trim().length() <= 60;
    }

    public boolean tipoValido() {
        return "Ingreso".equals(tipo) || "Gasto".equals(tipo);
    }

    public void crearCategoria() {
        if (!nombreValido() || !tipoValido()) {
            throw new IllegalArgumentException("Datos de categoría inválidos");
        }
    }

    public void modificarCategoria(String nuevoNombre) {
        this.nombre = nuevoNombre;
        crearCategoria();
    }
}