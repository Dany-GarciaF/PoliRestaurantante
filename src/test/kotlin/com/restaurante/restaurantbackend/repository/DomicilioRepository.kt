package com.restaurante.restaurantbackend.repository

import com.restaurante.restaurantbackend.entity.Domicilio
import org.springframework.data.jpa.repository.JpaRepository

interface DomicilioRepository : JpaRepository<Domicilio, Long>