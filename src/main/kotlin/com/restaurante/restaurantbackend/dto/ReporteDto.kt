package com.restaurante.restaurantbackend.dto

import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

data class ResumenVentasResponse(
    val fechaDesde: LocalDate,
    val fechaHasta: LocalDate,
    val pedidosEntregados: Long,
    val ventasTotales: BigDecimal,
    val ticketPromedio: BigDecimal
)

data class ProductoMasVendidoResponse(
    val productoId: UUID,
    val nombreProducto: String,
    val unidadesVendidas: Long,
    val ingresosGenerados: BigDecimal
)

data class ResumenCancelacionesResponse(
    val fechaDesde: LocalDate,
    val fechaHasta: LocalDate,
    val pedidosCancelados: Long,
    val valorTotalCancelado: BigDecimal
)

data class TiempoPreparacionResponse(
    val pedidosAnalizados: Long,
    val tiempoPromedioSegundos: Long?,
)
