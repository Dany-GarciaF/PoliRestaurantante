package com.restaurante.restaurantbackend.dto

import com.restaurante.restaurantbackend.entity.EstadoPago
import com.restaurante.restaurantbackend.entity.EstadoPedido
import com.restaurante.restaurantbackend.entity.MetodoPago
import com.restaurante.restaurantbackend.entity.TipoPedido
import com.restaurante.restaurantbackend.validation.NoControlCharacters
import com.restaurante.restaurantbackend.validation.RestaurantName
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.UUID

data class DetallePedidoRequest(
    val productoId: UUID,
    @field:Positive(message = "La cantidad debe ser mayor que cero.")
    val cantidad: Int,
    @field:Size(max = 500, message = "Las notas no pueden superar 500 caracteres.")
    @field:NoControlCharacters
    val notas: String? = null
)

data class DatosEntregaRequest(
    @field:NotBlank(message = "El nombre del cliente es obligatorio.")
    @field:Size(max = 120, message = "El nombre no puede superar 120 caracteres.")
    @field:RestaurantName
    val nombreCliente: String,
    @field:NotBlank(message = "El teléfono es obligatorio.")
    @field:Size(max = 30, message = "El teléfono no puede superar 30 caracteres.")
    @field:jakarta.validation.constraints.Pattern(
        regexp = """^\+?[0-9 ()-]{7,30}$""",
        message = "El teléfono tiene un formato inválido."
    )
    val telefonoCliente: String,
    @field:NotBlank(message = "La dirección es obligatoria.")
    @field:Size(max = 250, message = "La dirección no puede superar 250 caracteres.")
    @field:NoControlCharacters
    val direccion: String,
    @field:Size(max = 100, message = "El barrio no puede superar 100 caracteres.")
    @field:NoControlCharacters
    val barrio: String? = null,
    @field:Size(max = 500, message = "Las referencias no pueden superar 500 caracteres.")
    @field:NoControlCharacters
    val referencias: String? = null
)

data class CrearPedidoRequest(
    val tipo: TipoPedido,
    val mesaId: UUID? = null,
    val metodoPago: MetodoPago? = null,
    @field:Valid
    val datosEntrega: DatosEntregaRequest? = null,
    @field:NotEmpty(message = "El pedido debe incluir al menos un producto.")
    @field:Valid
    val detalles: List<DetallePedidoRequest>
)

data class CambiarEstadoPedidoRequest(
    val estado: EstadoPedido
)

data class DetallePedidoResponse(
    val productoId: UUID,
    val nombreProducto: String,
    val precioUnitario: BigDecimal,
    val cantidad: Int,
    val notas: String?
)

data class HistorialEstadoResponse(
    val estado: EstadoPedido,
    val fecha: OffsetDateTime,
    val usuarioId: UUID?,
    val usuarioNombre: String?
)

data class PedidoResponse(
    val id: UUID,
    val mesaId: UUID?,
    val meseroId: UUID?,
    val tipo: TipoPedido,
    val estado: EstadoPedido,
    val metodoPago: MetodoPago?,
    val estadoPago: EstadoPago,
    val total: BigDecimal,
    val fechaCreacion: OffsetDateTime,
    val detalles: List<DetallePedidoResponse>
)
