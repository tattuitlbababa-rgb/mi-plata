package com.miplata.miplata.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

// Antes de entrar a una página protegida, revisa que haya una sesión iniciada.
// Si no la hay, redirige al login.
public class AutenticacionInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler)
            throws IOException {
        HttpSession sesion = req.getSession(false);
        if (sesion == null || sesion.getAttribute(AuthWebController.SESION_USUARIO_ID) == null) {
            res.sendRedirect(req.getContextPath() + "/login");
            return false;
        }
        return true;
    }
}