package com.restaurante.restaurantbackend.service

import com.restaurante.restaurantbackend.dto.ActualizarConfiguracionDomiciliosRequest
import com.restaurante.restaurantbackend.dto.ConfiguracionDomiciliosResponse
import com.restaurante.restaurantbackend.entity.ConfiguracionDomicilios
import com.restaurante.restaurantbackend.repository.ConfiguracionDomiciliosRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
@Transactional(readOnly = true)
class ConfiguracionDomiciliosService(
    private val configuracionRepository: ConfiguracionDomiciliosRepository
) {
    fun obtenerActiva(): ConfiguracionDomiciliosResponse = toResponse(obtenerEntidadActiva())

    @Transactional
    fun actualizar(solicitud: ActualizarConfiguracionDomiciliosRequest): ConfiguracionDomiciliosResponse {
        val configuracion = obtenerEntidadActiva()
        configuracion.montoMaximoContraentrega = solicitud.montoMaximoContraentrega
        return toResponse(configuracion)
    }

    fun obtenerEntidadActiva(): ConfiguracionDomicilios =
        configuracionRepository.findFirstByActivaTrue()
            ?: throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "No existe una configuración activa para domicilios."
            )

    private fun toResponse(configuracion: ConfiguracionDomicilios): ConfiguracionDomiciliosResponse =
        ConfiguracionDomiciliosResponse(
            id = requireNotNull(configuracion.id),
            montoMaximoContraentrega = configuracion.montoMaximoContraentrega,
            activa = configuracion.activa
        )
}
