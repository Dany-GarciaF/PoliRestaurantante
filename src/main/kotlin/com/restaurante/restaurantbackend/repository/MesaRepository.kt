package com.restaurante.restaurantbackend.repository

import com.restaurante.restaurantbackend.entity.Mesa
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface MesaRepository : JpaRepository<Mesa, UUID> {
    fun existsByNumero(numero: Int): Boolean

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select mesa from Mesa mesa where mesa.id = :id")
    fun findByIdForUpdate(@Param("id") id: UUID): Mesa?
}
