package com.miplata.miplata.repositorio;

import com.miplata.miplata.dominio.Movimiento;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// DAO con una consulta armada con Criteria API.
// Cada filtro se agrega solo si el usuario lo completó (consulta dinámica).
@Repository
public class MovimientoBusquedaDao {

    @PersistenceContext
    private EntityManager em;

    public List<Movimiento> buscar(int usuarioId, String texto, String tipo,
                                   LocalDate desde, LocalDate hasta) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Movimiento> consulta = cb.createQuery(Movimiento.class);
        Root<Movimiento> m = consulta.from(Movimiento.class);

        List<Predicate> filtros = new ArrayList<>();

        // Siempre: solo los movimientos del usuario que inició sesión
        filtros.add(cb.equal(m.get("usuario").get("idUsuario"), usuarioId));

        // Texto: se busca en la descripción o en la categoría
        if (texto != null && !texto.isBlank()) {
            String patron = "%" + texto.trim().toLowerCase() + "%";
            filtros.add(cb.or(
                    cb.like(cb.lower(m.get("descripcion")), patron),
                    cb.like(cb.lower(m.get("categoriaNombre")), patron)));
        }

        // Tipo: Ingreso o Gasto
        if (tipo != null && !tipo.isBlank()) {
            filtros.add(cb.equal(m.get("tipo"), tipo));
        }

        // Rango de fechas
        if (desde != null) {
            filtros.add(cb.greaterThanOrEqualTo(m.<LocalDate>get("fecha"), desde));
        }
        if (hasta != null) {
            filtros.add(cb.lessThanOrEqualTo(m.<LocalDate>get("fecha"), hasta));
        }

        consulta.select(m)
                .where(filtros.toArray(new Predicate[0]))
                .orderBy(cb.desc(m.get("fecha")), cb.desc(m.get("idMovimiento")));

        return em.createQuery(consulta).getResultList();
    }
}