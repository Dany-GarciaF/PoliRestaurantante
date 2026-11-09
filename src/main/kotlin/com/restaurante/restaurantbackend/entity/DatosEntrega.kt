package com.restaurante.restaurantbackend.entity

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.validation.constraints.NotBlank

@Embeddable
class DatosEntrega(
    @field:NotBlank
    @Column(name = "cliente_nombre", length = 120)
    var nombreCliente: String? = null,

    @field:NotBlank
    @Column(name = "cliente_telefono", length = 30)
    var telefonoCliente: String? = null,

    @field:NotBlank
    @Column(name = "direccion_entrega", length = 250)
    var direccion: String? = null,

    @Column(name = "barrio_entrega", length = 100)
    var barrio: String? = null,

    @Column(name = "referencias_entrega", length = 500)
    var referencias: String? = null
)
