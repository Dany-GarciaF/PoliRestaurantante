package com.restaurante.restaurantbackend.dto

import com.restaurante.restaurantbackend.entity.EstadoDomicilio
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.OffsetDateTime
import java.util.UUID

data class CrearDomicilioRequest(
    val pedidoId: UUID,

    @field:NotBlank(message = "El nombre del cliente es obligatorio.")
    @field:Size(max = 120)
    val nombreCliente: String,

    @field:NotBlank(message = "El teléfono es obligatorio.")
    @field:Size(max = 30)
    val telefono: String,

    @field:NotBlank(message = "La dirección es obligatoria.")
    @field:Size(max = 250)
    val direccion: String
)

data class CambiarEstadoDomicilioRequest(
    val estado: EstadoDomicilio
)

data class DomicilioResponse(
    val id: UUID,
    val pedidoId: UUID,
    val nombreCliente: String,
    val telefono: String,
    val direccion: String,
    val estado: EstadoDomicilio,
    val fechaCreacion: OffsetDateTime
)