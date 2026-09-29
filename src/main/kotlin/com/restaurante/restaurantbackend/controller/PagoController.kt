package com.restaurante.restaurantbackend.controller

import com.restaurante.restaurantbackend.dto.CambiarEstadoPagoRequest
import com.restaurante.restaurantbackend.dto.CrearPagoRequest
import com.restaurante.restaurantbackend.dto.PagoResponse
import com.restaurante.restaurantbackend.service.PagoService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/pagos")
class PagoController(
    private val pagoService: PagoService
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('MESERO', 'ADMINISTRADOR')")
    fun registrar(
        @Valid @RequestBody solicitud: CrearPagoRequest
    ): PagoResponse = pagoService.registrar(solicitud)

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    fun obtener(@PathVariable id: UUID): PagoResponse =
        pagoService.obtener(id)

    @GetMapping("/pedido/{pedidoId}")
    @PreAuthorize("hasAnyRole('MESERO', 'ADMINISTRADOR')")
    fun listarPorPedido(
        @PathVariable pedidoId: UUID
    ): List<PagoResponse> = pagoService.listarPorPedido(pedidoId)

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    fun cambiarEstado(
        @PathVariable id: UUID,
        @Valid @RequestBody solicitud: CambiarEstadoPagoRequest
    ): PagoResponse = pagoService.cambiarEstado(id, solicitud)
}