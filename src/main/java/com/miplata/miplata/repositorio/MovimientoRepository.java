package com.miplata.miplata.repositorio;

import com.miplata.miplata.dominio.Movimiento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovimientoRepository extends JpaRepository<Movimiento, Integer> {

    // Historial del usuario, del más reciente al más antiguo
    List<Movimiento> findByUsuarioIdUsuarioOrderByFechaDescIdMovimientoDesc(int idUsuario);

    // Movimientos que pertenecen a una categoría (sirve para no borrar categorías en uso)
    boolean existsByCategoriaIdCategoria(int idCategoria);
}