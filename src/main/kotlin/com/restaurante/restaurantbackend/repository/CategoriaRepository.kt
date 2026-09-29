package com.restaurante.restaurantbackend.repository

import com.restaurante.restaurantbackend.entity.Categoria
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface CategoriaRepository : JpaRepository<Categoria, UUID> {
    fun existsByNombreIgnoreCase(nombre: String): Boolean
}
