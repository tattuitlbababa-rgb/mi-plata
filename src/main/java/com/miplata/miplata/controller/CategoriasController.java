package com.miplata.miplata.controller;

import com.miplata.miplata.dominio.Categoria;
import com.miplata.miplata.dominio.Usuario;
import com.miplata.miplata.repositorio.CategoriaRepository;
import com.miplata.miplata.repositorio.MovimientoRepository;
import com.miplata.miplata.repositorio.UsuarioRepository;
import com.miplata.miplata.util.Constantes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categorias")
public class CategoriasController {

    private final CategoriaRepository categorias;
    private final MovimientoRepository movimientos;
    private final UsuarioRepository usuarios;

    public CategoriasController(CategoriaRepository categorias,
                                MovimientoRepository movimientos,
                                UsuarioRepository usuarios) {
        this.categorias = categorias;
        this.movimientos = movimientos;
        this.usuarios = usuarios;
    }

    public record CategoriaRequest(String nombre, String tipo) {}

    // GET /api/categorias → predefinidas y propias del usuario
    @GetMapping
    public Map<String, Object> listar(@RequestAttribute("usuarioId") int usuarioId) {
        List<Map<String, Object>> propias = categorias.findByUsuarioIdUsuario(usuarioId).stream()
                .map(c -> Map.<String, Object>of(
                        "id", c.getIdCategoria(),
                        "nombre", c.getNombre(),
                        "tipo", c.getTipo()))
                .toList();

        return Map.of("predefinidas", Constantes.PREDEFINIDAS, "propias", propias);
    }

    // POST /api/categorias
    @PostMapping
    public ResponseEntity<Map<String, Object>> crear(@RequestAttribute("usuarioId") int usuarioId,
                                                     @RequestBody CategoriaRequest req) {
        String nombre = req.nombre() == null ? "" : req.nombre().trim();
        String tipo = req.tipo();

        Usuario usuario = usuarios.findById(usuarioId)
                .orElseThrow(() -> error(HttpStatus.UNAUTHORIZED, "Sesión inválida"));

        // Creo el objeto y dejo que la entidad valide sus datos
        Categoria nueva = new Categoria(nombre, tipo, usuario);
        try {
            nueva.crearCategoria();
        } catch (IllegalArgumentException e) {
            throw error(HttpStatus.BAD_REQUEST, e.getMessage());
        }

        // Verifico duplicados contra las predefinidas y las propias del usuario
        List<String> existentes = new ArrayList<>(Constantes.PREDEFINIDAS.get(tipo));
        categorias.findByUsuarioIdUsuarioAndTipo(usuarioId, tipo)
                .forEach(c -> existentes.add(c.getNombre()));

        String buscada = normalizar(nombre);
        if (existentes.stream().anyMatch(n -> normalizar(n).equals(buscada))) {
            throw error(HttpStatus.CONFLICT, "Esa categoría ya existe para este tipo");
        }

        Categoria guardada = categorias.save(nueva);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("id", guardada.getIdCategoria(), "nombre", nombre, "tipo", tipo));
    }

        // DELETE /api/categorias/{id}  (solo si no tiene movimientos)
    @DeleteMapping("/{id}")
    public Map<String, Object> eliminar(@RequestAttribute("usuarioId") int usuarioId,
                                        @PathVariable int id) {
        Categoria categoria = categorias.findById(id)
                .filter(c -> c.getIdUsuario() == usuarioId)
                .orElseThrow(() -> error(HttpStatus.NOT_FOUND, "Categoría no encontrada"));

        // Los movimientos guardan el nombre de la categoría, no su id
        if (movimientos.existsByUsuarioIdUsuarioAndTipoAndCategoriaNombre(
                usuarioId, categoria.getTipo(), categoria.getNombre())) {
            throw error(HttpStatus.CONFLICT, "La categoría tiene movimientos y no se puede eliminar");
        }

        categorias.delete(categoria);
        return Map.of("ok", true);
    }
    private static String normalizar(String texto) {
        return texto.toLowerCase().replaceAll("\\p{M}", "").trim();
    }

    private static ResponseStatusException error(HttpStatus estado, String mensaje) {
        return new ResponseStatusException(estado, mensaje);
    }
}