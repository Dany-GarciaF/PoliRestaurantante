package com.restaurante.restaurantbackend.repository

import com.restaurante.restaurantbackend.entity.Pago
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface PagoRepository : JpaRepository<Pago, UUID> {
    fun findByPedidoIdOrderByFechaDesc(pedidoId: UUID): List<Pago>
}