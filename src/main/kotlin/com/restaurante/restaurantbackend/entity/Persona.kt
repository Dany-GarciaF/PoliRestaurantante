package com.restaurante.restaurantbackend.entity

import jakarta.persistence.*

@Entity
@Table(name = "persona")
data class Persona(

    @Id
    val idpersona: Int,

    val nombre: String? = null,
    val apellido: String? = null,
    val email: String? = null,
    val num_documento: String? = null
)