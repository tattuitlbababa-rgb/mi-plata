package com.miplata.miplata.web;

import com.miplata.miplata.dto.MovimientoForm;
import com.miplata.miplata.excepcion.NegocioException;
import com.miplata.miplata.servicio.CategoriaService;
import com.miplata.miplata.servicio.MovimientoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// Formularios para registrar ingresos (/ingresar) y gastos (/gastos).
// Los dos usan la misma página y el mismo servicio; solo cambia el tipo.
@Controller
public class RegistroMovimientoWebController {

    private final MovimientoService movimientos;
    private final CategoriaService categorias;

    public RegistroMovimientoWebController(MovimientoService movimientos, CategoriaService categorias) {
        this.movimientos = movimientos;
        this.categorias = categorias;
    }

    @GetMapping("/ingresar")
    public String formularioIngreso(@SessionAttribute(AuthWebController.SESION_USUARIO_ID) int usuarioId,
                                    Model model) {
        return preparar("Ingreso", usuarioId, new MovimientoForm(), model);
    }

    @PostMapping("/ingresar")
    public String guardarIngreso(@SessionAttribute(AuthWebController.SESION_USUARIO_ID) int usuarioId,
                                 @ModelAttribute("form") MovimientoForm form, BindingResult enlace,
                                 Model model, RedirectAttributes aviso) {
        return guardar("Ingreso", usuarioId, form, model, aviso);
    }

    @GetMapping("/gastos")
    public String formularioGasto(@SessionAttribute(AuthWebController.SESION_USUARIO_ID) int usuarioId,
                                  Model model) {
        return preparar("Gasto", usuarioId, new MovimientoForm(), model);
    }

    @PostMapping("/gastos")
    public String guardarGasto(@SessionAttribute(AuthWebController.SESION_USUARIO_ID) int usuarioId,
                               @ModelAttribute("form") MovimientoForm form, BindingResult enlace,
                               Model model, RedirectAttributes aviso) {
        return guardar("Gasto", usuarioId, form, model, aviso);
    }

    private String guardar(String tipo, int usuarioId, MovimientoForm form,
                           Model model, RedirectAttributes aviso) {
        try {
            movimientos.registrar(usuarioId, tipo, form);
            aviso.addFlashAttribute("ok", tipo + " registrado");
            return "redirect:/movimientos";
        } catch (NegocioException e) {
            // Vuelve al formulario conservando lo que el usuario había escrito
            model.addAttribute("error", e.getMessage());
            return preparar(tipo, usuarioId, form, model);
        }
    }

    private String preparar(String tipo, int usuarioId, MovimientoForm form, Model model) {
        boolean esIngreso = "Ingreso".equals(tipo);
        model.addAttribute("form", form);
        model.addAttribute("tipo", tipo);
        model.addAttribute("titulo", esIngreso ? "INGRESAR DINERO" : "REGISTRAR GASTOS");
        model.addAttribute("subtitulo", esIngreso ? "Registrar ingreso" : "Anotar tus compras");
        model.addAttribute("accion", esIngreso ? "/ingresar" : "/gastos");
        model.addAttribute("categorias", categorias.nombresPorTipo(usuarioId, tipo));
        return "movimiento-form";
    }
}