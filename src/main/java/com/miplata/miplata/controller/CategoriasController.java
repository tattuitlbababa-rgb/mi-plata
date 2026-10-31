package com.miplata.miplata.controller;

import com.miplata.miplata.util.Constantes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categorias")
public class CategoriasController {

    private final JdbcTemplate db;

    public CategoriasController(JdbcTemplate db) {
        this.db = db;
    }

    public record CategoriaRequest(String nombre, String tipo) {}

    // GET /api/categorias → predefinidas y propias del usuario
    @GetMapping
    public Map<String, Object> listar(@RequestAttribute("usuarioId") int usuarioId) {
        List<Map<String, Object>> propias = db.queryForList(
                "SELECT id, nombre, tipo FROM categorias WHERE usuario_id = ? ORDER BY nombre", usuarioId);
        return Map.of("predefinidas", Constantes.PREDEFINIDAS, "propias", propias);
    }

    // POST /api/categorias
    @PostMapping
    public ResponseEntity<Map<String, Object>> crear(@RequestAttribute("usuarioId") int usuarioId,
                                                     @RequestBody CategoriaRequest req) {
        String nombre = req.nombre() == null ? "" : req.nombre().trim();
        String tipo = req.tipo();

        if (nombre.length() < 2 || nombre.length() > 60) {
            throw error(HttpStatus.BAD_REQUEST, "El nombre tiene que tener entre 2 y 60 caracteres");
        }
        if (tipo == null || !Constantes.TIPOS.contains(tipo)) {
            throw error(HttpStatus.BAD_REQUEST, "Tipo inválido");
        }

        List<String> propias = db.queryForList(
                "SELECT nombre FROM categorias WHERE usuario_id = ? AND tipo = ?", String.class, usuarioId, tipo);

        List<String> existentes = new ArrayList<>(Constantes.PREDEFINIDAS.get(tipo));
        existentes.addAll(propias);

        String buscada = normalizar(nombre);
        if (existentes.stream().anyMatch(n -> normalizar(n).equals(buscada))) {
            throw error(HttpStatus.CONFLICT, "Esa categoría ya existe para este tipo");
        }

        KeyHolder kh = new GeneratedKeyHolder();
        db.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO categorias (usuario_id, nombre, tipo) VALUES (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, usuarioId);
            ps.setString(2, nombre);
            ps.setString(3, tipo);
            return ps;
        }, kh);

        int id = kh.getKey().intValue();
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", id, "nombre", nombre, "tipo", tipo));
    }

    // DELETE /api/categorias/{id}  (solo si no tiene movimientos)
    @DeleteMapping("/{id}")
    public Map<String, Object> eliminar(@RequestAttribute("usuarioId") int usuarioId,
                                        @PathVariable int id) {
        List<Map<String, Object>> filas = db.queryForList(
                "SELECT nombre, tipo FROM categorias WHERE id = ? AND usuario_id = ?", id, usuarioId);
        if (filas.isEmpty()) throw error(HttpStatus.NOT_FOUND, "Categoría no encontrada");

        String nombre = (String) filas.get(0).get("nombre");
        String tipo = (String) filas.get(0).get("tipo");

        Integer uso = db.queryForObject(
                "SELECT COUNT(*) FROM movimientos WHERE usuario_id = ? AND tipo = ? AND categoria = ?",
                Integer.class, usuarioId, tipo, nombre);
        if (uso != null && uso > 0) {
            throw error(HttpStatus.CONFLICT, "La categoría tiene movimientos y no se puede eliminar");
        }

        db.update("DELETE FROM categorias WHERE id = ? AND usuario_id = ?", id, usuarioId);
        return Map.of("ok", true);
    }

    private static String normalizar(String texto) {
        return texto.toLowerCase().replaceAll("\\p{M}", "").trim();
    }

    private static ResponseStatusException error(HttpStatus estado, String mensaje) {
        return new ResponseStatusException(estado, mensaje);
    }
}