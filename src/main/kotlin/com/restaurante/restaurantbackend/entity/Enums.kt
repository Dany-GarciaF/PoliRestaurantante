package com.restaurante.restaurantbackend.entity

enum class RolUsuario {
    ADMINISTRADOR,
    MESERO,
    COCINA
}

enum class EstadoMesa {
    LIBRE,
    OCUPADA
}

enum class TipoPedido {
    SALON,
    PARA_LLEVAR,
    DOMICILIO
}

enum class EstadoPedido {
    PENDIENTE,
    EN_PREPARACION,
    LISTO,
    ENTREGADO,
    CANCELADO
}

enum class MetodoPago {
    EFECTIVO_CONTRAENTREGA,
    PAGO_VIRTUAL
}

enum class EstadoPago {
    PENDIENTE,
    PAGADO,
    RECHAZADO,
    REEMBOLSADO
}
