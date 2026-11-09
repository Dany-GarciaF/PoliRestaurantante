package com.restaurante.restaurantbackend.service

import com.restaurante.restaurantbackend.dto.CrearPedidoRequest
import com.restaurante.restaurantbackend.dto.DetallePedidoRequest
import com.restaurante.restaurantbackend.dto.FiltroPedidoAdministrativo
import com.restaurante.restaurantbackend.entity.Categoria
import com.restaurante.restaurantbackend.entity.EstadoMesa
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
import kotlin.test.assertTrue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest

@SpringBootTest
class AdministracionConsultasIntegrationTests(
    @Autowired private val pedidoService: PedidoService,
    @Autowired private val mesaService: MesaService,
    @Autowired private val categoriaRepository: CategoriaRepository,
    @Autowired private val productoRepository: ProductoRepository,
    @Autowired private val mesaRepository: MesaRepository,
    @Autowired private val usuarioRepository: UsuarioRepository
) {
    @Test
    fun `administracion filtra pedidos y mesero solo ve mesas activas libres`() {
        val suffix = UUID.randomUUID().toString().take(8)
        val categoria = categoriaRepository.save(Categoria(nombre = "Categoría consulta $suffix"))
        val producto = productoRepository.save(
            Producto(
                nombre = "Producto consulta $suffix",
                precio = BigDecimal("12000.00"),
                categoria = categoria
            )
        )
        val mesero = usuarioRepository.save(
            Usuario(
                nombre = "Mesero consulta $suffix",
                correo = "mesero-consulta-$suffix@pruebas.local",
                contrasenaHash = "no-usado",
                rol = RolUsuario.MESERO
            )
        )
        val mesaLibre = mesaRepository.save(Mesa(numero = (100000..999999).random()))
        val mesaOcupada = mesaRepository.save(
            Mesa(numero = (100000..999999).random(), estado = EstadoMesa.OCUPADA)
        )
        val mesaInactiva = mesaRepository.save(
            Mesa(numero = (100000..999999).random(), activa = false)
        )

        val pedido = pedidoService.crear(
            CrearPedidoRequest(
                tipo = TipoPedido.PARA_LLEVAR,
                detalles = listOf(DetallePedidoRequest(requireNotNull(producto.id), 1))
            ),
            mesero.correo
        )

        val resultados = pedidoService.listarAdministracion(
            FiltroPedidoAdministrativo(meseroId = mesero.id),
            PageRequest.of(0, 20)
        )
        val mesasDisponibles = mesaService.listarDisponibles()

        assertEquals(listOf(pedido.id), resultados.contenido.map { it.id })
        assertTrue(mesasDisponibles.any { it.id == mesaLibre.id })
        assertTrue(mesasDisponibles.none { it.id == mesaOcupada.id })
        assertTrue(mesasDisponibles.none { it.id == mesaInactiva.id })
    }
}
