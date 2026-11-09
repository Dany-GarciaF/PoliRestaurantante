package com.restaurante.restaurantbackend.config

import com.restaurante.restaurantbackend.service.AlmacenImagenes
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class UploadsConfiguration(
    private val almacen: AlmacenImagenes
) : WebMvcConfigurer {
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler("/uploads/**")
            .addResourceLocations(almacen.raizAbsoluta().toUri().toString().trimEnd('/') + "/")
    }
}
