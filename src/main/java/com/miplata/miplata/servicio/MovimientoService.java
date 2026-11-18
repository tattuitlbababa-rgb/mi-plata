package com.miplata.miplata.servicio;

import com.miplata.miplata.dominio.Movimiento;
import com.miplata.miplata.dominio.Usuario;
import com.miplata.miplata.dto.MovimientoForm;
import com.miplata.miplata.excepcion.DatosInvalidosException;
import com.miplata.miplata.excepcion.MovimientoNoEncontradoException;
import com.miplata.miplata.repositorio.MovimientoRepository;
import com.miplata.miplata.repositorio.UsuarioRepository;
import com.miplata.miplata.util.Constantes;
import com.miplata.miplata.util.Texto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

// Lógica de negocio de los movimientos. Los controladores llaman a esta clase
// y esta clase llama al repositorio (controlador -> servicio -> repositorio).
@Service
public class MovimientoService {

    private static final DateTimeFormatter FORMATO_VISUAL = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final MovimientoRepository movimientos;
    private final UsuarioRepository usuarios;
    private final CategoriaService categorias;

    public MovimientoService(MovimientoRepository movimientos, UsuarioRepository usuarios,
                             CategoriaService categorias) {
        this.movimientos = movimientos;
        this.usuarios = usuarios;
        this.categorias = categorias;
    }

    // Movimientos del usuario que contienen el texto buscado (vacío = todos)
    public List<Movimiento> buscar(int usuarioId, String texto) {
        String termino = Texto.normalizar(texto);
        return movimientos.findByUsuarioIdUsuarioOrderByFechaDescIdMovimientoDesc(usuarioId).stream()
                .filter(m -> termino.isEmpty() || coincide(m, termino))
                .toList();
    }

    // Los últimos N movimientos: ordena por fecha descendente y se queda con N
    public List<Movimiento> ultimos(int usuarioId, int cantidad) {
        return movimientos.findByUsuarioIdUsuarioOrderByFechaDescIdMovimientoDesc(usuarioId).stream()
                .sorted(Comparator.comparing(Movimiento::getFecha).reversed())
                .limit(cantidad)
                .toList();
    }

    // Saldo = ingresos - gastos, usando el método estático del dominio
    public double saldo(int usuarioId) {
        return Movimiento.calcularSaldo(
                movimientos.findByUsuarioIdUsuarioOrderByFechaDescIdMovimientoDesc(usuarioId));
    }

    // Alta de un ingreso o un gasto (HU3 y HU4)
    @Transactional
    public Movimiento registrar(int usuarioId, String tipo, MovimientoForm form) {
        if (!Constantes.TIPOS.contains(tipo)) throw new DatosInvalidosException("Tipo inválido");

        String descripcion = form.getDescripcion() == null ? "" : form.getDescripcion().trim();
        if (descripcion.isEmpty() || descripcion.length() > 150) {
            throw new DatosInvalidosException("La descripción es obligatoria (máximo 150 caracteres)");
        }

        Double importe = form.getImporte();
        if (importe == null || importe <= 0) {
            throw new DatosInvalidosException("El importe tiene que ser mayor a cero");
        }

        LocalDate fecha = parsearFecha(form.getFecha());
        String categoria = categorias.nombreExistente(usuarioId, tipo, form.getCategoria());

        Usuario usuario = usuarios.findById(usuarioId)
                .orElseThrow(() -> new DatosInvalidosException("Sesión inválida"));

        Movimiento nuevo = new Movimiento(descripcion, importe, fecha, tipo, categoria, usuario);
        try {
            nuevo.registrarMovimiento();   // validación final del dominio
        } catch (IllegalArgumentException e) {
            throw new DatosInvalidosException(e.getMessage());
        }
        return movimientos.save(nuevo);
    }

    public void eliminar(int usuarioId, int id) {
        Movimiento movimiento = movimientos.findById(id)
                .filter(m -> m.getIdUsuario() == usuarioId)
                .orElseThrow(() -> new MovimientoNoEncontradoException(id));
        movimientos.delete(movimiento);
    }

    private LocalDate parsearFecha(String texto) {
        try {
            return LocalDate.parse(texto == null ? "" : texto.trim());
        } catch (DateTimeParseException e) {
            throw new DatosInvalidosException("La fecha es obligatoria y tiene que ser válida");
        }
    }

    private boolean coincide(Movimiento m, String termino) {
        return Stream.of(
                        m.getFecha().format(FORMATO_VISUAL),
                        m.getDescripcion(),
                        m.getCategoriaNombre(),
                        m.getTipo(),
                        String.valueOf(m.getMonto()))
                .map(Texto::normalizar)
                .anyMatch(texto -> texto.contains(termino));
    }
}