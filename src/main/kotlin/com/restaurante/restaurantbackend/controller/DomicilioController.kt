package com.restaurante.restaurantbackend.controller

import com.restaurante.restaurantbackend.dto.CambiarEstadoDomicilioRequest
import com.restaurante.restaurantbackend.dto.CrearDomicilioRequest
import com.restaurante.restaurantbackend.dto.DomicilioResponse
import com.restaurante.restaurantbackend.service.DomicilioService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/domicilios")
class DomicilioController(
    private val domicilioService: DomicilioService
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('MESERO', 'ADMINISTRADOR')")
    fun crear(
        @Valid @RequestBody solicitud: CrearDomicilioRequest
    ): DomicilioResponse = domicilioService.crear(solicitud)

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    fun listar(): List<DomicilioResponse> =
        domicilioService.listar()

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MESERO', 'ADMINISTRADOR')")
    fun obtener(@PathVariable id: UUID): DomicilioResponse =
        domicilioService.obtener(id)

    @GetMapping("/pedido/{pedidoId}")
    @PreAuthorize("hasAnyRole('MESERO', 'ADMINISTRADOR')")
    fun obtenerPorPedido(
        @PathVariable pedidoId: UUID
    ): DomicilioResponse =
        domicilioService.obtenerPorPedido(pedidoId)

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('MESERO', 'ADMINISTRADOR')")
    fun cambiarEstado(
        @PathVariable id: UUID,
        @Valid @RequestBody solicitud: CambiarEstadoDomicilioRequest
    ): DomicilioResponse =
        domicilioService.cambiarEstado(id, solicitud)
}