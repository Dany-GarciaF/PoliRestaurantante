package com.restaurante.restaurantbackend.controller

import com.restaurante.restaurantbackend.dto.LoginRequest
import com.restaurante.restaurantbackend.dto.LoginResponse
import com.restaurante.restaurantbackend.service.AuthService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService
) {
    @PostMapping("/login")
    fun iniciarSesion(@Valid @RequestBody solicitud: LoginRequest): ResponseEntity<LoginResponse> =
        ResponseEntity.ok(authService.iniciarSesion(solicitud))
}
