package com.miplata.miplata.excepcion;

public class CategoriaDuplicadaException extends NegocioException {

    public CategoriaDuplicadaException() {
        super("Esa categoría ya existe para este tipo");
    }
}