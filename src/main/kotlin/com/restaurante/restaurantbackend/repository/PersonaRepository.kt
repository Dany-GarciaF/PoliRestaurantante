package com.restaurante.restaurantbackend.repository

import com.restaurante.restaurantbackend.entity.Persona
import org.springframework.data.jpa.repository.JpaRepository

interface PersonaRepository : JpaRepository<Persona, Int>