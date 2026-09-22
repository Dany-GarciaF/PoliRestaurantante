package com.restaurante.restaurantbackend.repository

import com.restaurante.restaurantbackend.entity.HistorialEstado
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface HistorialEstadoRepository : JpaRepository<HistorialEstado, UUID> {
    fun findByPedidoIdOrderByFechaAsc(pedidoId: UUID): List<HistorialEstado>
}
