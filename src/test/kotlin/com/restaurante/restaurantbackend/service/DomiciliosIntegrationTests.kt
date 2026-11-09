package com.restaurante.restaurantbackend.service

import com.restaurante.restaurantbackend.dto.ActualizarConfiguracionDomiciliosRequest
import com.restaurante.restaurantbackend.dto.CrearPedidoRequest
import com.restaurante.restaurantbackend.dto.DatosEntregaRequest
import com.restaurante.restaurantbackend.dto.DetallePedidoRequest
import com.restaurante.restaurantbackend.entity.Categoria
import com.restaurante.restaurantbackend.entity.EstadoPago
import com.restaurante.restaurantbackend.entity.MetodoPago
import com.restaurante.restaurantbackend.entity.Producto
import com.restaurante.restaurantbackend.entity.RolUsuario
import com.restaurante.restaurantbackend.entity.TipoPedido
import com.restaurante.restaurantbackend.entity.Usuario
import com.restaurante.restaurantbackend.repository.CategoriaRepository
import com.restaurante.restaurantbackend.repository.ProductoRepository
import com.restaurante.restaurantbackend.repository.UsuarioRepository
import java.math.BigDecimal
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.web.server.ResponseStatusException

@SpringBootTest
class DomiciliosIntegrationTests(
    @Autowired private val pedidoService: PedidoService,
    @Autowired private val configuracionDomiciliosService: ConfiguracionDomiciliosService,
    @Autowired private val categoriaRepository: CategoriaRepository,
    @Autowired private val productoRepository: ProductoRepository,
    @Autowired private val usuarioRepository: UsuarioRepository
) {
    @Test
    fun `domicilio sobre el límite requiere pago virtual`() {
        val suffix = UUID.randomUUID().toString().take(8)
        configuracionDomiciliosService.actualizar(
            ActualizarConfiguracionDomiciliosRequest(BigDecimal("50000.00"))
        )
        val categoria = categoriaRepository.save(Categoria(nombre = "Categoría domicilio $suffix"))
        val producto = productoRepository.save(
            Producto(
                nombre = "Producto domicilio $suffix",
                precio = BigDecimal("51000.00"),
                categoria = categoria
            )
        )
        val mesero = usuarioRepository.save(
            Usuario(
                nombre = "Mesero domicilio $suffix",
                correo = "mesero-domicilio-$suffix@pruebas.local",
                contrasenaHash = "no-usado",
                rol = RolUsuario.MESERO
            )
        )
        val detalle = DetallePedidoRequest(requireNotNull(producto.id), 1)
        val entrega = DatosEntregaRequest(
            nombreCliente = "Cliente Domicilio",
            telefonoCliente = "+57 300 123 4567",
            direccion = "Calle 10 # 20-30"
        )

        val exception = assertFailsWith<ResponseStatusException> {
            pedidoService.crear(
                CrearPedidoRequest(
                    tipo = TipoPedido.DOMICILIO,
                    metodoPago = MetodoPago.EFECTIVO_CONTRAENTREGA,
                    datosEntrega = entrega,
                    detalles = listOf(detalle)
                ),
                mesero.correo
            )
        }
        assertEquals(409, exception.statusCode.value())

        val pedidoVirtual = pedidoService.crear(
            CrearPedidoRequest(
                tipo = TipoPedido.DOMICILIO,
                metodoPago = MetodoPago.PAGO_VIRTUAL,
                datosEntrega = entrega,
                detalles = listOf(detalle)
            ),
            mesero.correo
        )

        assertEquals(MetodoPago.PAGO_VIRTUAL, pedidoVirtual.metodoPago)
        assertEquals(EstadoPago.PENDIENTE, pedidoVirtual.estadoPago)
        assertEquals(BigDecimal("51000.00"), pedidoVirtual.total)
    }
}
