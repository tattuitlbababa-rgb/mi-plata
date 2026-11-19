package com.miplata.miplata.servicio;

import com.miplata.miplata.dominio.Movimiento;
import com.miplata.miplata.dominio.Usuario;
import com.miplata.miplata.dto.MovimientoForm;
import com.miplata.miplata.excepcion.DatosInvalidosException;
import com.miplata.miplata.excepcion.MovimientoNoEncontradoException;
import com.miplata.miplata.repositorio.MovimientoBusquedaDao;
import com.miplata.miplata.repositorio.MovimientoRepository;
import com.miplata.miplata.repositorio.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// Pruebas de MovimientoService. Los repositorios son simulados (mocks): no se usa la base real.
class MovimientoServiceTest {

    private MovimientoRepository movimientos;
    private UsuarioRepository usuarios;
    private CategoriaService categorias;
    private MovimientoService servicio;
    private Usuario ana;

    @BeforeEach
    void preparar() {
        movimientos = mock(MovimientoRepository.class);
        usuarios = mock(UsuarioRepository.class);
        categorias = mock(CategoriaService.class);
        servicio = new MovimientoService(movimientos, mock(MovimientoBusquedaDao.class), usuarios, categorias);

        ana = new Usuario("Ana Perez", "ana@mail.com", "12345678", "secreta");
        ana.setIdUsuario(1);
    }

    private MovimientoForm form(String fecha, String descripcion, String categoria, Double importe) {
        MovimientoForm f = new MovimientoForm();
        f.setFecha(fecha);
        f.setDescripcion(descripcion);
        f.setCategoria(categoria);
        f.setImporte(importe);
        return f;
    }

    private Movimiento movimiento(int id, String tipo, double monto, LocalDate fecha, Usuario duenio) {
        Movimiento m = new Movimiento("Prueba", monto, fecha, tipo, "Otros", duenio);
        m.setIdMovimiento(id);
        return m;
    }

    @Test
    void registrarGuardaUnMovimientoValido() {
        when(usuarios.findById(1)).thenReturn(Optional.of(ana));
        when(categorias.nombreExistente(1, "Gasto", "Alimentación")).thenReturn("Alimentación");
        when(movimientos.save(any(Movimiento.class))).thenAnswer(inv -> inv.getArgument(0));

        Movimiento guardado = servicio.registrar(1, "Gasto",
                form("2026-11-01", "Supermercado", "Alimentación", 1500.0));

        assertEquals(1500.0, guardado.getMonto(), 0.001);
        assertEquals("Alimentación", guardado.getCategoriaNombre());
        verify(movimientos).save(any(Movimiento.class));
    }

    @Test
    void registrarConImporteCeroLanzaExcepcionYNoGuarda() {
        assertThrows(DatosInvalidosException.class, () ->
                servicio.registrar(1, "Gasto", form("2026-11-01", "Supermercado", "Alimentación", 0.0)));

        verify(movimientos, never()).save(any(Movimiento.class));
    }

    @Test
    void registrarConFechaInvalidaLanzaExcepcion() {
        assertThrows(DatosInvalidosException.class, () ->
                servicio.registrar(1, "Gasto", form("31/12/2026", "Supermercado", "Alimentación", 100.0)));
    }

    @Test
    void modificarActualizaFechaDescripcionEImporteSinTocarLaCategoria() {
        Movimiento m = movimiento(5, "Gasto", 1500, LocalDate.of(2026, 11, 1), ana);
        when(movimientos.findById(5)).thenReturn(Optional.of(m));

        servicio.modificar(1, 5, form("2026-11-05", "Verdulería", "Alimentación", 2000.0));

        assertEquals("Verdulería", m.getDescripcion());
        assertEquals(2000.0, m.getMonto(), 0.001);
        assertEquals(LocalDate.of(2026, 11, 5), m.getFecha());
        assertEquals("Otros", m.getCategoriaNombre());
    }

    @Test
    void modificarConImporteInvalidoNoCambiaNada() {
        Movimiento m = movimiento(5, "Gasto", 1500, LocalDate.of(2026, 11, 1), ana);
        when(movimientos.findById(5)).thenReturn(Optional.of(m));

        assertThrows(DatosInvalidosException.class, () ->
                servicio.modificar(1, 5, form("2026-11-05", "Verdulería", "Alimentación", 0.0)));

        assertEquals(1500.0, m.getMonto(), 0.001);
        assertEquals("Prueba", m.getDescripcion());
    }

    @Test
    void noSePuedeEliminarElMovimientoDeOtroUsuario() {
        Usuario beto = new Usuario("Beto Gomez", "beto@mail.com", "87654321", "secreta");
        beto.setIdUsuario(2);
        Movimiento ajeno = movimiento(5, "Gasto", 100, LocalDate.now(), beto);
        when(movimientos.findById(5)).thenReturn(Optional.of(ajeno));

        assertThrows(MovimientoNoEncontradoException.class, () -> servicio.eliminar(1, 5));

        verify(movimientos, never()).delete(any(Movimiento.class));
    }

    @Test
    void eliminarElMovimientoPropioLoBorra() {
        Movimiento propio = movimiento(5, "Gasto", 100, LocalDate.now(), ana);
        when(movimientos.findById(5)).thenReturn(Optional.of(propio));

        servicio.eliminar(1, 5);

        verify(movimientos).delete(propio);
    }

    @Test
    void saldoSumaIngresosYRestaGastos() {
        when(movimientos.findByUsuarioIdUsuarioOrderByFechaDescIdMovimientoDesc(1)).thenReturn(List.of(
                movimiento(1, "Ingreso", 1000, LocalDate.of(2026, 11, 1), ana),
                movimiento(2, "Gasto", 300, LocalDate.of(2026, 11, 2), ana)));

        assertEquals(700.0, servicio.saldo(1), 0.001);
    }

    @Test
    void ultimosDevuelveLosMasRecientesOrdenadosPorFecha() {
        when(movimientos.findByUsuarioIdUsuarioOrderByFechaDescIdMovimientoDesc(1)).thenReturn(List.of(
                movimiento(1, "Gasto", 10, LocalDate.of(2026, 11, 1), ana),
                movimiento(2, "Gasto", 20, LocalDate.of(2026, 11, 10), ana),
                movimiento(3, "Gasto", 30, LocalDate.of(2026, 11, 5), ana)));

        List<Movimiento> resultado = servicio.ultimos(1, 2);

        assertEquals(2, resultado.size());
        assertEquals(LocalDate.of(2026, 11, 10), resultado.get(0).getFecha());
        assertEquals(LocalDate.of(2026, 11, 5), resultado.get(1).getFecha());
    }
}