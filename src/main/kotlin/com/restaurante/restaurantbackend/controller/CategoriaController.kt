package com.restaurante.restaurantbackend.controller

import com.restaurante.restaurantbackend.dto.CategoriaRequest
import com.restaurante.restaurantbackend.dto.CategoriaResponse
import com.restaurante.restaurantbackend.service.CategoriaService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/admin/categorias")
@PreAuthorize("hasRole('ADMINISTRADOR')")
class CategoriaController(
    private val categoriaService: CategoriaService
) {
    @GetMapping
    fun listar(): List<CategoriaResponse> = categoriaService.listar()

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun crear(@Valid @RequestBody solicitud: CategoriaRequest): CategoriaResponse =
        categoriaService.crear(solicitud)

    @PutMapping("/{id}")
    fun actualizar(
        @PathVariable id: UUID,
        @Valid @RequestBody solicitud: CategoriaRequest
    ): CategoriaResponse = categoriaService.actualizar(id, solicitud)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun desactivar(@PathVariable id: UUID) {
        categoriaService.desactivar(id)
    }
}
