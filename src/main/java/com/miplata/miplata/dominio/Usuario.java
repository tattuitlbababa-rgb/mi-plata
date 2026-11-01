package com.miplata.miplata.dominio;

import jakarta.persistence.*;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private int idUsuario;

    @Column(name = "nombre", length = 100, nullable = false)
    private String nombre;

    // En la base la columna se llama "correo"; en el documento aparece como "email"
    @Column(name = "correo", length = 100, nullable = false, unique = true)
    private String email;

    @Column(name = "dni", length = 8, nullable = false)
    private String dni;

    // En la base se guarda como clave_hash, cifrada. Nunca se guarda en texto plano
    @Column(name = "clave_hash", length = 255, nullable = false)
    private String contraseña;

    // JPA necesita un constructor sin parámetros para crear el objeto al leer de la base
    protected Usuario() {
    }

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

    public boolean nombreValido() {
        return nombre != null && nombre.trim().length() >= 2
                && nombre.matches("[A-Za-zÁÉÍÓÚáéíóúÑñÜü\\s'.-]+");
    }

    public boolean emailValido() {
        return email != null && email.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]{2,}");
    }

    public boolean dniValido() {
        return dni != null && dni.matches("\\d{7,8}");
    }

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