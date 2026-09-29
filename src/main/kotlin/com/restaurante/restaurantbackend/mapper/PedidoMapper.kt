package com.restaurante.restaurantbackend.mapper

import com.restaurante.restaurantbackend.dto.DetallePedidoResponse
import com.restaurante.restaurantbackend.dto.PedidoResponse
import com.restaurante.restaurantbackend.entity.Pedido

object PedidoMapper {
    fun toResponse(pedido: Pedido): PedidoResponse =
        PedidoResponse(
            id = requireNotNull(pedido.id),
            mesaId = pedido.mesa?.id,
            meseroId = pedido.mesero?.id,
            tipo = pedido.tipo,
            estado = pedido.estado,
            metodoPago = pedido.metodoPago,
            estadoPago = pedido.estadoPago,
            total = pedido.total,
            fechaCreacion = pedido.fechaCreacion,
            detalles = pedido.detalles.map { detalle ->
                DetallePedidoResponse(
                    productoId = requireNotNull(detalle.producto?.id),
                    nombreProducto = detalle.nombreProducto,
                    precioUnitario = detalle.precioUnitario,
                    cantidad = detalle.cantidad,
                    notas = detalle.notas
                )
            }
        )
}
