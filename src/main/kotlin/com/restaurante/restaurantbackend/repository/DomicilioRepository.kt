package com.restaurante.restaurantbackend.repository

import com.restaurante.restaurantbackend.entity.Domicilio
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface DomicilioRepository : JpaRepository<Domicilio, UUID> {
    fun existsByPedidoId(pedidoId: UUID): Boolean

    fun findByPedidoId(pedidoId: UUID): Domicilio?
}