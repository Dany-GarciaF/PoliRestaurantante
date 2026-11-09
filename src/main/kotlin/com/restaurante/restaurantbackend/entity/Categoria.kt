package com.restaurante.restaurantbackend.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.validation.constraints.NotBlank
import java.util.UUID

@Entity
@Table(name = "categorias")
class Categoria(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @field:NotBlank
    @Column(nullable = false, unique = true, length = 100)
    var nombre: String = "",

    @Column(length = 300)
    var descripcion: String? = null,

    @Column(nullable = false)
    var activa: Boolean = true
)
