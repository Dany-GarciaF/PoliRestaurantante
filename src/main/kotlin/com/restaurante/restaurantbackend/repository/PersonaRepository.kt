package com.restaurante.restaurantbackend.repository

import com.restaurante.restaurantbackend.entity.Persona
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PersonaRepository : JpaRepository<Persona, Int> {
    fun findByNombre(nombre: String): Persona?
    fun findByEmail(email: String): Persona?
}