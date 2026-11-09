package com.restaurante.restaurantbackend.repository

import com.restaurante.restaurantbackend.entity.Categoria
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

interface CategoriaRepository : JpaRepository<Categoria, UUID> {
    fun existsByNombreIgnoreCase(nombre: String): Boolean
    fun findByActivaTrueOrderByNombreAsc(pageable: Pageable): Page<Categoria>
}
