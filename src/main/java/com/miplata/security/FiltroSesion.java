package com.miplata.miplata.security;

import com.miplata.miplata.util.JwtService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// Exige un token válido en las rutas /api, excepto /api/auth
@Component
public class FiltroSesion extends OncePerRequestFilter {

    private final JwtService jwt;

    public FiltroSesion(JwtService jwt) {
        this.jwt = jwt;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest req) {
        String ruta = req.getRequestURI();
        return !ruta.startsWith("/api/") || ruta.startsWith("/api/auth/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        String encabezado = req.getHeader("Authorization");
        if (encabezado == null || !encabezado.startsWith("Bearer ")) {
            responder(res, 401, "Sesión no iniciada");
            return;
        }

        int usuarioId;
        try {
            usuarioId = Integer.parseInt(jwt.leer(encabezado.substring(7)).getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            responder(res, 401, "Sesión inválida o vencida");
            return;
        }

        req.setAttribute("usuarioId", usuarioId);
        chain.doFilter(req, res);
    }

    private void responder(HttpServletResponse res, int estado, String mensaje) throws IOException {
        res.setStatus(estado);
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        res.getWriter().write("{\"error\":\"" + mensaje + "\"}");
    }
}