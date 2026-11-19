package com.miplata.miplata.dominio;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// Pruebas de la clase de dominio Movimiento (no necesitan base de datos)
class MovimientoTest {

    private Usuario usuario() {
        Usuario u = new Usuario("Ana Perez", "ana@mail.com", "12345678", "secreta");
        u.setIdUsuario(1);
        return u;
    }

    private Movimiento gasto(double monto) {
        return new Movimiento("Supermercado", monto, LocalDate.of(2026, 11, 1),
                "Gasto", "Alimentación", usuario());
    }

    @Test
    void movimientoConDatosCorrectosEsValido() {
        assertTrue(gasto(1500).esValido());
        assertDoesNotThrow(() -> gasto(1500).registrarMovimiento());
    }

    @Test
    void importeCeroONegativoEsInvalido() {
        assertThrows(IllegalArgumentException.class, () -> gasto(0).registrarMovimiento());
        assertThrows(IllegalArgumentException.class, () -> gasto(-50).registrarMovimiento());
    }

    @Test
    void descripcionVaciaOTipoInvalidoEsInvalido() {
        Movimiento sinDescripcion = new Movimiento("   ", 100, LocalDate.now(), "Gasto", "Otros", usuario());
        Movimiento tipoRaro = new Movimiento("Algo", 100, LocalDate.now(), "Prestamo", "Otros", usuario());

        assertFalse(sinDescripcion.esValido());
        assertFalse(tipoRaro.esValido());
    }

    @Test
    void saldoEsIngresosMenosGastos() {
        Movimiento ingreso = new Movimiento("Sueldo", 1000, LocalDate.of(2026, 11, 1),
                "Ingreso", "Trabajo", usuario());

        double saldo = Movimiento.calcularSaldo(List.of(ingreso, gasto(300), gasto(200)));

        assertEquals(500.0, saldo, 0.001);
    }

    @Test
    void saldoDeListaVaciaEsCero() {
        assertEquals(0.0, Movimiento.calcularSaldo(List.of()), 0.001);
    }

    @Test
    void modificarCambiaFechaDescripcionEImporte() {
        Movimiento m = gasto(1500);

        m.modificarMovimiento("Verdulería", 2000, LocalDate.of(2026, 11, 5));

        assertEquals("Verdulería", m.getDescripcion());
        assertEquals(2000.0, m.getMonto(), 0.001);
        assertEquals(LocalDate.of(2026, 11, 5), m.getFecha());
    }

    @Test
    void modificarConImporteInvalidoLanzaExcepcion() {
        Movimiento m = gasto(1500);

        assertThrows(IllegalArgumentException.class,
                () -> m.modificarMovimiento("Verdulería", 0, LocalDate.of(2026, 11, 5)));
    }
}