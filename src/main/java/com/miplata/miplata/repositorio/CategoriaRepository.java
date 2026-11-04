package com.miplata.miplata.repositorio;

import com.miplata.miplata.dominio.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {

    // Categorías de un usuario, filtradas por tipo (Ingreso o Gasto)
    List<Categoria> findByUsuarioIdUsuarioAndTipo(int idUsuario, String tipo);

    List<Categoria> findByUsuarioIdUsuario(int idUsuario);
}