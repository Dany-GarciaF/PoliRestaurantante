package com.restaurante.restaurantbackend.controller

import com.restaurante.restaurantbackend.entity.Persona
import com.restaurante.restaurantbackend.service.PersonaService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/personas")
class PersonaController(
    private val personaService: PersonaService
) {

    @PostMapping
    fun crear(@RequestBody persona: Persona): ResponseEntity<Persona> =
        ResponseEntity.ok(personaService.crear(persona))

    @GetMapping
    fun obtenerTodos(): ResponseEntity<List<Persona>> =
        ResponseEntity.ok(personaService.obtenerTodos())

    @GetMapping("/{id}")
    fun obtenerPorId(@PathVariable id: Int): ResponseEntity<Persona> =
        ResponseEntity.ok(personaService.obtenerPorId(id))

    @GetMapping("/nombre/{nombre}")
    fun obtenerPorNombre(@PathVariable nombre: String): ResponseEntity<Persona> =
        ResponseEntity.ok(personaService.obtenerPorNombre(nombre))

    @GetMapping("/email/{email}")
    fun obtenerPorEmail(@PathVariable email: String): ResponseEntity<Persona> =
        ResponseEntity.ok(personaService.obtenerPorEmail(email))

    @PutMapping("/{id}")
    fun actualizar(
        @PathVariable id: Int,
        @RequestBody persona: Persona
    ): ResponseEntity<Persona> =
        ResponseEntity.ok(personaService.actualizar(
            id,
            persona.nombre,
            persona.apellido,
            persona.email,
            persona.num_documento
        ))

    @DeleteMapping("/{id}")
    fun eliminar(@PathVariable id: Int): ResponseEntity<String> {
        personaService.eliminar(id)
        return ResponseEntity.ok("Persona eliminada correctamente")
    }
}