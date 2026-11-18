package com.miplata.miplata.excepcion;

public class CategoriaNoEncontradaException extends NegocioException {

    public CategoriaNoEncontradaException(int id) {
        super("No se encontró la categoría " + id);
    }
}