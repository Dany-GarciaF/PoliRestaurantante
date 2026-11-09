package com.restaurante.restaurantbackend.dto

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Digits
import java.math.BigDecimal
import java.util.UUID

data class ConfiguracionDomiciliosResponse(
    val id: UUID,
    val montoMaximoContraentrega: BigDecimal,
    val activa: Boolean
)

data class ActualizarConfiguracionDomiciliosRequest(
    @field:DecimalMin(value = "0.00", message = "El monto no puede ser negativo.")
    @field:Digits(integer = 10, fraction = 2, message = "El monto admite máximo 10 enteros y 2 decimales.")
    val montoMaximoContraentrega: BigDecimal
)
