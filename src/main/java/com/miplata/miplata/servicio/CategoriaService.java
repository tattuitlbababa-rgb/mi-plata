package com.miplata.miplata.servicio;

import com.miplata.miplata.dominio.Categoria;
import com.miplata.miplata.dominio.Movimiento;
import com.miplata.miplata.dominio.Usuario;
import com.miplata.miplata.dto.CategoriaResumen;
import com.miplata.miplata.excepcion.*;
import com.miplata.miplata.repositorio.CategoriaRepository;
import com.miplata.miplata.repositorio.MovimientoRepository;
import com.miplata.miplata.repositorio.UsuarioRepository;
import com.miplata.miplata.util.Constantes;
import com.miplata.miplata.util.Texto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CategoriaService {

    private final CategoriaRepository categorias;
    private final MovimientoRepository movimientos;
    private final UsuarioRepository usuarios;

    public CategoriaService(CategoriaRepository categorias, MovimientoRepository movimientos,
                            UsuarioRepository usuarios) {
        this.categorias = categorias;
        this.movimientos = movimientos;
        this.usuarios = usuarios;
    }

    // Predefinidas + propias del usuario, para llenar el desplegable de ingresar/gastos
    public List<String> nombresPorTipo(int usuarioId, String tipo) {
        List<String> nombres = new ArrayList<>(Constantes.PREDEFINIDAS.getOrDefault(tipo, List.of()));
        categorias.findByUsuarioIdUsuarioAndTipo(usuarioId, tipo).stream()
                .map(Categoria::getNombre)
                .forEach(nombres::add);
        return nombres;
    }

    // Devuelve el nombre tal como está guardado, o lanza excepción si la categoría no existe para ese tipo
    public String nombreExistente(int usuarioId, String tipo, String nombre) {
        String buscada = Texto.normalizar(nombre);
        return nombresPorTipo(usuarioId, tipo).stream()
                .filter(n -> Texto.normalizar(n).equals(buscada))
                .findFirst()
                .orElseThrow(() -> new DatosInvalidosException("Elegí una categoría válida"));
    }

    // Una fila por categoría (predefinidas, propias y las usadas en movimientos),
    // con cantidad de movimientos y total, ordenadas de mayor a menor total.
    public List<CategoriaResumen> resumenPorTipo(int usuarioId, String tipo) {
        List<Movimiento> delTipo = movimientos
                .findByUsuarioIdUsuarioOrderByFechaDescIdMovimientoDesc(usuarioId).stream()
                .filter(m -> tipo.equals(m.getTipo()))
                .toList();

        Map<String, Long> cantidades = delTipo.stream()
                .collect(Collectors.groupingBy(Movimiento::getCategoriaNombre, Collectors.counting()));
        Map<String, Double> totales = delTipo.stream()
                .collect(Collectors.groupingBy(Movimiento::getCategoriaNombre,
                        Collectors.summingDouble(Movimiento::getMonto)));
        Map<String, Categoria> propias = categorias.findByUsuarioIdUsuarioAndTipo(usuarioId, tipo).stream()
                .collect(Collectors.toMap(Categoria::getNombre, c -> c, (a, b) -> a));

        Set<String> nombres = new LinkedHashSet<>(Constantes.PREDEFINIDAS.getOrDefault(tipo, List.of()));
        nombres.addAll(propias.keySet());
        nombres.addAll(cantidades.keySet());

        return nombres.stream()
                .map(n -> {
                    Categoria propia = propias.get(n);
                    return new CategoriaResumen(
                            propia == null ? null : propia.getIdCategoria(),
                            n,
                            cantidades.getOrDefault(n, 0L),
                            totales.getOrDefault(n, 0.0),
                            propia != null);
                })
                .sorted(Comparator.comparingDouble(CategoriaResumen::getTotal).reversed()
                        .thenComparing(CategoriaResumen::getNombre))
                .toList();
    }

    @Transactional
    public Categoria crear(int usuarioId, String nombre, String tipo) {
        Usuario usuario = usuarios.findById(usuarioId)
                .orElseThrow(() -> new DatosInvalidosException("Sesión inválida"));

        Categoria nueva = new Categoria(nombre == null ? "" : nombre.trim(), tipo, usuario);
        if (!nueva.tipoValido()) throw new DatosInvalidosException("Tipo de categoría inválido");
        if (!nueva.nombreValido()) throw new DatosInvalidosException("El nombre debe tener entre 2 y 60 caracteres");
        if (existe(usuarioId, tipo, nueva.getNombre(), null)) throw new CategoriaDuplicadaException();

        return categorias.save(nueva);
    }

    @Transactional
    public void renombrar(int usuarioId, int id, String nuevoNombre) {
        Categoria categoria = propia(usuarioId, id);
        String viejo = categoria.getNombre();
        String nombre = nuevoNombre == null ? "" : nuevoNombre.trim();

        if (nombre.length() < 2 || nombre.length() > 60) {
            throw new DatosInvalidosException("El nombre debe tener entre 2 y 60 caracteres");
        }
        if (existe(usuarioId, categoria.getTipo(), nombre, id)) throw new CategoriaDuplicadaException();

        categoria.modificarCategoria(nombre);  // método del dominio
        movimientos.renombrarCategoria(usuarioId, categoria.getTipo(), viejo, nombre);
    }

    @Transactional
    public void eliminar(int usuarioId, int id) {
        Categoria categoria = propia(usuarioId, id);

        // Los movimientos guardan el nombre de la categoría, no su id
        if (movimientos.existsByUsuarioIdUsuarioAndTipoAndCategoriaNombre(
                usuarioId, categoria.getTipo(), categoria.getNombre())) {
            throw new CategoriaEnUsoException();
        }
        categorias.delete(categoria);
    }

    private Categoria propia(int usuarioId, int id) {
        return categorias.findById(id)
                .filter(c -> c.getIdUsuario() == usuarioId)
                .orElseThrow(() -> new CategoriaNoEncontradaException(id));
    }

    // ¿Ya existe una categoría con ese nombre (predefinida o propia) para ese tipo?
    // excluirId permite ignorar la propia categoría cuando se la está renombrando.
    private boolean existe(int usuarioId, String tipo, String nombre, Integer excluirId) {
        String buscada = Texto.normalizar(nombre);

        boolean enPredefinidas = Constantes.PREDEFINIDAS.getOrDefault(tipo, List.of()).stream()
                .anyMatch(n -> Texto.normalizar(n).equals(buscada));

        boolean enPropias = categorias.findByUsuarioIdUsuarioAndTipo(usuarioId, tipo).stream()
                .filter(c -> excluirId == null || c.getIdCategoria() != excluirId)
                .anyMatch(c -> Texto.normalizar(c.getNombre()).equals(buscada));

        return enPredefinidas || enPropias;
    }
}