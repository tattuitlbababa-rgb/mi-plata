package com.miplata.miplata.web;

import com.miplata.miplata.dto.EstadisticaResumen;
import com.miplata.miplata.servicio.EstadisticaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;

@Controller
public class EstadisticaWebController {

    private final EstadisticaService servicio;

    public EstadisticaWebController(EstadisticaService servicio) {
        this.servicio = servicio;
    }

    // GET /estadistica?mes=2026-11  (mes es opcional)
    @GetMapping("/estadistica")
    public String ver(@SessionAttribute(AuthWebController.SESION_USUARIO_ID) int usuarioId,
                      @RequestParam(defaultValue = "") String mes, Model model) {
        EstadisticaResumen resumen = servicio.resumen(usuarioId, mes);
        model.addAttribute("resumen", resumen);
        model.addAttribute("meses", servicio.meses(usuarioId));
        model.addAttribute("mes", resumen.getMes());
        return "estadistica";
    }
}