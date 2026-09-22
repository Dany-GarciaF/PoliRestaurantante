package com.restaurante.restaurantbackend.service

import com.restaurante.restaurantbackend.dto.LoginRequest
import com.restaurante.restaurantbackend.dto.LoginResponse
import com.restaurante.restaurantbackend.repository.UsuarioRepository
import com.restaurante.restaurantbackend.security.JwtService
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class AuthService(
    private val usuarioRepository: UsuarioRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService
) {
    fun iniciarSesion(solicitud: LoginRequest): LoginResponse {
        val usuario = usuarioRepository.findByCorreoIgnoreCase(solicitud.correo.trim())
            ?: throw invalidCredentials()
        if (!usuario.activo || !passwordEncoder.matches(solicitud.contrasena, usuario.contrasenaHash)) {
            throw invalidCredentials()
        }

        return LoginResponse(
            accessToken = jwtService.generateToken(usuario),
            rol = usuario.rol
        )
    }

    private fun invalidCredentials() =
        ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas.")
}
