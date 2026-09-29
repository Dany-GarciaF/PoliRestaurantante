package com.restaurante.restaurantbackend.service

import com.restaurante.restaurantbackend.dto.CambiarEstadoPagoRequest
import com.restaurante.restaurantbackend.dto.CrearPagoRequest
import com.restaurante.restaurantbackend.dto.PagoResponse
import com.restaurante.restaurantbackend.entity.EstadoPago
import com.restaurante.restaurantbackend.entity.Pago
import com.restaurante.restaurantbackend.mapper.PagoMapper
import com.restaurante.restaurantbackend.repository.PagoRepository
import com.restaurante.restaurantbackend.repository.PedidoRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@Service
@Transactional(readOnly = true)
class PagoService(
    private val pagoRepository: PagoRepository,
    private val pedidoRepository: PedidoRepository
) {

    @Transactional
    fun registrar(solicitud: CrearPagoRequest): PagoResponse {
        val pedido = pedidoRepository.findById(solicitud.pedidoId).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado.")
        }

        if (solicitud.total.compareTo(pedido.total) != 0) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "El total del pago debe coincidir con el total del pedido."
            )
        }

        val pago = Pago(
            pedido = pedido,
            metodoPago = solicitud.metodoPago,
            total = solicitud.total
        )

        return PagoMapper.toResponse(pagoRepository.save(pago))
    }

    fun obtener(id: UUID): PagoResponse =
        PagoMapper.toResponse(obtenerEntidad(id))

    fun listarPorPedido(pedidoId: UUID): List<PagoResponse> =
        pagoRepository.findByPedidoIdOrderByFechaDesc(pedidoId)
            .map(PagoMapper::toResponse)

    @Transactional
    fun cambiarEstado(
        id: UUID,
        solicitud: CambiarEstadoPagoRequest
    ): PagoResponse {
        val pago = obtenerEntidad(id)

        if (pago.estadoPago == EstadoPago.REEMBOLSADO) {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "Un pago reembolsado no puede cambiar de estado."
            )
        }

        pago.estadoPago = solicitud.estado
        pago.pedido?.estadoPago = solicitud.estado

        return PagoMapper.toResponse(pago)
    }

    private fun obtenerEntidad(id: UUID): Pago =
        pagoRepository.findById(id).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Pago no encontrado.")
        }
}