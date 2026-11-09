package com.restaurante.restaurantbackend.controller

import com.restaurante.restaurantbackend.dto.FiltroPedidoAdministrativo
import com.restaurante.restaurantbackend.dto.PaginaResponse
import com.restaurante.restaurantbackend.dto.PedidoAdministrativoResumenResponse
import com.restaurante.restaurantbackend.dto.PedidoResponse
import com.restaurante.restaurantbackend.entity.EstadoPedido
import com.restaurante.restaurantbackend.entity.TipoPedido
import com.restaurante.restaurantbackend.service.PedidoService
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.util.UUID

@RestController
@RequestMapping("/api/admin/pedidos")
@PreAuthorize("hasRole('ADMINISTRADOR')")
class PedidoAdminController(
    private val pedidoService: PedidoService
) {
    @GetMapping
    fun listar(
        @RequestParam(required = false) estado: EstadoPedido?,
        @RequestParam(required = false) tipo: TipoPedido?,
        @RequestParam(required = false) meseroId: UUID?,
        @RequestParam(required = false) mesaId: UUID?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) fechaDesde: LocalDate?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) fechaHasta: LocalDate?,
        @RequestParam(defaultValue = "0") @Min(0) pagina: Int,
        @RequestParam(defaultValue = "20") @Min(1) @Max(MAXIMUM_PAGE_SIZE.toLong()) tamano: Int
    ): PaginaResponse<PedidoAdministrativoResumenResponse> =
        pedidoService.listarAdministracion(
            FiltroPedidoAdministrativo(estado, tipo, meseroId, mesaId, fechaDesde, fechaHasta),
            PageRequest.of(pagina, tamano, Sort.by(Sort.Direction.DESC, "fechaCreacion"))
        )

    @GetMapping("/{id}")
    fun obtener(@PathVariable id: UUID): PedidoResponse =
        pedidoService.obtenerPedidoAdministracion(id)

    companion object {
        private const val MAXIMUM_PAGE_SIZE = 100
    }
}
