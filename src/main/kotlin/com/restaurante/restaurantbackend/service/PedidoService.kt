package com.restaurante.restaurantbackend.service

import com.restaurante.restaurantbackend.dto.CrearPedidoRequest
import com.restaurante.restaurantbackend.dto.HistorialEstadoResponse
import com.restaurante.restaurantbackend.dto.PedidoResponse
import com.restaurante.restaurantbackend.entity.EstadoPedido
import java.util.UUID

interface PedidoService {
    fun crear(solicitud: CrearPedidoRequest, correoMesero: String): PedidoResponse
    fun cambiarEstado(pedidoId: UUID, nuevoEstado: EstadoPedido, correoUsuario: String): PedidoResponse
    fun listarColaCocina(): List<PedidoResponse>
    fun listarPedidosMesero(correoMesero: String): List<PedidoResponse>
    fun historial(pedidoId: UUID, correoUsuario: String): List<HistorialEstadoResponse>
}
