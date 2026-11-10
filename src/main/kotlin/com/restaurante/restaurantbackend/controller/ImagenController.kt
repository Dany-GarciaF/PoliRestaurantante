package com.restaurante.restaurantbackend.controller

import com.restaurante.restaurantbackend.service.AlmacenImagenes
import org.springframework.http.CacheControl
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import java.time.Duration

@RestController
class ImagenController(private val almacen: AlmacenImagenes) {

    @GetMapping("/uploads/productos/{nombre}")
    fun obtener(@PathVariable nombre: String): ResponseEntity<ByteArray> {
        val (datos, tipo) = almacen.leer(nombre) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(tipo))
            .cacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePublic())
            .body(datos)
    }
}
