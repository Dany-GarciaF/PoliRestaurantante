package com.restaurante.restaurantbackend.repository

import com.restaurante.restaurantbackend.entity.Pago
import org.springframework.data.jpa.repository.JpaRepository

interface PagoRepository : JpaRepository<Pago, Long>