package com.miplata.miplata.excepcion;

public class CategoriaEnUsoException extends NegocioException {

    public CategoriaEnUsoException() {
        super("La categoría tiene movimientos y no se puede eliminar");
    }
}