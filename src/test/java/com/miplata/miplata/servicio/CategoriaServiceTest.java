package com.miplata.miplata.servicio;

import com.miplata.miplata.dominio.Categoria;
import com.miplata.miplata.dominio.Movimiento;
import com.miplata.miplata.dominio.Usuario;
import com.miplata.miplata.dto.CategoriaResumen;
import com.miplata.miplata.excepcion.CategoriaDuplicadaException;
import com.miplata.miplata.excepcion.CategoriaEnUsoException;
import com.miplata.miplata.excepcion.DatosInvalidosException;
import com.miplata.miplata.repositorio.CategoriaRepository;
import com.miplata.miplata.repositorio.MovimientoRepository;
import com.miplata.miplata.repositorio.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// Pruebas de CategoriaService. Los repositorios son simulados (mocks).
class CategoriaServiceTest {

    private CategoriaRepository categorias;
    private MovimientoRepository movimientos;
    private CategoriaService servicio;
    private Usuario ana;

    @BeforeEach
    void preparar() {
        categorias = mock(CategoriaRepository.class);
        movimientos = mock(MovimientoRepository.class);
        UsuarioRepository usuarios = mock(UsuarioRepository.class);
        servicio = new CategoriaService(categorias, movimientos, usuarios);

        ana = new Usuario("Ana Perez", "ana@mail.com", "12345678", "secreta");
        ana.setIdUsuario(1);
        when(usuarios.findById(1)).thenReturn(Optional.of(ana));
    }

    private Categoria categoria(int id, String nombre, String tipo) {
        Categoria c = new Categoria(nombre, tipo, ana);
        c.setIdCategoria(id);
        return c;
    }

    private Movimiento movimiento(String tipo, double monto, String categoria) {
        return new Movimiento("Prueba", monto, LocalDate.of(2026, 11, 1), tipo, categoria, ana);
    }

    @Test
    void crearUnaCategoriaNuevaLaGuardaSinEspaciosSobrantes() {
        when(categorias.save(any(Categoria.class))).thenAnswer(inv -> inv.getArgument(0));

        Categoria creada = servicio.crear(1, "  Mascotas ", "Gasto");

        assertEquals("Mascotas", creada.getNombre());
        verify(categorias).save(any(Categoria.class));
    }

    @Test
    void noSePuedeCrearUnaCategoriaConNombreDeUnaPredefinida() {
        // "otros" existe como predefinida (sin importar mayúsculas)
        assertThrows(CategoriaDuplicadaException.class, () -> servicio.crear(1, "otros", "Gasto"));
        // y tampoco se distingue la tilde: "Alimentación" ya existe
        assertThrows(CategoriaDuplicadaException.class, () -> servicio.crear(1, "alimentacion", "Gasto"));
    }

    @Test
    void unNombreMuyCortoEsInvalido() {
        assertThrows(DatosInvalidosException.class, () -> servicio.crear(1, "A", "Gasto"));
    }

    @Test
    void noSePuedeEliminarUnaCategoriaConMovimientos() {
        Categoria mascotas = categoria(3, "Mascotas", "Gasto");
        when(categorias.findById(3)).thenReturn(Optional.of(mascotas));
        when(movimientos.existsByUsuarioIdUsuarioAndTipoAndCategoriaNombre(1, "Gasto", "Mascotas"))
                .thenReturn(true);

        assertThrows(CategoriaEnUsoException.class, () -> servicio.eliminar(1, 3));

        verify(categorias, never()).delete(any(Categoria.class));
    }

    @Test
    void unaCategoriaSinMovimientosSePuedeEliminar() {
        Categoria mascotas = categoria(3, "Mascotas", "Gasto");
        when(categorias.findById(3)).thenReturn(Optional.of(mascotas));

        servicio.eliminar(1, 3);

        verify(categorias).delete(mascotas);
    }

    @Test
    void renombrarActualizaLaCategoriaYSusMovimientos() {
        Categoria mascotas = categoria(3, "Mascotas", "Gasto");
        when(categorias.findById(3)).thenReturn(Optional.of(mascotas));

        servicio.renombrar(1, 3, "Animales");

        assertEquals("Animales", mascotas.getNombre());
        verify(movimientos).renombrarCategoria(1, "Gasto", "Mascotas", "Animales");
    }

    @Test
    void resumenAgrupaGastosPorCategoriaYOrdenaPorTotal() {
        when(movimientos.findByUsuarioIdUsuarioOrderByFechaDescIdMovimientoDesc(1)).thenReturn(List.of(
                movimiento("Gasto", 100, "Alimentación"),
                movimiento("Gasto", 200, "Alimentación"),
                movimiento("Gasto", 50, "Servicios"),
                movimiento("Ingreso", 5000, "Trabajo")));

        List<CategoriaResumen> filas = servicio.resumenPorTipo(1, "Gasto");

        assertEquals("Alimentación", filas.get(0).getNombre());
        assertEquals(2L, filas.get(0).getCantidad());
        assertEquals(300.0, filas.get(0).getTotal(), 0.001);
        assertEquals("Servicios", filas.get(1).getNombre());
        assertEquals(50.0, filas.get(1).getTotal(), 0.001);
    }
}