package com.restaurante.restaurantbackend.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
import jakarta.persistence.Version
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "pedidos")
class Pedido(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mesa_id")
    var mesa: Mesa? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mesero_id")
    var mesero: Usuario? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    var tipo: TipoPedido = TipoPedido.SALON,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var estado: EstadoPedido = EstadoPedido.PENDIENTE,

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago", length = 20)
    var metodoPago: MetodoPago? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_pago", nullable = false, length = 20)
    var estadoPago: EstadoPago = EstadoPago.PENDIENTE,

    @Column(nullable = false, precision = 12, scale = 2)
    var total: BigDecimal = BigDecimal.ZERO,

    @Embedded
    var datosEntrega: DatosEntrega? = null,

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    var fechaCreacion: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "fecha_ultima_actualizacion", nullable = false)
    var fechaUltimaActualizacion: OffsetDateTime = OffsetDateTime.now(),

    @Version
    @Column(nullable = false)
    var version: Long = 0,

    @OneToMany(mappedBy = "pedido", cascade = [CascadeType.ALL], orphanRemoval = true)
    var detalles: MutableList<DetallePedido> = mutableListOf()
) {
    @PrePersist
    fun asignarFechasCreacion() {
        val ahora = OffsetDateTime.now()
        fechaCreacion = ahora
        fechaUltimaActualizacion = ahora
    }

    @PreUpdate
    fun actualizarFechaModificacion() {
        fechaUltimaActualizacion = OffsetDateTime.now()
    }
}
