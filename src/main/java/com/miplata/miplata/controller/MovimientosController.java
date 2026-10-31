package com.miplata.miplata.controller;

import com.miplata.miplata.util.Constantes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
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
@RequestMapping("/api/movimientos")
public class MovimientosController {

    private final JdbcTemplate db;

    public MovimientosController(JdbcTemplate db) {
        this.db = db;
    }

    public record MovimientoRequest(String fecha, String descripcion, String categoria, String tipo, Double importe) {}

    private static final RowMapper<Map<String, Object>> MAPPER = (rs, i) -> Map.of(
            "id", rs.getInt("id"),
            "fecha", rs.getString("fecha"),
            "descripcion", rs.getString("descripcion"),
            "categoria", rs.getString("categoria"),
            "tipo", rs.getString("tipo"),
            "importe", rs.getDouble("importe")
    );

    // GET /api/movimientos?q=texto  (q es opcional)
    @GetMapping
    public List<Map<String, Object>> listar(@RequestAttribute("usuarioId") int usuarioId,
                                            @RequestParam(defaultValue = "") String q) {
        String termino = q.trim();
        List<Object> params = new ArrayList<>();
        params.add(usuarioId);

        StringBuilder sql = new StringBuilder(
                "SELECT id, DATE_FORMAT(fecha, '%Y-%m-%d') AS fecha, descripcion, categoria, tipo, importe " +
                "FROM movimientos WHERE usuario_id = ?");

        if (!termino.isEmpty()) {
            String like = "%" + termino + "%";
            sql.append(" AND (descripcion LIKE ? OR categoria LIKE ? OR tipo LIKE ? " +
                       "OR DATE_FORMAT(fecha, '%d/%m/%Y') LIKE ? OR CAST(importe AS CHAR) LIKE ?)");
            for (int i = 0; i < 5; i++) params.add(like);
        }

        sql.append(" ORDER BY fecha DESC, id DESC");
        return db.query(sql.toString(), MAPPER, params.toArray());
    }

    // POST /api/movimientos
    @PostMapping
    public ResponseEntity<Map<String, Object>> crear(@RequestAttribute("usuarioId") int usuarioId,
                                                     @RequestBody MovimientoRequest req) {
        String fecha = limpiar(req.fecha());
        String descripcion = limpiar(req.descripcion());
        String categoria = limpiar(req.categoria());
        String tipo = req.tipo();
        Double importeReq = req.importe();

        if (!fecha.matches("\\d{4}-\\d{2}-\\d{2}")) throw error(HttpStatus.BAD_REQUEST, "Fecha inválida");
        if (descripcion.isEmpty() || descripcion.length() > 150) throw error(HttpStatus.BAD_REQUEST, "La descripción es obligatoria");
        if (categoria.isEmpty() || categoria.length() > 60) throw error(HttpStatus.BAD_REQUEST, "La categoría es obligatoria");
        if (tipo == null || !Constantes.TIPOS.contains(tipo)) throw error(HttpStatus.BAD_REQUEST, "Tipo inválido");
        if (importeReq == null || importeReq <= 0) throw error(HttpStatus.BAD_REQUEST, "El importe tiene que ser mayor a cero");

        double importe = importeReq;
        KeyHolder kh = new GeneratedKeyHolder();
        db.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO movimientos (usuario_id, fecha, descripcion, categoria, tipo, importe) " +
                    "VALUES (?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, usuarioId);
            ps.setString(2, fecha);
            ps.setString(3, descripcion);
            ps.setString(4, categoria);
            ps.setString(5, tipo);
            ps.setDouble(6, importe);
            return ps;
        }, kh);

        int id = kh.getKey().intValue();
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", id,
                "fecha", fecha,
                "descripcion", descripcion,
                "categoria", categoria,
                "tipo", tipo,
                "importe", importe
        ));
    }

    // DELETE /api/movimientos/{id}
    @DeleteMapping("/{id}")
    public Map<String, Object> eliminar(@RequestAttribute("usuarioId") int usuarioId,
                                        @PathVariable int id) {
        int filas = db.update("DELETE FROM movimientos WHERE id = ? AND usuario_id = ?", id, usuarioId);
        if (filas == 0) throw error(HttpStatus.NOT_FOUND, "Movimiento no encontrado");
        return Map.of("ok", true);
    }

    private static String limpiar(String texto) {
        return texto == null ? "" : texto.trim();
    }

    private static ResponseStatusException error(HttpStatus estado, String mensaje) {
        return new ResponseStatusException(estado, mensaje);
    }
}