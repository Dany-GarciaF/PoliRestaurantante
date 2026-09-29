package com.restaurante.restaurantbackend.controller

import com.restaurante.restaurantbackend.dto.CambiarEstadoUsuarioRequest
import com.restaurante.restaurantbackend.dto.UsuarioResponse
import com.restaurante.restaurantbackend.service.UsuarioService
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/admin/usuarios")
@PreAuthorize("hasRole('ADMINISTRADOR')")
class UsuarioAdminController(
    private val usuarioService: UsuarioService
) {
    @PatchMapping("/{id}/estado")
    fun cambiarEstado(
        @PathVariable id: UUID,
        @Valid @RequestBody solicitud: CambiarEstadoUsuarioRequest
    ): UsuarioResponse = usuarioService.cambiarEstado(id, solicitud)
}
