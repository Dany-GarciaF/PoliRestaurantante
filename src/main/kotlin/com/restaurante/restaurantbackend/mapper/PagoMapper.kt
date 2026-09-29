package com.restaurante.restaurantbackend.mapper

import com.restaurante.restaurantbackend.dto.PagoResponse
import com.restaurante.restaurantbackend.entity.Pago

object PagoMapper {

    fun toResponse(pago: Pago): PagoResponse =
        PagoResponse(
            id = requireNotNull(pago.id),
            pedidoId = requireNotNull(pago.pedido?.id),
            metodoPago = pago.metodoPago,
            total = pago.total,
            estadoPago = pago.estadoPago,
            fecha = pago.fecha
        )
}