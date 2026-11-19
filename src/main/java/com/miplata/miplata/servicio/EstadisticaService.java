package com.miplata.miplata.servicio;

import com.miplata.miplata.dominio.Movimiento;
import com.miplata.miplata.dto.EstadisticaResumen;
import com.miplata.miplata.repositorio.MovimientoRepository;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// Estadísticas del usuario. Todo se calcula con Streams sobre la lista de movimientos.
@Service
public class EstadisticaService {

    private final MovimientoRepository movimientos;

    public EstadisticaService(MovimientoRepository movimientos) {
        this.movimientos = movimientos;
    }

    // Meses que tienen movimientos ("2026-11", "2026-10"...), del más reciente al más antiguo
    public List<String> meses(int usuarioId) {
        return historial(usuarioId).stream()
                .map(m -> YearMonth.from(m.getFecha()).toString())
                .distinct()
                .sorted(Comparator.reverseOrder())
                .toList();
    }

    // mes con formato "2026-11"; vacío (o inválido) = todos los meses
    public EstadisticaResumen resumen(int usuarioId, String mes) {
        String mesValido = (mes != null && mes.matches("\\d{4}-\\d{2}")) ? mes : "";

        List<Movimiento> todos = historial(usuarioId);
        double saldo = Movimiento.calcularSaldo(todos);   // el saldo no depende del filtro

        // filter: nos quedamos con los movimientos del período elegido
        List<Movimiento> periodo = todos.stream()
                .filter(m -> mesValido.isEmpty() || YearMonth.from(m.getFecha()).toString().equals(mesValido))
                .toList();

        double ingresos = periodo.stream()
                .filter(m -> "Ingreso".equals(m.getTipo()))
                .mapToDouble(Movimiento::getMonto)
                .sum();

        List<Movimiento> soloGastos = periodo.stream()
                .filter(m -> "Gasto".equals(m.getTipo()))
                .toList();

        double gastos = soloGastos.stream().mapToDouble(Movimiento::getMonto).sum();

        // count: cuántos movimientos hay en el período
        long cantidad = periodo.stream().count();

        // groupingBy: suma de gastos por categoría
        Map<String, Double> totalPorCategoria = soloGastos.stream()
                .collect(Collectors.groupingBy(Movimiento::getCategoriaNombre,
                        Collectors.summingDouble(Movimiento::getMonto)));

        // map + sorted: convierte cada entrada en una fila con su porcentaje y ordena de mayor a menor
        List<EstadisticaResumen.Fila> filas = totalPorCategoria.entrySet().stream()
                .map(e -> new EstadisticaResumen.Fila(
                        e.getKey(), e.getValue(), gastos > 0 ? e.getValue() * 100 / gastos : 0))
                .sorted(Comparator.comparingDouble(EstadisticaResumen.Fila::getTotal).reversed())
                .toList();

        // sorted + limit: los 3 gastos más altos
        List<Movimiento> mayores = soloGastos.stream()
                .sorted(Comparator.comparingDouble(Movimiento::getMonto).reversed())
                .limit(3)
                .toList();

        return new EstadisticaResumen(mesValido, ingresos, gastos, saldo, cantidad, filas, mayores);
    }

    private List<Movimiento> historial(int usuarioId) {
        return movimientos.findByUsuarioIdUsuarioOrderByFechaDescIdMovimientoDesc(usuarioId);
    }
}