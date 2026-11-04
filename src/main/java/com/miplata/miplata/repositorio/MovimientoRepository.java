package com.miplata.miplata.repositorio;

import com.miplata.miplata.dominio.Movimiento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovimientoRepository extends JpaRepository<Movimiento, Integer> {

    // Historial del usuario, del más reciente al más antiguo
    List<Movimiento> findByUsuarioIdUsuarioOrderByFechaDescIdMovimientoDesc(int idUsuario);

    // Indica si hay movimientos de ese usuario, con ese tipo y ese nombre de categoría
    // (así se verifica si una categoría está en uso antes de borrarla)
    boolean existsByUsuarioIdUsuarioAndTipoAndCategoriaNombre(int idUsuario, String tipo, String categoriaNombre);
}