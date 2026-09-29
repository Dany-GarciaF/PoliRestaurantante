package com.restaurante.restaurantbackend.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("bootstrap.admin")
class InitialAdminProperties {
    var nombre: String = ""
    var correo: String = ""
    var contrasena: String = ""
}
