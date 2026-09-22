package com.restaurante.restaurantbackend.controller

import com.restaurante.restaurantbackend.dto.MesaRequest
import com.restaurante.restaurantbackend.dto.MesaResponse
import com.restaurante.restaurantbackend.service.MesaService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/admin/mesas")
@PreAuthorize("hasRole('ADMINISTRADOR')")
class MesaController(
    private val mesaService: MesaService
) {
    @GetMapping
    fun listar(): List<MesaResponse> = mesaService.listar()

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun crear(@Valid @RequestBody solicitud: MesaRequest): MesaResponse =
        mesaService.crear(solicitud)

    @PutMapping("/{id}")
    fun actualizar(
        @PathVariable id: UUID,
        @Valid @RequestBody solicitud: MesaRequest
    ): MesaResponse = mesaService.actualizar(id, solicitud)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun desactivar(@PathVariable id: UUID) {
        mesaService.desactivar(id)
    }
}
