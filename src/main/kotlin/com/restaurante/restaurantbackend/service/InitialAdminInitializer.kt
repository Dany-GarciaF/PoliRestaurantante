package com.restaurante.restaurantbackend.service

import com.restaurante.restaurantbackend.config.InitialAdminProperties
import com.restaurante.restaurantbackend.entity.RolUsuario
import com.restaurante.restaurantbackend.entity.Usuario
import com.restaurante.restaurantbackend.repository.UsuarioRepository
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class InitialAdminInitializer(
    private val properties: InitialAdminProperties,
    private val usuarioRepository: UsuarioRepository,
    private val passwordEncoder: PasswordEncoder
) : ApplicationRunner {
    override fun run(args: ApplicationArguments) {
        if (usuarioRepository.count() > 0) {
            return
        }

        require(properties.nombre.isNotBlank() && properties.correo.isNotBlank() && properties.contrasena.isNotBlank()) {
            "La base de datos no tiene usuarios. Configure INITIAL_ADMIN_NAME, INITIAL_ADMIN_EMAIL e INITIAL_ADMIN_PASSWORD."
        }

        usuarioRepository.save(
            Usuario(
                nombre = properties.nombre.trim(),
                correo = properties.correo.trim().lowercase(),
                contrasenaHash = requireNotNull(passwordEncoder.encode(properties.contrasena)),
                rol = RolUsuario.ADMINISTRADOR
            )
        )
    }
}
