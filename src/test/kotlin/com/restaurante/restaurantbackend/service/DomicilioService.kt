package com.restaurante.restaurantbackend.service

import com.restaurante.restaurantbackend.entity.Domicilio
import com.restaurante.restaurantbackend.entity.EstadoDomicilio
import com.restaurante.restaurantbackend.repository.DomicilioRepository
import org.springframework.stereotype.Service

@Service
class DomicilioService(
    private val domicilioRepository: DomicilioRepository
) {

    fun crearDomicilio(domicilio: Domicilio): Domicilio =
        domicilioRepository.save(domicilio)

    fun listarDomicilios(): List<Domicilio> =
        domicilioRepository.findAll()

    fun obtenerDomicilio(id: Long): Domicilio =
        domicilioRepository.findById(id).orElseThrow { RuntimeException("Domicilio no encontrado") }

    fun cambiarEstado(id: Long, estado: EstadoDomicilio): Domicilio {
        val domicilio = obtenerDomicilio(id)
        domicilio.estado = estado
        return domicilioRepository.save(domicilio)
    }

    fun cancelarDomicilio(id: Long): Domicilio =
        cambiarEstado(id, EstadoDomicilio.CANCELADO)
}