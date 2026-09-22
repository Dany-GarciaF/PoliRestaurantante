package com.restaurante.restaurantbackend.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.validation.constraints.Positive
import java.util.UUID

@Entity
@Table(name = "mesas")
class Mesa(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @field:Positive
    @Column(nullable = false, unique = true)
    var numero: Int = 0,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    var estado: EstadoMesa = EstadoMesa.LIBRE,

    @Column(nullable = false)
    var activa: Boolean = true
)
