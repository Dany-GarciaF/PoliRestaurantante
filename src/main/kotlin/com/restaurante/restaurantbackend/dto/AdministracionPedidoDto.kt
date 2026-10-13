package com.restaurante.restaurantbackend.dto

import com.restaurante.restaurantbackend.entity.EstadoPedido
import com.restaurante.restaurantbackend.entity.TipoPedido
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

data class FiltroPedidoAdministrativo(
    val estado: EstadoPedido? = null,
    val tipo: TipoPedido? = null,
    val meseroId: UUID? = null,
    val mesaId: UUID? = null,
    val fechaDesde: LocalDate? = null,
    val fechaHasta: LocalDate? = null
)

data class PedidoAdministrativoResumenResponse(
    val id: UUID,
    val tipo: TipoPedido,
    val estado: EstadoPedido,
    val total: BigDecimal,
    val mesaId: UUID?,
    val numeroMesa: Int?,
    val meseroId: UUID?,
    val nombreMesero: String?,
    val fechaCreacion: OffsetDateTime
)
