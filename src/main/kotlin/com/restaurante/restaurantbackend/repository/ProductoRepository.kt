package com.restaurante.restaurantbackend.repository

import com.restaurante.restaurantbackend.entity.Producto
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import java.util.UUID

interface ProductoRepository : JpaRepository<Producto, UUID>, JpaSpecificationExecutor<Producto> {
    fun existsByNombreIgnoreCase(nombre: String): Boolean
    fun findByDisponibleTrue(): List<Producto>
}
