package com.restaurante.restaurantbackend.service

import com.restaurante.restaurantbackend.dto.CrearPedidoRequest
import com.restaurante.restaurantbackend.dto.DetallePedidoRequest
import com.restaurante.restaurantbackend.entity.Categoria
import com.restaurante.restaurantbackend.entity.EstadoMesa
import com.restaurante.restaurantbackend.entity.EstadoPedido
import com.restaurante.restaurantbackend.entity.Mesa
import com.restaurante.restaurantbackend.entity.Producto
import com.restaurante.restaurantbackend.entity.RolUsuario
import com.restaurante.restaurantbackend.entity.TipoPedido
import com.restaurante.restaurantbackend.entity.Usuario
import com.restaurante.restaurantbackend.repository.CategoriaRepository
import com.restaurante.restaurantbackend.repository.MesaRepository
import com.restaurante.restaurantbackend.repository.ProductoRepository
import com.restaurante.restaurantbackend.repository.UsuarioRepository
import java.math.BigDecimal
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class PedidoServiceIntegrationTests(
    @Autowired private val pedidoService: PedidoService,
    @Autowired private val categoriaRepository: CategoriaRepository,
    @Autowired private val productoRepository: ProductoRepository,
    @Autowired private val mesaRepository: MesaRepository,
    @Autowired private val usuarioRepository: UsuarioRepository
) {
    @Test
    fun `cocina prepara y mesero entrega un pedido liberando la mesa`() {
        val suffix = UUID.randomUUID().toString().take(8)
        val categoria = categoriaRepository.save(Categoria(nombre = "Categoría $suffix"))
        val producto = productoRepository.save(
            Producto(
                nombre = "Producto $suffix",
                precio = BigDecimal("12500.00"),
                categoria = categoria
            )
        )
        val mesa = mesaRepository.save(Mesa(numero = (100000..999999).random()))
        val mesero = usuarioRepository.save(
            Usuario(
                nombre = "Mesero $suffix",
                correo = "mesero-$suffix@pruebas.local",
                contrasenaHash = "no-usado",
                rol = RolUsuario.MESERO
            )
        )
        val cocina = usuarioRepository.save(
            Usuario(
                nombre = "Cocina $suffix",
                correo = "cocina-$suffix@pruebas.local",
                contrasenaHash = "no-usado",
                rol = RolUsuario.COCINA
            )
        )

        val pedido = pedidoService.crear(
            CrearPedidoRequest(
                tipo = TipoPedido.SALON,
                mesaId = mesa.id,
                detalles = listOf(DetallePedidoRequest(productoId = requireNotNull(producto.id), cantidad = 2))
            ),
            mesero.correo
        )

        assertEquals(EstadoPedido.PENDIENTE, pedido.estado)
        assertEquals(BigDecimal("25000.00"), pedido.total)
        assertEquals(EstadoMesa.OCUPADA, mesaRepository.findById(requireNotNull(mesa.id)).orElseThrow().estado)
        assertEquals(1, pedidoService.historial(pedido.id, mesero.correo).size)

        pedidoService.cambiarEstado(pedido.id, EstadoPedido.EN_PREPARACION, cocina.correo)
        pedidoService.cambiarEstado(pedido.id, EstadoPedido.LISTO, cocina.correo)
        val entregado = pedidoService.cambiarEstado(pedido.id, EstadoPedido.ENTREGADO, mesero.correo)

        assertEquals(EstadoPedido.ENTREGADO, entregado.estado)
        assertEquals(4, pedidoService.historial(pedido.id, mesero.correo).size)
        assertEquals(EstadoMesa.LIBRE, mesaRepository.findById(requireNotNull(mesa.id)).orElseThrow().estado)
    }
}
