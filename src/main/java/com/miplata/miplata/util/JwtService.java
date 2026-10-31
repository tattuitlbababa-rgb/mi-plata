package com.miplata.miplata.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey clave;
    private final long horas;

    public JwtService(@Value("${jwt.secret}") String secreto,
                      @Value("${jwt.expiracion-horas}") long horas) {
        this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        this.horas = horas;
    }

    public String crearToken(int usuarioId, String nombre) {
        Date ahora = new Date();
        return Jwts.builder()
                .subject(String.valueOf(usuarioId))
                .claim("nombre", nombre)
                .issuedAt(ahora)
                .expiration(new Date(ahora.getTime() + horas * 3600_000L))
                .signWith(clave)
                .compact();
    }

    public Claims leer(String token) {
        return Jwts.parser()
                .verifyWith(clave)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}