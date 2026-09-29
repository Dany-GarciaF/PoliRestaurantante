package com.restaurante.restaurantbackend.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "domicilios")
class Domicilio(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false, unique = true)
    var pedido: Pedido? = null,

    @Column(name = "nombre_cliente", nullable = false, length = 120)
    var nombreCliente: String = "",

    @Column(nullable = false, length = 30)
    var telefono: String = "",

    @Column(nullable = false, length = 250)
    var direccion: String = "",

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var estado: EstadoDomicilio = EstadoDomicilio.PENDIENTE,

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    var fechaCreacion: OffsetDateTime = OffsetDateTime.now()
)

enum class EstadoDomicilio {
    PENDIENTE,
    EN_PREPARACION,
    EN_CAMINO,
    ENTREGADO,
    CANCELADO
}