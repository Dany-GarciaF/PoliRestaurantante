package com.restaurante.restaurantbackend.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "domicilios")
data class Domicilio(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    val nombreCliente: String,
    val telefono: String,
    val direccion: String,

    @Enumerated(EnumType.STRING)
    var estado: EstadoDomicilio = EstadoDomicilio.PENDIENTE,

    val fechaCreacion: LocalDateTime = LocalDateTime.now()
)

enum class EstadoDomicilio {
    PENDIENTE,
    EN_PREPARACION,
    EN_CAMINO,
    ENTREGADO,
    CANCELADO
}