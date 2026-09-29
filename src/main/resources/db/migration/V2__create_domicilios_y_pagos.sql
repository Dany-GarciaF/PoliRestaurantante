CREATE TABLE domicilios (
    id UUID PRIMARY KEY,
    pedido_id UUID NOT NULL UNIQUE REFERENCES pedidos(id) ON DELETE CASCADE,
    nombre_cliente VARCHAR(120) NOT NULL,
    telefono VARCHAR(30) NOT NULL,
    direccion VARCHAR(250) NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    fecha_creacion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT ck_domicilios_estado CHECK (
        estado IN (
            'PENDIENTE',
            'EN_PREPARACION',
            'EN_CAMINO',
            'ENTREGADO',
            'CANCELADO'
        )
    )
);

CREATE TABLE pagos (
    id UUID PRIMARY KEY,
    pedido_id UUID NOT NULL REFERENCES pedidos(id),
    metodo_pago VARCHAR(30) NOT NULL,
    total NUMERIC(12, 2) NOT NULL,
    estado_pago VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    fecha TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT ck_pagos_metodo CHECK (
        metodo_pago IN (
            'EFECTIVO_CONTRAENTREGA',
            'PAGO_VIRTUAL'
        )
    ),

    CONSTRAINT ck_pagos_estado CHECK (
        estado_pago IN (
            'PENDIENTE',
            'PAGADO',
            'RECHAZADO',
            'REEMBOLSADO'
        )
    ),

    CONSTRAINT ck_pagos_total CHECK (total > 0)
);

CREATE INDEX idx_domicilios_pedido_id
    ON domicilios(pedido_id);

CREATE INDEX idx_pagos_pedido_id
    ON pagos(pedido_id);