package com.restaurante.restaurantbackend.controller

import com.restaurante.restaurantbackend.dto.ProductoRequest
import com.restaurante.restaurantbackend.dto.ProductoResponse
import com.restaurante.restaurantbackend.service.ProductoService
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
@RequestMapping("/api/admin/productos")
@PreAuthorize("hasRole('ADMINISTRADOR')")
class ProductoController(
    private val productoService: ProductoService
) {
    @GetMapping
    fun listar(): List<ProductoResponse> = productoService.listar()

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun crear(@Valid @RequestBody solicitud: ProductoRequest): ProductoResponse =
        productoService.crear(solicitud)

    @PutMapping("/{id}")
    fun actualizar(
        @PathVariable id: UUID,
        @Valid @RequestBody solicitud: ProductoRequest
    ): ProductoResponse = productoService.actualizar(id, solicitud)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun desactivar(@PathVariable id: UUID) {
        productoService.desactivar(id)
    }
}
