package com.restaurante.restaurantbackend.config

import org.springframework.boot.context.properties.ConfigurationProperties
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive

@ConfigurationProperties("app.security.jwt")
class JwtProperties {
    @field:NotBlank
    var secretBase64: String = ""

    @field:Positive
    var expirationMinutes: Long = 60
}
