package com.restaurante.restaurantbackend.controller

import com.restaurante.restaurantbackend.dto.MesaResponse
import com.restaurante.restaurantbackend.service.MesaService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/mesas")
@PreAuthorize("hasRole('MESERO')")
class MesaDisponibleController(
    private val mesaService: MesaService
) {
    @GetMapping("/disponibles")
    fun listarDisponibles(): List<MesaResponse> = mesaService.listarDisponibles()
}
