package com.miplata.miplata.excepcion;

// Datos que no cumplen las reglas de negocio (monto <= 0, fecha inválida, nombre muy corto...)
public class DatosInvalidosException extends NegocioException {

    public DatosInvalidosException(String mensaje) {
        super(mensaje);
    }
}