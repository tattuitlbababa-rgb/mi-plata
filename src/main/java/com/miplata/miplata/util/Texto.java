package com.miplata.miplata.util;

import java.text.Normalizer;

public final class Texto {

    // Minúsculas y sin tildes, para comparar textos sin distinguir mayúsculas ni acentos
    public static String normalizar(String texto) {
        if (texto == null) return "";
        return Normalizer.normalize(texto.toLowerCase().trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
    }

    private Texto() {}
}