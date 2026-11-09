package com.restaurante.restaurantbackend.service

import com.restaurante.restaurantbackend.dto.ActualizarDetallePedidoRequest
import com.restaurante.restaurantbackend.dto.AgregarDetallePedidoRequest
import com.restaurante.restaurantbackend.dto.CrearPedidoRequest
import com.restaurante.restaurantbackend.dto.FiltroPedidoAdministrativo
import com.restaurante.restaurantbackend.dto.HistorialEstadoResponse
import com.restaurante.restaurantbackend.dto.PaginaResponse
import com.restaurante.restaurantbackend.dto.PedidoAdministrativoResumenResponse
import com.restaurante.restaurantbackend.dto.PedidoResponse
import com.restaurante.restaurantbackend.entity.EstadoPedido
import org.springframework.data.domain.Pageable
import java.util.UUID

interface PedidoService {
    fun crear(solicitud: CrearPedidoRequest, correoMesero: String): PedidoResponse
    fun listarAdministracion(
        filtro: FiltroPedidoAdministrativo,
        pageable: Pageable
    ): PaginaResponse<PedidoAdministrativoResumenResponse>
    fun obtenerPedidoAdministracion(pedidoId: UUID): PedidoResponse
    fun obtenerPedidoMesero(pedidoId: UUID, correoMesero: String): PedidoResponse
    fun agregarDetalle(pedidoId: UUID, solicitud: AgregarDetallePedidoRequest, correoMesero: String): PedidoResponse
    fun actualizarDetalle(
        pedidoId: UUID,
        detalleId: UUID,
        solicitud: ActualizarDetallePedidoRequest,
        correoMesero: String
    ): PedidoResponse
    fun eliminarDetalle(pedidoId: UUID, detalleId: UUID, correoMesero: String): PedidoResponse
    fun cambiarEstado(pedidoId: UUID, nuevoEstado: EstadoPedido, correoUsuario: String): PedidoResponse
    fun listarColaCocina(): List<PedidoResponse>
    fun listarPedidosMesero(correoMesero: String): List<PedidoResponse>
    fun historial(pedidoId: UUID, correoUsuario: String): List<HistorialEstadoResponse>
}
