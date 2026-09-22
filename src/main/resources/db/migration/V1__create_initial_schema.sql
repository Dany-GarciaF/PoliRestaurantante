CREATE TABLE usuarios (
    id UUID PRIMARY KEY,
    nombre VARCHAR(120) NOT NULL,
    correo VARCHAR(160) NOT NULL UNIQUE,
    contrasena_hash VARCHAR(255) NOT NULL,
    rol VARCHAR(20) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_usuarios_rol CHECK (rol IN ('ADMINISTRADOR', 'MESERO', 'COCINA'))
);

CREATE TABLE mesas (
    id UUID PRIMARY KEY,
    numero INTEGER NOT NULL UNIQUE,
    estado VARCHAR(10) NOT NULL DEFAULT 'LIBRE',
    activa BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT ck_mesas_numero CHECK (numero > 0),
    CONSTRAINT ck_mesas_estado CHECK (estado IN ('LIBRE', 'OCUPADA'))
);

CREATE TABLE categorias (
    id UUID PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    descripcion VARCHAR(300),
    activa BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE productos (
    id UUID PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL UNIQUE,
    descripcion VARCHAR(500),
    precio NUMERIC(12, 2) NOT NULL,
    disponible BOOLEAN NOT NULL DEFAULT TRUE,
    categoria_id UUID NOT NULL REFERENCES categorias(id),
    CONSTRAINT ck_productos_precio CHECK (precio >= 0)
);

CREATE TABLE configuracion_domicilios (
    id UUID PRIMARY KEY,
    monto_maximo_contraentrega NUMERIC(12, 2) NOT NULL,
    activa BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT ck_configuracion_domicilios_monto CHECK (monto_maximo_contraentrega >= 0)
);

CREATE TABLE pedidos (
    id UUID PRIMARY KEY,
    mesa_id UUID REFERENCES mesas(id),
    mesero_id UUID REFERENCES usuarios(id),
    tipo VARCHAR(15) NOT NULL,
    estado VARCHAR(20) NOT NULL,
    metodo_pago VARCHAR(20),
    estado_pago VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    total NUMERIC(12, 2) NOT NULL DEFAULT 0,
    cliente_nombre VARCHAR(120),
    cliente_telefono VARCHAR(30),
    direccion_entrega VARCHAR(250),
    barrio_entrega VARCHAR(100),
    referencias_entrega VARCHAR(500),
    fecha_creacion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_ultima_actualizacion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_pedidos_tipo CHECK (tipo IN ('SALON', 'PARA_LLEVAR', 'DOMICILIO')),
    CONSTRAINT ck_pedidos_estado CHECK (estado IN ('PENDIENTE', 'EN_PREPARACION', 'LISTO', 'ENTREGADO', 'CANCELADO')),
    CONSTRAINT ck_pedidos_metodo_pago CHECK (metodo_pago IS NULL OR metodo_pago IN ('EFECTIVO_CONTRAENTREGA', 'PAGO_VIRTUAL')),
    CONSTRAINT ck_pedidos_estado_pago CHECK (estado_pago IN ('PENDIENTE', 'PAGADO', 'RECHAZADO', 'REEMBOLSADO')),
    CONSTRAINT ck_pedidos_total CHECK (total >= 0)
);

CREATE TABLE detalles_pedido (
    id UUID PRIMARY KEY,
    pedido_id UUID NOT NULL REFERENCES pedidos(id) ON DELETE CASCADE,
    producto_id UUID NOT NULL REFERENCES productos(id),
    nombre_producto VARCHAR(150) NOT NULL,
    precio_unitario NUMERIC(12, 2) NOT NULL,
    cantidad INTEGER NOT NULL,
    notas VARCHAR(500),
    CONSTRAINT ck_detalles_pedido_precio CHECK (precio_unitario >= 0),
    CONSTRAINT ck_detalles_pedido_cantidad CHECK (cantidad > 0)
);

CREATE TABLE historial_estados (
    id UUID PRIMARY KEY,
    pedido_id UUID NOT NULL REFERENCES pedidos(id) ON DELETE CASCADE,
    estado VARCHAR(20) NOT NULL,
    usuario_id UUID REFERENCES usuarios(id),
    fecha TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_historial_estados_estado CHECK (estado IN ('PENDIENTE', 'EN_PREPARACION', 'LISTO', 'ENTREGADO', 'CANCELADO'))
);

CREATE INDEX idx_pedidos_estado_fecha_creacion ON pedidos(estado, fecha_creacion);
CREATE INDEX idx_pedidos_mesa_id ON pedidos(mesa_id);
CREATE INDEX idx_historial_estados_pedido_id ON historial_estados(pedido_id);
