package com.restaurante.restaurantbackend.repository

import com.restaurante.restaurantbackend.entity.EstadoPedido
import com.restaurante.restaurantbackend.entity.Pedido
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface PedidoRepository : JpaRepository<Pedido, UUID> {
    fun findByEstadoOrderByFechaCreacionAsc(estado: EstadoPedido): List<Pedido>
    fun findByEstadoInOrderByFechaCreacionAsc(estados: Collection<EstadoPedido>): List<Pedido>
    fun findByMesaIdOrderByFechaCreacionDesc(mesaId: UUID): List<Pedido>
    fun findByMeseroIdOrderByFechaCreacionDesc(meseroId: UUID): List<Pedido>
}
