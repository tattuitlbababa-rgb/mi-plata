package com.miplata.miplata.dominio;

public class Usuario {

    private int idUsuario;
    private String nombre;
    private String email;
    private String dni;
    private String contraseña;

    public Usuario(String nombre, String email, String dni, String contraseña) {
        this.nombre = nombre;
        this.email = email;
        this.dni = dni;
        this.contraseña = contraseña;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getContraseña() {
        return contraseña;
    }

    public void setContraseña(String contraseña) {
        this.contraseña = contraseña;
    }

    // Valida que el nombre tenga al menos 2 letras y solo letras y espacios
    public boolean nombreValido() {
        return nombre != null && nombre.trim().length() >= 2
                && nombre.matches("[A-Za-zÁÉÍÓÚáéíóúÑñÜü\\s'.-]+");
    }

    // Valida que el email tenga un formato correcto
    public boolean emailValido() {
        return email != null && email.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]{2,}");
    }

    // Valida que el DNI tenga 7 u 8 dígitos
    public boolean dniValido() {
        return dni != null && dni.matches("\\d{7,8}");
    }

    // Valida que la contraseña tenga al menos 6 caracteres
    public boolean contraseñaValida() {
        return contraseña != null && contraseña.length() >= 6;
    }

    public void registrarUsuario() {
        if (!nombreValido() || !emailValido() || !dniValido() || !contraseñaValida()) {
            throw new IllegalArgumentException("Datos de usuario inválidos");
        }
    }

    public void modificarDatos(String nombre, String email, String dni) {
        this.nombre = nombre;
        this.email = email;
        this.dni = dni;
        registrarUsuario();
    }

    public boolean iniciarSesion(String emailIngresado, String contraseñaIngresada) {
        return this.email.equals(emailIngresado) && this.contraseña.equals(contraseñaIngresada);
    }
}