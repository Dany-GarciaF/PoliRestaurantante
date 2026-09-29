package com.restaurante.restaurantbackend.dto

import com.restaurante.restaurantbackend.entity.EstadoPago
import com.restaurante.restaurantbackend.entity.MetodoPago
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.UUID

data class CrearPagoRequest(
    @field:NotNull(message = "El pedido es obligatorio.")
    val pedidoId: UUID,

    @field:NotNull(message = "El método de pago es obligatorio.")
    val metodoPago: MetodoPago,

    @field:DecimalMin(
        value = "0.01",
        message = "El total debe ser mayor que cero."
    )
    val total: BigDecimal
)

data class PagoResponse(
    val id: UUID,
    val pedidoId: UUID,
    val metodoPago: MetodoPago,
    val total: BigDecimal,
    val estadoPago: EstadoPago,
    val fecha: OffsetDateTime
)

data class CambiarEstadoPagoRequest(
    @field:NotNull(message = "El estado del pago es obligatorio.")
    val estado: EstadoPago
)