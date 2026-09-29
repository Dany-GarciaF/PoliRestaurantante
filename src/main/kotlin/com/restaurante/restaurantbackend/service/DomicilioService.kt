package com.restaurante.restaurantbackend.service

import com.restaurante.restaurantbackend.dto.CambiarEstadoDomicilioRequest
import com.restaurante.restaurantbackend.dto.CrearDomicilioRequest
import com.restaurante.restaurantbackend.dto.DomicilioResponse
import com.restaurante.restaurantbackend.entity.Domicilio
import com.restaurante.restaurantbackend.entity.TipoPedido
import com.restaurante.restaurantbackend.mapper.DomicilioMapper
import com.restaurante.restaurantbackend.repository.DomicilioRepository
import com.restaurante.restaurantbackend.repository.PedidoRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@Service
@Transactional(readOnly = true)
class DomicilioService(
    private val domicilioRepository: DomicilioRepository,
    private val pedidoRepository: PedidoRepository
) {

    @Transactional
    fun crear(solicitud: CrearDomicilioRequest): DomicilioResponse {
        val pedido = pedidoRepository.findById(solicitud.pedidoId).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado.")
        }

        if (pedido.tipo != TipoPedido.DOMICILIO) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "El pedido no es de tipo domicilio."
            )
        }

        if (domicilioRepository.existsByPedidoId(solicitud.pedidoId)) {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "El pedido ya tiene un domicilio registrado."
            )
        }

        val domicilio = Domicilio(
            pedido = pedido,
            nombreCliente = solicitud.nombreCliente.trim(),
            telefono = solicitud.telefono.trim(),
            direccion = solicitud.direccion.trim()
        )

        return DomicilioMapper.toResponse(
            domicilioRepository.save(domicilio)
        )
    }

    fun listar(): List<DomicilioResponse> =
        domicilioRepository.findAll()
            .map(DomicilioMapper::toResponse)

    fun obtener(id: UUID): DomicilioResponse =
        DomicilioMapper.toResponse(obtenerEntidad(id))

    fun obtenerPorPedido(pedidoId: UUID): DomicilioResponse {
        val domicilio = domicilioRepository.findByPedidoId(pedidoId)
            ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Domicilio no encontrado."
            )

        return DomicilioMapper.toResponse(domicilio)
    }

    @Transactional
    fun cambiarEstado(
        id: UUID,
        solicitud: CambiarEstadoDomicilioRequest
    ): DomicilioResponse {
        val domicilio = obtenerEntidad(id)
        domicilio.estado = solicitud.estado
        return DomicilioMapper.toResponse(domicilio)
    }

    private fun obtenerEntidad(id: UUID): Domicilio =
        domicilioRepository.findById(id).orElseThrow {
            ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Domicilio no encontrado."
            )
        }
}