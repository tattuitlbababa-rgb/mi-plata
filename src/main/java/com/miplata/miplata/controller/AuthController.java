package com.miplata.miplata.controller;

import com.miplata.miplata.util.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JdbcTemplate db;
    private final JwtService jwt;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AuthController(JdbcTemplate db, JwtService jwt) {
        this.db = db;
        this.jwt = jwt;
    }

    public record RegistroRequest(String nombre, String dni, String correo, String clave) {}
    public record LoginRequest(String correo, String clave) {}

    // POST /api/auth/registro
    @PostMapping("/registro")
    public ResponseEntity<Map<String, Object>> registro(@RequestBody RegistroRequest req) {
        String nombre = limpiar(req.nombre());
        String dni = limpiar(req.dni());
        String correo = limpiar(req.correo()).toLowerCase();
        String clave = req.clave() == null ? "" : req.clave();

        if (nombre.length() < 2 || !nombre.matches("[A-Za-zÁÉÍÓÚáéíóúÑñÜü\\s'.-]+")) {
            throw error(HttpStatus.BAD_REQUEST, "Nombre inválido");
        }
        if (!dni.matches("\\d{7,8}")) {
            throw error(HttpStatus.BAD_REQUEST, "El DNI debe tener 7 u 8 dígitos");
        }
        if (!correo.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]{2,}")) {
            throw error(HttpStatus.BAD_REQUEST, "Correo inválido");
        }
        if (clave.length() < 6) {
            throw error(HttpStatus.BAD_REQUEST, "La clave debe tener al menos 6 caracteres");
        }

        Integer existentes = db.queryForObject(
                "SELECT COUNT(*) FROM usuarios WHERE correo = ?", Integer.class, correo);
        if (existentes != null && existentes > 0) {
            throw error(HttpStatus.CONFLICT, "Ese correo ya está registrado");
        }

        String hash = encoder.encode(clave);
        KeyHolder kh = new GeneratedKeyHolder();
        db.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO usuarios (nombre, dni, correo, clave_hash) VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, nombre);
            ps.setString(2, dni);
            ps.setString(3, correo);
            ps.setString(4, hash);
            return ps;
        }, kh);

        int id = kh.getKey().intValue();
        return ResponseEntity.status(HttpStatus.CREATED).body(respuestaSesion(id, nombre));
    }

    // POST /api/auth/login
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginRequest req) {
        String correo = limpiar(req.correo()).toLowerCase();
        String clave = req.clave() == null ? "" : req.clave();

        List<Map<String, Object>> filas = db.queryForList(
                "SELECT id, nombre, clave_hash FROM usuarios WHERE correo = ?", correo);

        // Mismo mensaje si el correo no existe o la clave es incorrecta
        if (filas.isEmpty() || !encoder.matches(clave, (String) filas.get(0).get("clave_hash"))) {
            throw error(HttpStatus.UNAUTHORIZED, "Correo o clave incorrectos");
        }

        Map<String, Object> fila = filas.get(0);
        return respuestaSesion(((Number) fila.get("id")).intValue(), (String) fila.get("nombre"));
    }

    private Map<String, Object> respuestaSesion(int id, String nombre) {
        return Map.of(
                "token", jwt.crearToken(id, nombre),
                "usuario", Map.of("id", id, "nombre", nombre)
        );
    }

    private String limpiar(String texto) {
        return texto == null ? "" : texto.trim();
    }

    private ResponseStatusException error(HttpStatus estado, String mensaje) {
        return new ResponseStatusException(estado, mensaje);
    }
}