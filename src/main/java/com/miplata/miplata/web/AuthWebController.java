package com.miplata.miplata.web;

import com.miplata.miplata.dominio.Usuario;
import com.miplata.miplata.repositorio.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

// Login, registro y cierre de sesión con páginas Thymeleaf.
// La sesión se guarda en el servidor (HttpSession), no en el navegador.
@Controller
public class AuthWebController {

    public static final String SESION_USUARIO_ID = "usuarioId";
    public static final String SESION_NOMBRE = "nombre";

    private final UsuarioRepository usuarios;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AuthWebController(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    @GetMapping("/login")
    public String formularioLogin(HttpSession sesion) {
        if (sesion.getAttribute(SESION_USUARIO_ID) != null) return "redirect:/inicio";
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String correo, @RequestParam String clave,
                        HttpServletRequest req, Model model) {
        String correoLimpio = correo.trim().toLowerCase();

        // Mismo mensaje si el correo no existe o la clave es incorrecta
        Optional<Usuario> usuario = usuarios.findByEmail(correoLimpio)
                .filter(u -> encoder.matches(clave, u.getContraseña()));

        if (usuario.isEmpty()) {
            model.addAttribute("error", "Correo o clave incorrectos");
            model.addAttribute("correo", correo);
            return "login";
        }

        iniciarSesion(req, usuario.get());
        return "redirect:/inicio";
    }

    @GetMapping("/registro")
    public String formularioRegistro() {
        return "registro";
    }

    @PostMapping("/registro")
    public String registro(@RequestParam String nombre, @RequestParam String dni,
                           @RequestParam String correo, @RequestParam String clave,
                           RedirectAttributes aviso, Model model) {
        Usuario nuevo = new Usuario(nombre.trim(), correo.trim().toLowerCase(), dni.trim(), clave);

        // Cada validación la hace la propia entidad (métodos del dominio)
        String error = null;
        if (!nuevo.nombreValido()) error = "Nombre inválido";
        else if (!nuevo.dniValido()) error = "El DNI debe tener 7 u 8 dígitos";
        else if (!nuevo.emailValido()) error = "Correo inválido";
        else if (!nuevo.contraseñaValida()) error = "La clave debe tener al menos 6 caracteres";
        else if (usuarios.existsByEmail(nuevo.getEmail())) error = "Ese correo ya está registrado";

        if (error != null) {
            model.addAttribute("error", error);
            model.addAttribute("nombre", nombre);
            model.addAttribute("dni", dni);
            model.addAttribute("correo", correo);
            return "registro";
        }

        // La clave nunca se guarda en texto plano: se guarda su hash BCrypt
        nuevo.setContraseña(encoder.encode(clave));
        usuarios.save(nuevo);

        // HU1: al registrarse correctamente se vuelve a la pantalla de inicio de sesión
        aviso.addFlashAttribute("ok", "Cuenta creada. Ingresá con tu correo y tu clave.");
        return "redirect:/login";
    }

    @PostMapping("/logout")
    public String logout(HttpSession sesion) {
        sesion.invalidate();
        return "redirect:/login";
    }

    private void iniciarSesion(HttpServletRequest req, Usuario usuario) {
        HttpSession sesion = req.getSession(true);
        req.changeSessionId(); // nuevo identificador al ingresar (evita fijación de sesión)
        sesion.setAttribute(SESION_USUARIO_ID, usuario.getIdUsuario());
        sesion.setAttribute(SESION_NOMBRE, usuario.getNombre());
    }
}