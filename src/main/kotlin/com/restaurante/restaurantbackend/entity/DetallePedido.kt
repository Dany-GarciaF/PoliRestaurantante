package com.restaurante.restaurantbackend.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.validation.constraints.Positive
import java.math.BigDecimal
import java.util.UUID

@Entity
@Table(name = "detalles_pedido")
class DetallePedido(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false)
    var pedido: Pedido? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    var producto: Producto? = null,

    @Column(name = "nombre_producto", nullable = false, length = 150)
    var nombreProducto: String = "",

    @Column(name = "precio_unitario", nullable = false, precision = 12, scale = 2)
    var precioUnitario: BigDecimal = BigDecimal.ZERO,

    @field:Positive
    @Column(nullable = false)
    var cantidad: Int = 1,

    @Column(length = 500)
    var notas: String? = null
)
