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
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import java.math.BigDecimal
import java.util.UUID

@Entity
@Table(name = "productos")
class Producto(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @field:NotBlank
    @Column(nullable = false, unique = true, length = 150)
    var nombre: String = "",

    @Column(length = 500)
    var descripcion: String? = null,

    @field:DecimalMin("0.00")
    @Column(nullable = false, precision = 12, scale = 2)
    var precio: BigDecimal = BigDecimal.ZERO,

    @Column(nullable = false)
    var disponible: Boolean = true,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    var categoria: Categoria? = null
)
