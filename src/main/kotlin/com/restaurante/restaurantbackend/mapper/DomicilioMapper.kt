package com.restaurante.restaurantbackend.mapper

import com.restaurante.restaurantbackend.dto.DomicilioResponse
import com.restaurante.restaurantbackend.entity.Domicilio

object DomicilioMapper {

    fun toResponse(domicilio: Domicilio): DomicilioResponse =
        DomicilioResponse(
            id = requireNotNull(domicilio.id),
            pedidoId = requireNotNull(domicilio.pedido?.id),
            nombreCliente = domicilio.nombreCliente,
            telefono = domicilio.telefono,
            direccion = domicilio.direccion,
            estado = domicilio.estado,
            fechaCreacion = domicilio.fechaCreacion
        )
}