package com.miplata.miplata.web;

import com.miplata.miplata.excepcion.MovimientoNoEncontradoException;
import com.miplata.miplata.servicio.MovimientoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/movimientos")
public class MovimientosWebController {

    private final MovimientoService servicio;

    public MovimientosWebController(MovimientoService servicio) {
        this.servicio = servicio;
    }

    // GET /movimientos?q=texto  (q es opcional)
    @GetMapping
    public String listar(@SessionAttribute(AuthWebController.SESION_USUARIO_ID) int usuarioId,
                         @RequestParam(defaultValue = "") String q, Model model) {
        model.addAttribute("movimientos", servicio.buscar(usuarioId, q));
        model.addAttribute("q", q);
        return "movimientos";
    }

    // POST /movimientos/{id}/eliminar  -> vuelve al listado con un aviso
    @PostMapping("/{id}/eliminar")
    public String eliminar(@SessionAttribute(AuthWebController.SESION_USUARIO_ID) int usuarioId,
                           @PathVariable int id, RedirectAttributes aviso) {
        try {
            servicio.eliminar(usuarioId, id);
            aviso.addFlashAttribute("ok", "Movimiento eliminado");
        } catch (MovimientoNoEncontradoException e) {
            aviso.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/movimientos";
    }
}