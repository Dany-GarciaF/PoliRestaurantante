package com.restaurante.restaurantbackend.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.validation.constraints.DecimalMin
import java.math.BigDecimal
import java.util.UUID

@Entity
@Table(name = "configuracion_domicilios")
class ConfiguracionDomicilios(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @field:DecimalMin("0.00")
    @Column(name = "monto_maximo_contraentrega", nullable = false, precision = 12, scale = 2)
    var montoMaximoContraentrega: BigDecimal = BigDecimal.ZERO,

    @Column(nullable = false)
    var activa: Boolean = true
)
