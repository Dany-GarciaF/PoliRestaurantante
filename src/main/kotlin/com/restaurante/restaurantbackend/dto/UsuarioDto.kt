package com.restaurante.restaurantbackend.dto

import com.restaurante.restaurantbackend.entity.RolUsuario
import com.restaurante.restaurantbackend.validation.NoControlCharacters
import com.restaurante.restaurantbackend.validation.RestaurantName
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.util.UUID

data class CrearUsuarioRequest(
    @field:NotBlank(message = "El nombre es obligatorio.")
    @field:Size(max = 120, message = "El nombre no puede superar 120 caracteres.")
    @field:RestaurantName
    val nombre: String,
    @field:Email(message = "El correo no tiene un formato válido.")
    @field:NotBlank(message = "El correo es obligatorio.")
    @field:Size(max = 160, message = "El correo no puede superar 160 caracteres.")
    @field:NoControlCharacters
    val correo: String,
    @field:NotBlank(message = "La contraseña es obligatoria.")
    @field:Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres.")
    val contrasena: String,
    val rol: RolUsuario
)

data class UsuarioResponse(
    val id: UUID,
    val nombre: String,
    val correo: String,
    val rol: RolUsuario,
    val activo: Boolean
)

data class CambiarEstadoUsuarioRequest(
    val activo: Boolean
)
