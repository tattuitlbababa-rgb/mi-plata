package com.miplata.miplata.dominio;

public class Categoria {

    private int idCategoria;
    private String nombre;
    private int idUsuario;
    private String tipo;

    public Categoria(String nombre, String tipo, Usuario usuario) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.idUsuario = usuario.getIdUsuario();
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

    public int getIdUsuario() {
        return idUsuario;
    }

    public String getTipo() {
        return tipo;
    }

    // Valida que el nombre tenga entre 2 y 60 caracteres
    public boolean nombreValido() {
        return nombre != null && nombre.trim().length() >= 2 && nombre.trim().length() <= 60;
    }

    // Valida que el tipo sea Ingreso o Gasto
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

    // Una categoría solo se puede eliminar si no tiene movimientos asociados
    public boolean eliminarCategoria(java.util.List<Movimiento> movimientos) {
        boolean tieneMovimientos = movimientos.stream()
                .anyMatch(m -> m.getIdCategoria() == this.idCategoria);
        if (tieneMovimientos) {
            throw new IllegalStateException("La categoría tiene movimientos y no se puede eliminar");
        }
        return true;
    }
}