package com.restaurante.restaurantbackend.service

import com.restaurante.restaurantbackend.dto.CrearPedidoRequest
import com.restaurante.restaurantbackend.dto.DetallePedidoRequest
import com.restaurante.restaurantbackend.entity.Categoria
import com.restaurante.restaurantbackend.entity.EstadoPedido
import com.restaurante.restaurantbackend.entity.Producto
import com.restaurante.restaurantbackend.entity.RolUsuario
import com.restaurante.restaurantbackend.entity.TipoPedido
import com.restaurante.restaurantbackend.entity.Usuario
import com.restaurante.restaurantbackend.repository.CategoriaRepository
import com.restaurante.restaurantbackend.repository.ProductoRepository
import com.restaurante.restaurantbackend.repository.UsuarioRepository
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertTrue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class ReporteJdbcIntegrationTests(
    @Autowired private val reporteService: ReporteService,
    @Autowired private val pedidoService: PedidoService,
    @Autowired private val categoriaRepository: CategoriaRepository,
    @Autowired private val productoRepository: ProductoRepository,
    @Autowired private val usuarioRepository: UsuarioRepository
) {
    @Test
    fun `reportes JDBC consolidan pedidos entregados cancelados y tiempos de preparación`() {
        val suffix = UUID.randomUUID().toString().take(8)
        val categoria = categoriaRepository.save(Categoria(nombre = "Categoría reporte $suffix"))
        val producto = productoRepository.save(
            Producto(
                nombre = "Producto reporte $suffix",
                precio = BigDecimal("17000.00"),
                categoria = categoria
            )
        )
        val mesero = usuarioRepository.save(
            Usuario(
                nombre = "Mesero reporte $suffix",
                correo = "mesero-reporte-$suffix@pruebas.local",
                contrasenaHash = "no-usado",
                rol = RolUsuario.MESERO
            )
        )
        val cocina = usuarioRepository.save(
            Usuario(
                nombre = "Cocina reporte $suffix",
                correo = "cocina-reporte-$suffix@pruebas.local",
                contrasenaHash = "no-usado",
                rol = RolUsuario.COCINA
            )
        )
        val solicitud = CrearPedidoRequest(
            tipo = TipoPedido.PARA_LLEVAR,
            detalles = listOf(DetallePedidoRequest(requireNotNull(producto.id), 1))
        )
        val entregado = pedidoService.crear(solicitud, mesero.correo)
        pedidoService.cambiarEstado(entregado.id, EstadoPedido.EN_PREPARACION, cocina.correo)
        pedidoService.cambiarEstado(entregado.id, EstadoPedido.LISTO, cocina.correo)
        pedidoService.cambiarEstado(entregado.id, EstadoPedido.ENTREGADO, mesero.correo)

        val cancelado = pedidoService.crear(solicitud, mesero.correo)
        pedidoService.cambiarEstado(cancelado.id, EstadoPedido.CANCELADO, mesero.correo)

        val hoy = LocalDate.now()
        val ventas = reporteService.resumenVentas(hoy, hoy)
        val productos = reporteService.productosMasVendidos(hoy, hoy, 100)
        val cancelaciones = reporteService.resumenCancelaciones(hoy, hoy)
        val preparacion = reporteService.tiempoPromedioPreparacion(hoy, hoy)

        assertTrue(ventas.pedidosEntregados >= 1)
        assertTrue(ventas.ventasTotales >= producto.precio)
        assertTrue(productos.any { it.productoId == producto.id && it.unidadesVendidas >= 1 })
        assertTrue(cancelaciones.pedidosCancelados >= 1)
        assertTrue(cancelaciones.valorTotalCancelado >= producto.precio)
        assertTrue(preparacion.pedidosAnalizados >= 1)
    }
}
