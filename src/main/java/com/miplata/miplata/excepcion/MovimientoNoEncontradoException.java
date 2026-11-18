package com.miplata.miplata.excepcion;

// Se lanza cuando se busca un movimiento que no existe o que es de otro usuario
public class MovimientoNoEncontradoException extends NegocioException {

    public MovimientoNoEncontradoException(int id) {
        super("No se encontró el movimiento " + id);
    }
}