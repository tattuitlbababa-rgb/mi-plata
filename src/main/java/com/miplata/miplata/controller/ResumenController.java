package com.miplata.miplata.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/resumen")
public class ResumenController {

    private final JdbcTemplate db;

    public ResumenController(JdbcTemplate db) {
        this.db = db;
    }

    // GET /api/resumen?mes=2026-10  (mes es opcional)
    @GetMapping
    public Map<String, Object> resumen(@RequestAttribute("usuarioId") int usuarioId,
                                       @RequestParam(defaultValue = "") String mes) {

        // Saldo: siempre del historial completo
        Double saldo = db.queryForObject(
                "SELECT COALESCE(SUM(CASE WHEN tipo = 'Ingreso' THEN importe ELSE -importe END), 0) " +
                "FROM movimientos WHERE usuario_id = ?",
                Double.class, usuarioId);

        // Totales del período
        boolean filtrarMes = mes.matches("\\d{4}-\\d{2}");
        String sql = "SELECT tipo, categoria, SUM(importe) AS total FROM movimientos WHERE usuario_id = ?";
        List<Object> params = new ArrayList<>();
        params.add(usuarioId);

        if (filtrarMes) {
            sql += " AND DATE_FORMAT(fecha, '%Y-%m') = ?";
            params.add(mes);
        }
        sql += " GROUP BY tipo, categoria";

        double ingresos = 0;
        double gastos = 0;
        List<Map<String, Object>> gastosPorCategoria = new ArrayList<>();

        for (Map<String, Object> fila : db.queryForList(sql, params.toArray())) {
            double total = ((Number) fila.get("total")).doubleValue();
            if ("Ingreso".equals(fila.get("tipo"))) {
                ingresos += total;
            } else {
                gastos += total;
                gastosPorCategoria.add(Map.of("categoria", fila.get("categoria"), "total", total));
            }
        }

        gastosPorCategoria.sort((a, b) ->
                Double.compare((Double) b.get("total"), (Double) a.get("total")));

        List<String> meses = db.queryForList(
                "SELECT DISTINCT DATE_FORMAT(fecha, '%Y-%m') AS mes FROM movimientos " +
                "WHERE usuario_id = ? ORDER BY mes DESC", String.class, usuarioId);

        return Map.of(
                "saldo", saldo == null ? 0.0 : saldo,
                "ingresos", ingresos,
                "gastos", gastos,
                "gastosPorCategoria", gastosPorCategoria,
                "meses", meses
        );
    }
}