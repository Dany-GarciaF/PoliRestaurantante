package com.restaurante.restaurantbackend.service

import com.restaurante.restaurantbackend.entity.Pago
import com.restaurante.restaurantbackend.repository.PagoRepository
import org.springframework.stereotype.Service

@Service
class PagoService(
    private val pagoRepository: PagoRepository
) {

    fun registrarPago(pago: Pago): Pago =
        pagoRepository.save(pago)

    fun obtenerPago(id: Long): Pago =
        pagoRepository.findById(id).orElseThrow { RuntimeException("Pago no encontrado") }

    fun obtenerPagoPorDomicilio(idDomicilio: Long): List<Pago> =
        pagoRepository.findAll().filter { it.idDomicilio == idDomicilio }

    fun obtenerPagoPorPedido(idPedido: Long): List<Pago> =
        pagoRepository.findAll().filter { it.idPedido == idPedido }
}