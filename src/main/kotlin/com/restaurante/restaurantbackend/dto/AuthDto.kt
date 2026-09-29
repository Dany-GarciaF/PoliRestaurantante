package com.restaurante.restaurantbackend.dto

import com.restaurante.restaurantbackend.entity.RolUsuario
import com.restaurante.restaurantbackend.validation.NoControlCharacters
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class LoginRequest(
    @field:Email(message = "El correo no tiene un formato válido.")
    @field:NotBlank(message = "El correo es obligatorio.")
    @field:Size(max = 160, message = "El correo no puede superar 160 caracteres.")
    @field:NoControlCharacters
    val correo: String,
    @field:NotBlank(message = "La contraseña es obligatoria.")
    @field:Size(max = 72, message = "La contraseña no puede superar 72 caracteres.")
    val contrasena: String
)

data class LoginResponse(
    val accessToken: String,
    val tokenType: String = "Bearer",
    val rol: RolUsuario
)
