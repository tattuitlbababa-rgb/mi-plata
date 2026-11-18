package com.miplata.miplata.excepcion;

// Base de las excepciones propias de Mi Plata. El mensaje está pensado para mostrarse al usuario.
public class NegocioException extends RuntimeException {

    public NegocioException(String mensaje) {
        super(mensaje);
    }
}