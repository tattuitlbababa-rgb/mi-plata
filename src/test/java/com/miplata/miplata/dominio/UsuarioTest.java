package com.miplata.miplata.dominio;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

// Pruebas de las validaciones de la clase de dominio Usuario
class UsuarioTest {

    private Usuario con(String nombre, String email, String dni, String clave) {
        return new Usuario(nombre, email, dni, clave);
    }

    @Test
    void usuarioConDatosCorrectosSePuedeRegistrar() {
        assertDoesNotThrow(() ->
                con("Ana Perez", "ana@mail.com", "12345678", "secreta").registrarUsuario());
    }

    @Test
    void nombreConNumerosOMuyCortoEsInvalido() {
        assertFalse(con("Ana1", "a@b.com", "12345678", "secreta").nombreValido());
        assertFalse(con("A", "a@b.com", "12345678", "secreta").nombreValido());
        assertTrue(con("José Núñez", "a@b.com", "12345678", "secreta").nombreValido());
    }

    @Test
    void dniDebeTenerSieteOOchoDigitos() {
        assertTrue(con("Ana", "a@b.com", "1234567", "secreta").dniValido());
        assertTrue(con("Ana", "a@b.com", "12345678", "secreta").dniValido());
        assertFalse(con("Ana", "a@b.com", "123456", "secreta").dniValido());
        assertFalse(con("Ana", "a@b.com", "123456789", "secreta").dniValido());
        assertFalse(con("Ana", "a@b.com", "12a45678", "secreta").dniValido());
    }

    @Test
    void emailDebeTenerArrobaYDominio() {
        assertTrue(con("Ana", "ana@mail.com", "12345678", "secreta").emailValido());
        assertFalse(con("Ana", "ana-sin-arroba", "12345678", "secreta").emailValido());
        assertFalse(con("Ana", "ana@mail", "12345678", "secreta").emailValido());
    }

    @Test
    void claveDebeTenerAlMenosSeisCaracteres() {
        assertTrue(con("Ana", "a@b.com", "12345678", "123456").contraseñaValida());
        assertFalse(con("Ana", "a@b.com", "12345678", "12345").contraseñaValida());
    }

    @Test
    void registrarUsuarioInvalidoLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () ->
                con("Ana", "mail-sin-arroba", "12345678", "secreta").registrarUsuario());
    }
}