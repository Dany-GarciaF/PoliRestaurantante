package com.restaurante.restaurantbackend.service

import com.restaurante.restaurantbackend.dto.CrearUsuarioRequest
import com.restaurante.restaurantbackend.dto.CambiarEstadoUsuarioRequest
import com.restaurante.restaurantbackend.dto.UsuarioResponse
import com.restaurante.restaurantbackend.entity.RolUsuario
import com.restaurante.restaurantbackend.entity.Usuario
import com.restaurante.restaurantbackend.mapper.UsuarioMapper
import com.restaurante.restaurantbackend.repository.UsuarioRepository
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class UsuarioService(
    private val usuarioRepository: UsuarioRepository,
    private val passwordEncoder: PasswordEncoder
) {
    @Transactional
    fun crear(solicitud: CrearUsuarioRequest): UsuarioResponse {
        val correo = solicitud.correo.trim().lowercase()
        if (usuarioRepository.existsByCorreoIgnoreCase(correo)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un usuario con ese correo.")
        }

        val usuario = Usuario(
            nombre = solicitud.nombre.trim(),
            correo = correo,
            contrasenaHash = requireNotNull(passwordEncoder.encode(solicitud.contrasena)),
            rol = solicitud.rol
        )
        return UsuarioMapper.toResponse(usuarioRepository.save(usuario))
    }

    @Transactional
    fun cambiarEstado(id: java.util.UUID, solicitud: CambiarEstadoUsuarioRequest): UsuarioResponse {
        val usuario = usuarioRepository.findById(id).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado.")
        }
        if (!solicitud.activo &&
            usuario.activo &&
            usuario.rol == RolUsuario.ADMINISTRADOR &&
            usuarioRepository.countByRolAndActivoTrue(RolUsuario.ADMINISTRADOR) == 1L
        ) {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "No se puede desactivar el último administrador activo."
            )
        }

        usuario.activo = solicitud.activo
        return UsuarioMapper.toResponse(usuario)
    }
}
