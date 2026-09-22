package com.restaurante.restaurantbackend.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "usuarios")
class Usuario(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @field:NotBlank
    @Column(nullable = false, length = 120)
    var nombre: String = "",

    @field:Email
    @field:NotBlank
    @Column(nullable = false, unique = true, length = 160)
    var correo: String = "",

    @Column(name = "contrasena_hash", nullable = false, length = 255)
    var contrasenaHash: String = "",

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var rol: RolUsuario = RolUsuario.MESERO,

    @Column(nullable = false)
    var activo: Boolean = true,

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    var fechaCreacion: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "fecha_actualizacion", nullable = false)
    var fechaActualizacion: OffsetDateTime = OffsetDateTime.now()
) {
    @PrePersist
    fun asignarFechasCreacion() {
        val ahora = OffsetDateTime.now()
        fechaCreacion = ahora
        fechaActualizacion = ahora
    }

    @PreUpdate
    fun actualizarFechaModificacion() {
        fechaActualizacion = OffsetDateTime.now()
    }
}
