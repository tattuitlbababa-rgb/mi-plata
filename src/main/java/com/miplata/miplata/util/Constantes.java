package com.miplata.miplata.util;

import java.util.List;
import java.util.Map;

public final class Constantes {

    public static final List<String> TIPOS = List.of("Ingreso", "Gasto");

    // Las mismas que usan ingresar.html y gastos.html
    public static final Map<String, List<String>> PREDEFINIDAS = Map.of(
            "Ingreso", List.of("Trabajo", "Ventas", "Regalo", "Otros"),
            "Gasto", List.of("Alimentación", "Transporte", "Servicios", "Salud", "Entretenimiento", "Otros")
    );

    private Constantes() {}
}