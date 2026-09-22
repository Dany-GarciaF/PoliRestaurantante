package com.restaurante.restaurantbackend.repository

import com.restaurante.restaurantbackend.entity.ConfiguracionDomicilios
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ConfiguracionDomiciliosRepository : JpaRepository<ConfiguracionDomicilios, UUID> {
    fun findFirstByActivaTrue(): ConfiguracionDomicilios?
}
