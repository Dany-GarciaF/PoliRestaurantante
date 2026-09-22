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
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "historial_estados")
class HistorialEstado(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false)
    var pedido: Pedido? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var estado: EstadoPedido = EstadoPedido.PENDIENTE,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    var usuario: Usuario? = null,

    @Column(nullable = false)
    var fecha: OffsetDateTime = OffsetDateTime.now()
)
