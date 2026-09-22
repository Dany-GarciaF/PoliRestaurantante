package com.restaurante.restaurantbackend.controller

import com.restaurante.restaurantbackend.entity.Domicilio
import com.restaurante.restaurantbackend.entity.EstadoDomicilio
import com.restaurante.restaurantbackend.service.DomicilioService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/domicilios")
class DomicilioController(
    private val domicilioService: DomicilioService
) {

    @PostMapping
    fun crear(@RequestBody domicilio: Domicilio): ResponseEntity<Domicilio> =
        ResponseEntity.ok(domicilioService.crearDomicilio(domicilio))

    @GetMapping
    fun listar(): ResponseEntity<List<Domicilio>> =
        ResponseEntity.ok(domicilioService.listarDomicilios())

    @GetMapping("/{id}")
    fun obtener(@PathVariable id: Long): ResponseEntity<Domicilio> =
        ResponseEntity.ok(domicilioService.obtenerDomicilio(id))

    @PutMapping("/{id}/estado")
    fun cambiarEstado(
        @PathVariable id: Long,
        @RequestParam estado: EstadoDomicilio
    ): ResponseEntity<Domicilio> =
        ResponseEntity.ok(domicilioService.cambiarEstado(id, estado))

    @DeleteMapping("/{id}")
    fun cancelar(@PathVariable id: Long): ResponseEntity<Domicilio> =
        ResponseEntity.ok(domicilioService.cancelarDomicilio(id))
}