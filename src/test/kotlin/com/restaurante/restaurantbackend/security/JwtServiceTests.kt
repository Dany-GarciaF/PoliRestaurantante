package com.restaurante.restaurantbackend.security

import com.restaurante.restaurantbackend.entity.RolUsuario
import com.restaurante.restaurantbackend.entity.Usuario
import kotlin.test.Test
import kotlin.test.assertEquals
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class JwtServiceTests(
    @Autowired private val jwtService: JwtService
) {
    @Test
    fun `genera un token firmado con el correo del usuario`() {
        val usuario = Usuario(
            correo = "mesero@polirestaurante.local",
            rol = RolUsuario.MESERO
        )

        val token = jwtService.generateToken(usuario)

        assertEquals(usuario.correo, jwtService.extractCorreo(token))
    }
}
