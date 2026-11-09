package com.restaurante.restaurantbackend.service

import com.restaurante.restaurantbackend.entity.Categoria
import com.restaurante.restaurantbackend.entity.Producto
import com.restaurante.restaurantbackend.repository.CategoriaRepository
import com.restaurante.restaurantbackend.repository.ProductoRepository
import java.math.BigDecimal
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest

@SpringBootTest
class CatalogoServiceIntegrationTests(
    @Autowired private val catalogoService: CatalogoService,
    @Autowired private val categoriaRepository: CategoriaRepository,
    @Autowired private val productoRepository: ProductoRepository
) {
    @Test
    fun `lista y filtra productos disponibles sin enviar búsqueda nula a SQL`() {
        val suffix = UUID.randomUUID().toString().take(8)
        val categoria = categoriaRepository.save(Categoria(nombre = "Categoría catálogo $suffix"))
        val productoVisible = productoRepository.save(
            Producto(
                nombre = "Hamburguesa catálogo $suffix",
                precio = BigDecimal("15000.00"),
                categoria = categoria
            )
        )
        productoRepository.save(
            Producto(
                nombre = "Producto agotado $suffix",
                precio = BigDecimal("10000.00"),
                disponible = false,
                categoria = categoria
            )
        )

        val sinBusqueda = catalogoService.listarProductos(null, null, PageRequest.of(0, 100))
        val porCategoria = catalogoService.listarProductos(categoria.id, null, PageRequest.of(0, 100))
        val porTexto = catalogoService.listarProductos(null, "HAMBURGUESA", PageRequest.of(0, 100))

        assertTrue(sinBusqueda.contenido.any { it.id == productoVisible.id })
        assertEquals(listOf(productoVisible.id), porCategoria.contenido.map { it.id })
        assertEquals(listOf(productoVisible.id), porTexto.contenido.map { it.id })
    }
}
