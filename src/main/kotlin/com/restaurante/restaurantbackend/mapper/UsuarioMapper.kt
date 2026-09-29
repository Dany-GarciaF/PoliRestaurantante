package com.restaurante.restaurantbackend.mapper

import com.restaurante.restaurantbackend.dto.UsuarioResponse
import com.restaurante.restaurantbackend.entity.Usuario

object UsuarioMapper {
    fun toResponse(usuario: Usuario): UsuarioResponse =
        UsuarioResponse(
            id = requireNotNull(usuario.id),
            nombre = usuario.nombre,
            correo = usuario.correo,
            rol = usuario.rol,
            activo = usuario.activo
        )
}
