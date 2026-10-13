package com.restaurante.restaurantbackend.dto

import com.restaurante.restaurantbackend.entity.EstadoMesa
import com.restaurante.restaurantbackend.validation.NoControlCharacters
import com.restaurante.restaurantbackend.validation.RestaurantName
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import java.math.BigDecimal
import java.util.UUID

data class CategoriaRequest(
    @field:NotBlank(message = "El nombre es obligatorio.")
    @field:Size(max = 100, message = "El nombre no puede superar 100 caracteres.")
    @field:RestaurantName
    val nombre: String,
    @field:Size(max = 300, message = "La descripción no puede superar 300 caracteres.")
    @field:NoControlCharacters
    val descripcion: String? = null,
    val activa: Boolean = true
)

data class CategoriaResponse(
    val id: UUID,
    val nombre: String,
    val descripcion: String?,
    val activa: Boolean
)

data class ProductoRequest(
    @field:NotBlank(message = "El nombre es obligatorio.")
    @field:Size(max = 150, message = "El nombre no puede superar 150 caracteres.")
    @field:RestaurantName
    val nombre: String,
    @field:Size(max = 500, message = "La descripción no puede superar 500 caracteres.")
    @field:NoControlCharacters
    val descripcion: String? = null,
    @field:DecimalMin(value = "0.00", message = "El precio no puede ser negativo.")
    val precio: BigDecimal,
    @field:NotNull(message = "La categoría es obligatoria.")
    val categoriaId: UUID,
    val disponible: Boolean = true
)

data class ProductoResponse(
    val id: UUID,
    val nombre: String,
    val descripcion: String?,
    val precio: BigDecimal,
    val disponible: Boolean,
    val categoria: CategoriaResponse
)

data class MesaRequest(
    @field:Positive(message = "El número de mesa debe ser mayor que cero.")
    val numero: Int,
    val activa: Boolean = true
)

data class MesaResponse(
    val id: UUID,
    val numero: Int,
    val estado: EstadoMesa,
    val activa: Boolean
)
