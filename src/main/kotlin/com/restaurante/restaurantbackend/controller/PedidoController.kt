package com.restaurante.restaurantbackend.controller

import com.restaurante.restaurantbackend.dto.CambiarEstadoPedidoRequest
import com.restaurante.restaurantbackend.dto.CrearPedidoRequest
import com.restaurante.restaurantbackend.dto.HistorialEstadoResponse
import com.restaurante.restaurantbackend.dto.PedidoResponse
import com.restaurante.restaurantbackend.service.PedidoService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/pedidos")
class PedidoController(
    private val pedidoService: PedidoService
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('MESERO')")
    fun crear(
        @Valid @RequestBody solicitud: CrearPedidoRequest,
        authentication: Authentication
    ): PedidoResponse = pedidoService.crear(solicitud, authentication.name)

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('MESERO', 'COCINA')")
    fun cambiarEstado(
        @PathVariable id: UUID,
        @Valid @RequestBody solicitud: CambiarEstadoPedidoRequest,
        authentication: Authentication
    ): PedidoResponse = pedidoService.cambiarEstado(id, solicitud.estado, authentication.name)

    @GetMapping("/cocina")
    @PreAuthorize("hasRole('COCINA')")
    fun colaCocina(): List<PedidoResponse> = pedidoService.listarColaCocina()

    @GetMapping("/mios")
    @PreAuthorize("hasRole('MESERO')")
    fun misPedidos(authentication: Authentication): List<PedidoResponse> =
        pedidoService.listarPedidosMesero(authentication.name)

    @GetMapping("/{id}/historial")
    @PreAuthorize("hasAnyRole('MESERO', 'COCINA')")
    fun historial(@PathVariable id: UUID, authentication: Authentication): List<HistorialEstadoResponse> =
        pedidoService.historial(id, authentication.name)
}
