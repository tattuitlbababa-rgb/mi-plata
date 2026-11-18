package com.miplata.miplata.web;

import com.miplata.miplata.excepcion.NegocioException;
import com.miplata.miplata.servicio.CategoriaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/categorias")
public class CategoriasWebController {

    private final CategoriaService servicio;

    public CategoriasWebController(CategoriaService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public String listar(@SessionAttribute(AuthWebController.SESION_USUARIO_ID) int usuarioId, Model model) {
        model.addAttribute("ingresos", servicio.resumenPorTipo(usuarioId, "Ingreso"));
        model.addAttribute("gastos", servicio.resumenPorTipo(usuarioId, "Gasto"));
        return "categorias";
    }

    @PostMapping
    public String crear(@SessionAttribute(AuthWebController.SESION_USUARIO_ID) int usuarioId,
                        @RequestParam String nombre, @RequestParam String tipo,
                        RedirectAttributes aviso) {
        try {
            servicio.crear(usuarioId, nombre, tipo);
            aviso.addFlashAttribute("ok", "Categoría creada");
        } catch (NegocioException e) {
            aviso.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/categorias";
    }

    @PostMapping("/{id}/editar")
    public String editar(@SessionAttribute(AuthWebController.SESION_USUARIO_ID) int usuarioId,
                         @PathVariable int id, @RequestParam String nombre,
                         RedirectAttributes aviso) {
        try {
            servicio.renombrar(usuarioId, id, nombre);
            aviso.addFlashAttribute("ok", "Categoría actualizada");
        } catch (NegocioException e) {
            aviso.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/categorias";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@SessionAttribute(AuthWebController.SESION_USUARIO_ID) int usuarioId,
                           @PathVariable int id, RedirectAttributes aviso) {
        try {
            servicio.eliminar(usuarioId, id);
            aviso.addFlashAttribute("ok", "Categoría eliminada");
        } catch (NegocioException e) {
            aviso.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/categorias";
    }
}