package com.restaurante.restaurantbackend.controller

import com.restaurante.restaurantbackend.dto.ActualizarConfiguracionDomiciliosRequest
import com.restaurante.restaurantbackend.dto.ConfiguracionDomiciliosResponse
import com.restaurante.restaurantbackend.service.ConfiguracionDomiciliosService
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/configuracion/domicilios")
@PreAuthorize("hasRole('ADMINISTRADOR')")
class ConfiguracionDomiciliosController(
    private val configuracionDomiciliosService: ConfiguracionDomiciliosService
) {
    @GetMapping
    fun obtener(): ConfiguracionDomiciliosResponse = configuracionDomiciliosService.obtenerActiva()

    @PutMapping
    fun actualizar(
        @Valid @RequestBody solicitud: ActualizarConfiguracionDomiciliosRequest
    ): ConfiguracionDomiciliosResponse = configuracionDomiciliosService.actualizar(solicitud)
}
