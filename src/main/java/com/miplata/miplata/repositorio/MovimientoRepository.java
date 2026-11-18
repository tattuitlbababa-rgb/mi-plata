package com.miplata.miplata.repositorio;

import com.miplata.miplata.dominio.Movimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MovimientoRepository extends JpaRepository<Movimiento, Integer> {

    // Historial del usuario, del más reciente al más antiguo
    List<Movimiento> findByUsuarioIdUsuarioOrderByFechaDescIdMovimientoDesc(int idUsuario);

    // Indica si hay movimientos de ese usuario, con ese tipo y ese nombre de categoría
    // (así se verifica si una categoría está en uso antes de borrarla)
    boolean existsByUsuarioIdUsuarioAndTipoAndCategoriaNombre(int idUsuario, String tipo, String categoriaNombre);

    // Como el movimiento guarda el NOMBRE de la categoría, al renombrar una categoría
    // hay que actualizar también los movimientos que la usan.
    @Modifying
    @Query("update Movimiento m set m.categoriaNombre = :nuevo "
            + "where m.usuario.idUsuario = :usuarioId and m.tipo = :tipo and m.categoriaNombre = :viejo")
    int renombrarCategoria(@Param("usuarioId") int usuarioId, @Param("tipo") String tipo,
                           @Param("viejo") String viejo, @Param("nuevo") String nuevo);
}