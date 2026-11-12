package com.miplata.miplata.controller;

import com.miplata.miplata.dominio.Movimiento;
import com.miplata.miplata.dominio.Usuario;
import com.miplata.miplata.repositorio.MovimientoRepository;
import com.miplata.miplata.repositorio.UsuarioRepository;
import com.miplata.miplata.util.Constantes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/movimientos")
public class MovimientosController {

    private static final DateTimeFormatter FORMATO_VISUAL = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final MovimientoRepository movimientos;
    private final UsuarioRepository usuarios;

    public MovimientosController(MovimientoRepository movimientos, UsuarioRepository usuarios) {
        this.movimientos = movimientos;
        this.usuarios = usuarios;
    }

    public record MovimientoRequest(String fecha, String descripcion, String categoria, String tipo, Double importe) {}

    // GET /api/movimientos?q=texto  (q es opcional)
    // La búsqueda se hace en Java con Stream: filter() sobre los movimientos del usuario
    @GetMapping
    public List<Map<String, Object>> listar(@RequestAttribute("usuarioId") int usuarioId,
                                            @RequestParam(defaultValue = "") String q) {
        String termino = normalizar(q);

        return movimientos.findByUsuarioIdUsuarioOrderByFechaDescIdMovimientoDesc(usuarioId).stream()
                .filter(m -> termino.isEmpty() || coincide(m, termino))
                .map(this::aMapa)
                .toList();
    }

    // POST /api/movimientos
    @PostMapping
    public ResponseEntity<Map<String, Object>> crear(@RequestAttribute("usuarioId") int usuarioId,
                                                     @RequestBody MovimientoRequest req) {
        String fecha = limpiar(req.fecha());
        String descripcion = limpiar(req.descripcion());
        String categoria = limpiar(req.categoria());
        String tipo = req.tipo();
        Double importe = req.importe();

        if (!fecha.matches("\\d{4}-\\d{2}-\\d{2}")) throw error(HttpStatus.BAD_REQUEST, "Fecha inválida");
        if (descripcion.isEmpty() || descripcion.length() > 150) throw error(HttpStatus.BAD_REQUEST, "La descripción es obligatoria");
        if (categoria.isEmpty() || categoria.length() > 60) throw error(HttpStatus.BAD_REQUEST, "La categoría es obligatoria");
        if (tipo == null || !Constantes.TIPOS.contains(tipo)) throw error(HttpStatus.BAD_REQUEST, "Tipo inválido");
        if (importe == null || importe <= 0) throw error(HttpStatus.BAD_REQUEST, "El importe tiene que ser mayor a cero");

        LocalDate fechaValida;
        try {
            fechaValida = LocalDate.parse(fecha);
        } catch (DateTimeParseException e) {
            throw error(HttpStatus.BAD_REQUEST, "Fecha inválida");
        }

        Usuario usuario = usuarios.findById(usuarioId)
                .orElseThrow(() -> error(HttpStatus.UNAUTHORIZED, "Sesión inválida"));

        Movimiento nuevo = new Movimiento(descripcion, importe, fechaValida, tipo, categoria, usuario);
        try {
            nuevo.registrarMovimiento();
        } catch (IllegalArgumentException e) {
            throw error(HttpStatus.BAD_REQUEST, e.getMessage());
        }

        Movimiento guardado = movimientos.save(nuevo);
        return ResponseEntity.status(HttpStatus.CREATED).body(aMapa(guardado));
    }

    // DELETE /api/movimientos/{id}
    @DeleteMapping("/{id}")
    public Map<String, Object> eliminar(@RequestAttribute("usuarioId") int usuarioId,
                                        @PathVariable int id) {
        Movimiento movimiento = movimientos.findById(id)
                .filter(m -> m.getIdUsuario() == usuarioId)
                .orElseThrow(() -> error(HttpStatus.NOT_FOUND, "Movimiento no encontrado"));

        movimientos.delete(movimiento);
        return Map.of("ok", true);
    }

    // Revisa si el texto buscado aparece en fecha, descripción, categoría, tipo o importe
    private boolean coincide(Movimiento m, String termino) {
        return Stream.of(
                        m.getFecha().format(FORMATO_VISUAL),
                        m.getDescripcion(),
                        m.getCategoriaNombre(),
                        m.getTipo(),
                        String.valueOf(m.getMonto()))
                .map(this::normalizar)
                .anyMatch(texto -> texto.contains(termino));
    }

    // Convierte el movimiento en el formato que espera movimientos.html
    private Map<String, Object> aMapa(Movimiento m) {
        return Map.<String, Object>of(
                "id", m.getIdMovimiento(),
                "fecha", m.getFecha().toString(),
                "descripcion", m.getDescripcion(),
                "categoria", m.getCategoriaNombre(),
                "tipo", m.getTipo(),
                "importe", m.getMonto());
    }

    // Pasa a minúsculas y quita acentos, para que la búsqueda no distinga mayúsculas ni tildes
    private String normalizar(String texto) {
        if (texto == null) return "";
        return Normalizer.normalize(texto.toLowerCase().trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
    }

    private static String limpiar(String texto) {
        return texto == null ? "" : texto.trim();
    }

    private static ResponseStatusException error(HttpStatus estado, String mensaje) {
        return new ResponseStatusException(estado, mensaje);
    }
}