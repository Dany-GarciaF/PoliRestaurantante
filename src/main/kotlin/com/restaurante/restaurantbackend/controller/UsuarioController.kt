package com.restaurante.restaurantbackend.controller

import com.restaurante.restaurantbackend.dto.CrearUsuarioRequest
import com.restaurante.restaurantbackend.dto.UsuarioResponse
import com.restaurante.restaurantbackend.service.UsuarioService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/usuarios")
class UsuarioController(
    private val usuarioService: UsuarioService
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    fun crear(@Valid @RequestBody solicitud: CrearUsuarioRequest): UsuarioResponse =
        usuarioService.crear(solicitud)
}
