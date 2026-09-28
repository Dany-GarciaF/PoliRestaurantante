package com.restaurante.restaurantbackend.service

import com.restaurante.restaurantbackend.entity.Persona
import com.restaurante.restaurantbackend.repository.PersonaRepository
import org.springframework.stereotype.Service

@Service
class PersonaService(
    private val personaRepository: PersonaRepository
) {

    fun crear(persona: Persona): Persona =
        personaRepository.save(persona)

    fun obtenerPorId(id: Int): Persona =
        personaRepository.findById(id).orElseThrow { RuntimeException("Persona no encontrada") }

    fun obtenerTodos(): List<Persona> =
        personaRepository.findAll()

    fun obtenerPorNombre(nombre: String): Persona =
        personaRepository.findByNombre(nombre) ?: throw RuntimeException("Persona no encontrada")

    fun obtenerPorEmail(email: String): Persona =
        personaRepository.findByEmail(email) ?: throw RuntimeException("Persona no encontrada")

    fun actualizar(id: Int, nombre: String?, apellido: String?, email: String?, numDocumento: String?): Persona {
        val persona = obtenerPorId(id)
        val actualizada = persona.copy(
            nombre = nombre,
            apellido = apellido,
            email = email,
            num_documento = numDocumento
        )
        return personaRepository.save(actualizada)
    }

    fun eliminar(id: Int) =
        personaRepository.deleteById(id)
}