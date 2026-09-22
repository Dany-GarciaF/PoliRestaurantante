package com.restaurante.restaurantbackend.service

import com.restaurante.restaurantbackend.dto.CambiarEstadoPedidoRequest
import com.restaurante.restaurantbackend.dto.CrearPedidoRequest
import com.restaurante.restaurantbackend.dto.HistorialEstadoResponse
import com.restaurante.restaurantbackend.dto.PedidoResponse
import com.restaurante.restaurantbackend.entity.DatosEntrega
import com.restaurante.restaurantbackend.entity.DetallePedido
import com.restaurante.restaurantbackend.entity.EstadoMesa
import com.restaurante.restaurantbackend.entity.EstadoPedido
import com.restaurante.restaurantbackend.entity.HistorialEstado
import com.restaurante.restaurantbackend.entity.Mesa
import com.restaurante.restaurantbackend.entity.Pedido
import com.restaurante.restaurantbackend.entity.RolUsuario
import com.restaurante.restaurantbackend.entity.TipoPedido
import com.restaurante.restaurantbackend.entity.Usuario
import com.restaurante.restaurantbackend.mapper.PedidoMapper
import com.restaurante.restaurantbackend.repository.HistorialEstadoRepository
import com.restaurante.restaurantbackend.repository.MesaRepository
import com.restaurante.restaurantbackend.repository.PedidoRepository
import com.restaurante.restaurantbackend.repository.ProductoRepository
import com.restaurante.restaurantbackend.repository.UsuarioRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal
import java.util.UUID

@Service
@Transactional(readOnly = true)
class PedidoServiceImpl(
    private val pedidoRepository: PedidoRepository,
    private val productoRepository: ProductoRepository,
    private val mesaRepository: MesaRepository,
    private val usuarioRepository: UsuarioRepository,
    private val historialEstadoRepository: HistorialEstadoRepository
) : PedidoService {
    @Transactional
    override fun crear(solicitud: CrearPedidoRequest, correoMesero: String): PedidoResponse {
        val mesero = obtenerUsuarioActivo(correoMesero)
        requireRole(mesero, RolUsuario.MESERO)

        val mesa = validarYObtenerMesa(solicitud)
        val pedido = Pedido(
            mesa = mesa,
            mesero = mesero,
            tipo = solicitud.tipo,
            metodoPago = solicitud.metodoPago,
            datosEntrega = solicitud.datosEntrega?.let {
                DatosEntrega(
                    nombreCliente = it.nombreCliente.trim(),
                    telefonoCliente = it.telefonoCliente.trim(),
                    direccion = it.direccion.trim(),
                    barrio = it.barrio?.trim(),
                    referencias = it.referencias?.trim()
                )
            }
        )

        validarDatosEntrega(solicitud, pedido)
        pedido.detalles.addAll(solicitud.detalles.map { detalleSolicitud ->
            val producto = productoRepository.findById(detalleSolicitud.productoId).orElseThrow {
                ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado.")
            }
            if (!producto.disponible || producto.categoria?.activa != true) {
                throw ResponseStatusException(HttpStatus.CONFLICT, "El producto '${producto.nombre}' no está disponible.")
            }

            DetallePedido(
                pedido = pedido,
                producto = producto,
                nombreProducto = producto.nombre,
                precioUnitario = producto.precio,
                cantidad = detalleSolicitud.cantidad,
                notas = detalleSolicitud.notas?.trim()
            )
        })
        pedido.total = pedido.detalles.fold(BigDecimal.ZERO) { total, detalle ->
            total + detalle.precioUnitario.multiply(BigDecimal.valueOf(detalle.cantidad.toLong()))
        }

        val pedidoGuardado = pedidoRepository.save(pedido)
        if (mesa != null) {
            mesa.estado = EstadoMesa.OCUPADA
        }
        registrarHistorial(pedidoGuardado, mesero)
        return PedidoMapper.toResponse(pedidoGuardado)
    }

    @Transactional
    override fun cambiarEstado(
        pedidoId: UUID,
        nuevoEstado: EstadoPedido,
        correoUsuario: String
    ): PedidoResponse {
        val pedido = obtenerPedido(pedidoId)
        val usuario = obtenerUsuarioActivo(correoUsuario)

        when (usuario.rol) {
            RolUsuario.COCINA -> cambiarEstadoDesdeCocina(pedido, nuevoEstado)
            RolUsuario.MESERO -> cambiarEstadoDesdeMesero(pedido, nuevoEstado)
            RolUsuario.ADMINISTRADOR -> throw ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "El administrador no puede cambiar el estado operativo de un pedido."
            )
        }

        pedido.estado = nuevoEstado
        if (nuevoEstado == EstadoPedido.ENTREGADO || nuevoEstado == EstadoPedido.CANCELADO) {
            pedido.mesa?.estado = EstadoMesa.LIBRE
        }
        registrarHistorial(pedido, usuario)
        return PedidoMapper.toResponse(pedido)
    }

    override fun listarColaCocina(): List<PedidoResponse> =
        pedidoRepository.findByEstadoInOrderByFechaCreacionAsc(
            listOf(EstadoPedido.PENDIENTE, EstadoPedido.EN_PREPARACION)
        ).map(PedidoMapper::toResponse)

    override fun listarPedidosMesero(correoMesero: String): List<PedidoResponse> {
        val mesero = obtenerUsuarioActivo(correoMesero)
        requireRole(mesero, RolUsuario.MESERO)
        return pedidoRepository.findByMeseroIdOrderByFechaCreacionDesc(requireNotNull(mesero.id))
            .map(PedidoMapper::toResponse)
    }

    override fun historial(pedidoId: UUID, correoUsuario: String): List<HistorialEstadoResponse> {
        val pedido = obtenerPedido(pedidoId)
        val usuario = obtenerUsuarioActivo(correoUsuario)
        if (usuario.rol == RolUsuario.MESERO && pedido.mesero?.id != usuario.id) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "No puede consultar el historial de otro mesero.")
        }
        return historialEstadoRepository.findByPedidoIdOrderByFechaAsc(pedidoId).map { historial ->
            HistorialEstadoResponse(
                estado = historial.estado,
                fecha = historial.fecha,
                usuarioId = historial.usuario?.id,
                usuarioNombre = historial.usuario?.nombre
            )
        }
    }

    private fun validarYObtenerMesa(solicitud: CrearPedidoRequest): Mesa? =
        when (solicitud.tipo) {
            TipoPedido.SALON -> {
                val mesaId = solicitud.mesaId ?: throw ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Un pedido de salón requiere una mesa."
                )
                val mesa = mesaRepository.findByIdForUpdate(mesaId)
                    ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Mesa no encontrada.")
                if (!mesa.activa || mesa.estado != EstadoMesa.LIBRE) {
                    throw ResponseStatusException(HttpStatus.CONFLICT, "La mesa no está disponible.")
                }
                mesa
            }

            TipoPedido.PARA_LLEVAR,
            TipoPedido.DOMICILIO -> {
                if (solicitud.mesaId != null) {
                    throw ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Este tipo de pedido no debe tener una mesa asignada."
                    )
                }
                null
            }
        }

    private fun validarDatosEntrega(solicitud: CrearPedidoRequest, pedido: Pedido) {
        if (solicitud.tipo == TipoPedido.DOMICILIO &&
            (pedido.datosEntrega == null || solicitud.metodoPago == null)
        ) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Un domicilio requiere datos de entrega y método de pago."
            )
        }
        if (solicitud.tipo != TipoPedido.DOMICILIO && pedido.datosEntrega != null) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Los datos de entrega solo aplican a pedidos a domicilio."
            )
        }
    }

    private fun cambiarEstadoDesdeCocina(pedido: Pedido, nuevoEstado: EstadoPedido) {
        val transicionValida =
            (pedido.estado == EstadoPedido.PENDIENTE && nuevoEstado == EstadoPedido.EN_PREPARACION) ||
                (pedido.estado == EstadoPedido.EN_PREPARACION && nuevoEstado == EstadoPedido.LISTO)
        if (!transicionValida) {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "Cocina solo puede pasar PENDIENTE a EN_PREPARACION o EN_PREPARACION a LISTO."
            )
        }
    }

    private fun cambiarEstadoDesdeMesero(pedido: Pedido, nuevoEstado: EstadoPedido) {
        val transicionValida =
            (pedido.estado == EstadoPedido.LISTO && nuevoEstado == EstadoPedido.ENTREGADO) ||
                (pedido.estado == EstadoPedido.PENDIENTE || pedido.estado == EstadoPedido.EN_PREPARACION) &&
                    nuevoEstado == EstadoPedido.CANCELADO
        if (!transicionValida) {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "Mesero solo puede entregar pedidos LISTOS o cancelar pedidos PENDIENTES o EN_PREPARACION."
            )
        }
    }

    private fun registrarHistorial(pedido: Pedido, usuario: Usuario) {
        historialEstadoRepository.save(
            HistorialEstado(
                pedido = pedido,
                estado = pedido.estado,
                usuario = usuario
            )
        )
    }

    private fun obtenerPedido(id: UUID): Pedido =
        pedidoRepository.findById(id).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado.")
        }

    private fun obtenerUsuarioActivo(correo: String): Usuario =
        usuarioRepository.findByCorreoIgnoreCase(correo).takeIf { it?.activo == true }
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autorizado.")

    private fun requireRole(usuario: Usuario, rol: RolUsuario) {
        if (usuario.rol != rol) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "No tiene el rol requerido para esta operación.")
        }
    }
}
