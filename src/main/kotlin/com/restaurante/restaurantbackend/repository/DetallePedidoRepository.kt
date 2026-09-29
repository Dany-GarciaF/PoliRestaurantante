package com.restaurante.restaurantbackend.repository

import com.restaurante.restaurantbackend.entity.DetallePedido
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface DetallePedidoRepository : JpaRepository<DetallePedido, UUID> {
    fun findByPedidoId(pedidoId: UUID): List<DetallePedido>
}