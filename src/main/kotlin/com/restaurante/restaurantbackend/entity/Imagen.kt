package com.restaurante.restaurantbackend.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.OffsetDateTime

@Entity
@Table(name = "imagenes")
class Imagen(
    @Id
    @Column(length = 80)
    var nombre: String = "",

    @Column(name = "tipo_contenido", nullable = false, length = 40)
    var tipoContenido: String = "",

    @JdbcTypeCode(SqlTypes.VARBINARY)
    @Column(nullable = false)
    var datos: ByteArray = ByteArray(0),

    @Column(name = "fecha_creacion", nullable = false)
    var fechaCreacion: OffsetDateTime = OffsetDateTime.now()
)
