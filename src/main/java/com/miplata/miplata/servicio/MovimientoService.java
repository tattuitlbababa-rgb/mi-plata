package com.miplata.miplata.servicio;

import com.miplata.miplata.dominio.Movimiento;
import com.miplata.miplata.dominio.Usuario;
import com.miplata.miplata.dto.MovimientoForm;
import com.miplata.miplata.excepcion.DatosInvalidosException;
import com.miplata.miplata.excepcion.MovimientoNoEncontradoException;
import com.miplata.miplata.repositorio.MovimientoBusquedaDao;
import com.miplata.miplata.repositorio.MovimientoRepository;
import com.miplata.miplata.repositorio.UsuarioRepository;
import com.miplata.miplata.util.Constantes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;

// Lógica de negocio de los movimientos. Los controladores llaman a esta clase
// y esta clase llama al repositorio (controlador -> servicio -> repositorio).
@Service
public class MovimientoService {

    private static final Logger log = LoggerFactory.getLogger(MovimientoService.class);

    private final MovimientoRepository movimientos;
    private final MovimientoBusquedaDao busqueda;
    private final UsuarioRepository usuarios;
    private final CategoriaService categorias;

    public MovimientoService(MovimientoRepository movimientos, MovimientoBusquedaDao busqueda,
                             UsuarioRepository usuarios, CategoriaService categorias) {
        this.movimientos = movimientos;
        this.busqueda = busqueda;
        this.usuarios = usuarios;
        this.categorias = categorias;
    }

    // Búsqueda con filtros opcionales (texto, tipo y rango de fechas) usando Criteria API
    public List<Movimiento> buscar(int usuarioId, String texto, String tipo,
                                   LocalDate desde, LocalDate hasta) {
        return busqueda.buscar(usuarioId, texto, tipo, desde, hasta);
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

    // Busca un movimiento del usuario; si no existe o es de otro usuario, lanza excepción
    public Movimiento obtener(int usuarioId, int id) {
        return movimientos.findById(id)
                .filter(m -> m.getIdUsuario() == usuarioId)
                .orElseThrow(() -> new MovimientoNoEncontradoException(id));
    }

    // Alta de un ingreso o un gasto (HU3 y HU4)
    @Transactional
    public Movimiento registrar(int usuarioId, String tipo, MovimientoForm form) {
        boolean guardado = false;
        try {
            if (!Constantes.TIPOS.contains(tipo)) throw new DatosInvalidosException("Tipo inválido");

            String descripcion = validarDescripcion(form.getDescripcion());
            double importe = validarImporte(form.getImporte());
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

            Movimiento resultado = movimientos.save(nuevo);
            guardado = true;
            return resultado;
        } finally {
            // El bloque finally se ejecuta SIEMPRE: tanto si el movimiento se guardó
            // como si alguna validación lanzó una excepción. Deja constancia del intento.
            log.info("Intento de registrar {} del usuario {}: {}",
                    tipo, usuarioId, guardado ? "GUARDADO" : "RECHAZADO");
        }
    }

    // Modifica fecha, descripción e importe. La categoría y el tipo no cambian.
    // Al ser @Transactional, si algo falla se deshace todo; si sale bien, JPA
    // guarda los cambios solo al terminar el método (dirty checking).
    @Transactional
    public void modificar(int usuarioId, int id, MovimientoForm form) {
        Movimiento movimiento = obtener(usuarioId, id);

        String descripcion = validarDescripcion(form.getDescripcion());
        double importe = validarImporte(form.getImporte());
        LocalDate fecha = parsearFecha(form.getFecha());

        try {
            movimiento.modificarMovimiento(descripcion, importe, fecha);  // método del dominio
        } catch (IllegalArgumentException e) {
            throw new DatosInvalidosException(e.getMessage());
        }
    }

    public void eliminar(int usuarioId, int id) {
        movimientos.delete(obtener(usuarioId, id));
    }

    private String validarDescripcion(String texto) {
        String descripcion = texto == null ? "" : texto.trim();
        if (descripcion.isEmpty() || descripcion.length() > 150) {
            throw new DatosInvalidosException("La descripción es obligatoria (máximo 150 caracteres)");
        }
        return descripcion;
    }

    private double validarImporte(Double importe) {
        if (importe == null || importe <= 0) {
            throw new DatosInvalidosException("El importe tiene que ser mayor a cero");
        }
        return importe;
    }

    private LocalDate parsearFecha(String texto) {
        try {
            return LocalDate.parse(texto == null ? "" : texto.trim());
        } catch (DateTimeParseException e) {
            throw new DatosInvalidosException("La fecha es obligatoria y tiene que ser válida");
        }
    }
}