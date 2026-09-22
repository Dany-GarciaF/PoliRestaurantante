package com.restaurante.restaurantbackend.service

import com.restaurante.restaurantbackend.dto.MesaRequest
import com.restaurante.restaurantbackend.dto.MesaResponse
import com.restaurante.restaurantbackend.entity.EstadoMesa
import com.restaurante.restaurantbackend.entity.Mesa
import com.restaurante.restaurantbackend.mapper.AdministracionMapper
import com.restaurante.restaurantbackend.repository.MesaRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@Service
@Transactional(readOnly = true)
class MesaService(
    private val mesaRepository: MesaRepository
) {
    fun listar(): List<MesaResponse> =
        mesaRepository.findAll().map(AdministracionMapper::toResponse)

    @Transactional
    fun crear(solicitud: MesaRequest): MesaResponse {
        verificarNumeroDisponible(solicitud.numero, null)
        return AdministracionMapper.toResponse(
            mesaRepository.save(Mesa(numero = solicitud.numero, activa = solicitud.activa))
        )
    }

    @Transactional
    fun actualizar(id: UUID, solicitud: MesaRequest): MesaResponse {
        val mesa = obtenerEntidad(id)
        verificarNumeroDisponible(solicitud.numero, mesa)
        if (!solicitud.activa && mesa.estado == EstadoMesa.OCUPADA) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "No se puede desactivar una mesa ocupada.")
        }
        mesa.numero = solicitud.numero
        mesa.activa = solicitud.activa
        return AdministracionMapper.toResponse(mesa)
    }

    @Transactional
    fun desactivar(id: UUID) {
        val mesa = obtenerEntidad(id)
        if (mesa.estado == EstadoMesa.OCUPADA) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "No se puede desactivar una mesa ocupada.")
        }
        mesa.activa = false
    }

    private fun verificarNumeroDisponible(numero: Int, mesaActual: Mesa?) {
        if ((mesaActual == null || mesaActual.numero != numero) && mesaRepository.existsByNumero(numero)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una mesa con ese número.")
        }
    }

    private fun obtenerEntidad(id: UUID): Mesa =
        mesaRepository.findById(id).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Mesa no encontrada.")
        }
}
