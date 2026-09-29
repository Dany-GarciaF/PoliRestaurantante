package com.restaurante.restaurantbackend.service

import com.restaurante.restaurantbackend.dto.ProductoRequest
import com.restaurante.restaurantbackend.dto.ProductoResponse
import com.restaurante.restaurantbackend.entity.Producto
import com.restaurante.restaurantbackend.mapper.AdministracionMapper
import com.restaurante.restaurantbackend.repository.ProductoRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@Service
@Transactional(readOnly = true)
class ProductoService(
    private val productoRepository: ProductoRepository,
    private val categoriaService: CategoriaService
) {
    fun listar(): List<ProductoResponse> =
        productoRepository.findAll().map(AdministracionMapper::toResponse)

    @Transactional
    fun crear(solicitud: ProductoRequest): ProductoResponse {
        verificarNombreDisponible(solicitud.nombre, null)
        val producto = Producto()
        actualizarCampos(producto, solicitud)
        return AdministracionMapper.toResponse(productoRepository.save(producto))
    }

    @Transactional
    fun actualizar(id: UUID, solicitud: ProductoRequest): ProductoResponse {
        val producto = obtenerEntidad(id)
        verificarNombreDisponible(solicitud.nombre, producto)
        actualizarCampos(producto, solicitud)
        return AdministracionMapper.toResponse(producto)
    }

    @Transactional
    fun desactivar(id: UUID) {
        obtenerEntidad(id).disponible = false
    }

    private fun actualizarCampos(producto: Producto, solicitud: ProductoRequest) {
        producto.nombre = solicitud.nombre.trim()
        producto.descripcion = solicitud.descripcion?.trim()
        producto.precio = solicitud.precio
        producto.disponible = solicitud.disponible
        producto.categoria = categoriaService.obtenerEntidad(solicitud.categoriaId)
    }

    private fun verificarNombreDisponible(nombre: String, productoActual: Producto?) {
        if ((productoActual == null || !productoActual.nombre.equals(nombre.trim(), ignoreCase = true)) &&
            productoRepository.existsByNombreIgnoreCase(nombre.trim())
        ) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un producto con ese nombre.")
        }
    }

    private fun obtenerEntidad(id: UUID): Producto =
        productoRepository.findById(id).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado.")
        }
}
