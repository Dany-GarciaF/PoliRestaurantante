package com.restaurante.restaurantbackend.repository

import com.restaurante.restaurantbackend.entity.Usuario
import com.restaurante.restaurantbackend.entity.RolUsuario
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface UsuarioRepository : JpaRepository<Usuario, UUID> {
    fun existsByCorreoIgnoreCase(correo: String): Boolean
    fun findByCorreoIgnoreCase(correo: String): Usuario?
    fun countByRolAndActivoTrue(rol: RolUsuario): Long
}
