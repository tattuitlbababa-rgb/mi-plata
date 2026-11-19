package com.miplata.miplata.web;

import com.miplata.miplata.servicio.MovimientoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

@Controller
public class InicioWebController {

    private final MovimientoService servicio;

    public InicioWebController(MovimientoService servicio) {
        this.servicio = servicio;
    }

    // La dirección raíz lleva al inicio (y si no hay sesión, el interceptor manda al login)
    @GetMapping("/")
    public String raiz() {
        return "redirect:/inicio";
    }

    @GetMapping("/inicio")
    public String inicio(@SessionAttribute(AuthWebController.SESION_USUARIO_ID) int usuarioId,
                         Model model) {
        model.addAttribute("saldo", servicio.saldo(usuarioId));
        model.addAttribute("ultimos", servicio.ultimos(usuarioId, 3));
        return "inicio";
    }
}