package com.miplata.miplata.web;

import com.miplata.miplata.dominio.Movimiento;
import com.miplata.miplata.dto.MovimientoForm;
import com.miplata.miplata.excepcion.MovimientoNoEncontradoException;
import com.miplata.miplata.excepcion.NegocioException;
import com.miplata.miplata.servicio.MovimientoService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/movimientos")
public class MovimientosWebController {

    private final MovimientoService servicio;

    public MovimientosWebController(MovimientoService servicio) {
        this.servicio = servicio;
    }

    // GET /movimientos?q=texto&tipo=Gasto&desde=2026-11-01&hasta=2026-11-30  (todo opcional)
    @GetMapping
    public String listar(@SessionAttribute(AuthWebController.SESION_USUARIO_ID) int usuarioId,
                         @RequestParam(defaultValue = "") String q,
                         @RequestParam(defaultValue = "") String tipo,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                         Model model) {
        model.addAttribute("movimientos", servicio.buscar(usuarioId, q, tipo, desde, hasta));
        model.addAttribute("q", q);
        model.addAttribute("tipo", tipo);
        model.addAttribute("desde", desde);
        model.addAttribute("hasta", hasta);
        model.addAttribute("filtrado", !q.isBlank() || !tipo.isBlank() || desde != null || hasta != null);
        return "movimientos";
    }

    // GET /movimientos/5/editar -> formulario con los datos actuales
    @GetMapping("/{id}/editar")
    public String formularioEditar(@SessionAttribute(AuthWebController.SESION_USUARIO_ID) int usuarioId,
                                   @PathVariable int id, Model model, RedirectAttributes aviso) {
        try {
            Movimiento m = servicio.obtener(usuarioId, id);
            MovimientoForm form = new MovimientoForm();
            form.setFecha(m.getFecha().toString());
            form.setDescripcion(m.getDescripcion());
            form.setCategoria(m.getCategoriaNombre());
            form.setImporte(m.getMonto());
            return preparar(usuarioId, id, form, model);
        } catch (MovimientoNoEncontradoException e) {
            aviso.addFlashAttribute("error", e.getMessage());
            return "redirect:/movimientos";
        }
    }

    // POST /movimientos/5/editar -> guarda los cambios
    @PostMapping("/{id}/editar")
    public String editar(@SessionAttribute(AuthWebController.SESION_USUARIO_ID) int usuarioId,
                         @PathVariable int id,
                         @ModelAttribute("form") MovimientoForm form, BindingResult enlace,
                         Model model, RedirectAttributes aviso) {
        try {
            servicio.modificar(usuarioId, id, form);
            aviso.addFlashAttribute("ok", "Movimiento actualizado");
            return "redirect:/movimientos";
        } catch (MovimientoNoEncontradoException e) {
            aviso.addFlashAttribute("error", e.getMessage());
            return "redirect:/movimientos";
        } catch (NegocioException e) {
            // Dato inválido: vuelve al formulario conservando lo escrito
            model.addAttribute("error", e.getMessage());
            return preparar(usuarioId, id, form, model);
        }
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

    private String preparar(int usuarioId, int id, MovimientoForm form, Model model) {
        Movimiento m = servicio.obtener(usuarioId, id);
        model.addAttribute("form", form);
        model.addAttribute("id", id);
        model.addAttribute("tipo", m.getTipo());
        model.addAttribute("categoriaNombre", m.getCategoriaNombre());
        return "movimiento-editar";
    }
}