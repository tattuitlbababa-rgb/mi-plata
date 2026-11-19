package com.miplata.miplata.servicio;

import com.miplata.miplata.dominio.Movimiento;
import com.miplata.miplata.dominio.Usuario;
import com.miplata.miplata.dto.EstadisticaResumen;
import com.miplata.miplata.repositorio.MovimientoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// Pruebas de EstadisticaService (las operaciones con Streams)
class EstadisticaServiceTest {

    private MovimientoRepository movimientos;
    private EstadisticaService servicio;
    private Usuario ana;

    @BeforeEach
    void preparar() {
        movimientos = mock(MovimientoRepository.class);
        servicio = new EstadisticaService(movimientos);

        ana = new Usuario("Ana Perez", "ana@mail.com", "12345678", "secreta");
        ana.setIdUsuario(1);
    }

    private Movimiento mov(String tipo, double monto, String categoria, LocalDate fecha) {
        return new Movimiento("Prueba", monto, fecha, tipo, categoria, ana);
    }

    private void historial(Movimiento... lista) {
        when(movimientos.findByUsuarioIdUsuarioOrderByFechaDescIdMovimientoDesc(1)).thenReturn(List.of(lista));
    }

    @Test
    void resumenCalculaTotalesCantidadSaldoYPorcentajes() {
        historial(
                mov("Ingreso", 1000, "Trabajo", LocalDate.of(2026, 11, 1)),
                mov("Gasto", 300, "Alimentación", LocalDate.of(2026, 11, 2)),
                mov("Gasto", 200, "Servicios", LocalDate.of(2026, 11, 3)),
                mov("Gasto", 100, "Alimentación", LocalDate.of(2026, 11, 4)),
                mov("Gasto", 50, "Salud", LocalDate.of(2026, 11, 5)));

        EstadisticaResumen r = servicio.resumen(1, "");

        assertEquals(1000.0, r.getIngresos(), 0.001);
        assertEquals(650.0, r.getGastos(), 0.001);
        assertEquals(350.0, r.getSaldo(), 0.001);
        assertEquals(5L, r.getCantidad());

        // Tres categorías de gasto; la mayor es Alimentación (300 + 100 = 400 de 650)
        assertEquals(3, r.getGastosPorCategoria().size());
        assertEquals("Alimentación", r.getGastosPorCategoria().get(0).getNombre());
        assertEquals(400.0, r.getGastosPorCategoria().get(0).getTotal(), 0.001);
        assertEquals(61.54, r.getGastosPorCategoria().get(0).getPorcentaje(), 0.01);

        // Los 3 gastos más altos, de mayor a menor
        assertEquals(3, r.getMayoresGastos().size());
        assertEquals(300.0, r.getMayoresGastos().get(0).getMonto(), 0.001);
        assertEquals(200.0, r.getMayoresGastos().get(1).getMonto(), 0.001);
        assertEquals(100.0, r.getMayoresGastos().get(2).getMonto(), 0.001);
    }

    @Test
    void resumenFiltraPorMesPeroElSaldoEsDelHistorialCompleto() {
        historial(
                mov("Ingreso", 1000, "Trabajo", LocalDate.of(2026, 11, 1)),
                mov("Gasto", 300, "Alimentación", LocalDate.of(2026, 11, 2)),
                mov("Gasto", 999, "Alimentación", LocalDate.of(2026, 10, 15)));

        EstadisticaResumen noviembre = servicio.resumen(1, "2026-11");
        EstadisticaResumen octubre = servicio.resumen(1, "2026-10");
        EstadisticaResumen invalido = servicio.resumen(1, "abc");

        assertEquals(300.0, noviembre.getGastos(), 0.001);
        assertEquals(2L, noviembre.getCantidad());
        assertEquals(999.0, octubre.getGastos(), 0.001);
        assertEquals(1L, octubre.getCantidad());

        // El saldo no depende del mes: 1000 - 300 - 999
        assertEquals(-299.0, noviembre.getSaldo(), 0.001);

        // Un mes con formato inválido se toma como "todos los meses"
        assertEquals(3L, invalido.getCantidad());
    }

    @Test
    void mesesSeListanDelMasRecienteAlMasAntiguoSinRepetir() {
        historial(
                mov("Gasto", 10, "Otros", LocalDate.of(2026, 10, 15)),
                mov("Gasto", 10, "Otros", LocalDate.of(2026, 11, 1)),
                mov("Gasto", 10, "Otros", LocalDate.of(2026, 11, 20)));

        assertEquals(List.of("2026-11", "2026-10"), servicio.meses(1));
    }
}