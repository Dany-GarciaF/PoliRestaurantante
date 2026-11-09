package com.restaurante.restaurantbackend.controller

import com.restaurante.restaurantbackend.dto.ActualizarUsuarioRequest
import com.restaurante.restaurantbackend.dto.CambiarEstadoUsuarioRequest
import com.restaurante.restaurantbackend.dto.PaginaResponse
import com.restaurante.restaurantbackend.dto.RestablecerContrasenaRequest
import com.restaurante.restaurantbackend.dto.UsuarioResponse
import com.restaurante.restaurantbackend.entity.RolUsuario
import com.restaurante.restaurantbackend.service.UsuarioService
import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.PutMapping
import java.util.UUID

@RestController
@RequestMapping("/api/admin/usuarios")
@PreAuthorize("hasRole('ADMINISTRADOR')")
class UsuarioAdminController(
    private val usuarioService: UsuarioService
) {
    @GetMapping
    fun listar(
        @RequestParam(required = false) rol: RolUsuario?,
        @RequestParam(required = false) activo: Boolean?,
        @RequestParam(required = false) busqueda: String?,
        @RequestParam(defaultValue = "0") @Min(0) pagina: Int,
        @RequestParam(defaultValue = "20") @Min(1) @Max(MAXIMUM_PAGE_SIZE.toLong()) tamano: Int
    ): PaginaResponse<UsuarioResponse> =
        usuarioService.listar(
            rol,
            activo,
            busqueda,
            PageRequest.of(pagina, tamano, Sort.by(Sort.Direction.ASC, "nombre"))
        )

    @PutMapping("/{id}")
    fun actualizar(
        @PathVariable id: UUID,
        @Valid @RequestBody solicitud: ActualizarUsuarioRequest
    ): UsuarioResponse = usuarioService.actualizar(id, solicitud)

    @PatchMapping("/{id}/estado")
    fun cambiarEstado(
        @PathVariable id: UUID,
        @Valid @RequestBody solicitud: CambiarEstadoUsuarioRequest
    ): UsuarioResponse = usuarioService.cambiarEstado(id, solicitud)

    @PatchMapping("/{id}/contrasena")
    fun restablecerContrasena(
        @PathVariable id: UUID,
        @Valid @RequestBody solicitud: RestablecerContrasenaRequest
    ) {
        usuarioService.restablecerContrasena(id, solicitud)
    }

    companion object {
        private const val MAXIMUM_PAGE_SIZE = 100
    }
}
