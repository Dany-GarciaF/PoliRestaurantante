package com.restaurante.restaurantbackend.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "pagos")
data class Pago(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    val idDomicilio: Long? = null,
    val idPedido: Long? = null,

    @Enumerated(EnumType.STRING)
    val metodoPago: MetodoPago,

    val total: Double,

    @Enumerated(EnumType.STRING)
    var estadoPago: EstadoPago = EstadoPago.PENDIENTE,

    val fecha: LocalDateTime = LocalDateTime.now()
)

enum class MetodoPago {
    EFECTIVO,
    TARJETA
}

enum class EstadoPago {
    PENDIENTE,
    PAGADO
}