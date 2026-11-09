package com.restaurante.restaurantbackend.service

import com.restaurante.restaurantbackend.dto.CategoriaResponse
import com.restaurante.restaurantbackend.dto.PaginaResponse
import com.restaurante.restaurantbackend.dto.ProductoResponse
import com.restaurante.restaurantbackend.entity.Categoria
import com.restaurante.restaurantbackend.entity.Producto
import com.restaurante.restaurantbackend.mapper.AdministracionMapper
import com.restaurante.restaurantbackend.repository.CategoriaRepository
import com.restaurante.restaurantbackend.repository.ProductoRepository
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class CatalogoService(
    private val categoriaRepository: CategoriaRepository,
    private val productoRepository: ProductoRepository
) {
    fun listarCategorias(pageable: Pageable): PaginaResponse<CategoriaResponse> =
        categoriaRepository.findByActivaTrueOrderByNombreAsc(pageable).toPagina(AdministracionMapper::toResponse)

    fun listarProductos(
        categoriaId: UUID?,
        busqueda: String?,
        pageable: Pageable
    ): PaginaResponse<ProductoResponse> {
        val textoBusqueda = busqueda?.trim()?.ifBlank { null }
        val specification = Specification<Producto> { root, _, criteriaBuilder ->
            val categoria = root.join<Producto, Categoria>("categoria")
            val predicates = mutableListOf<jakarta.persistence.criteria.Predicate>(
                criteriaBuilder.isTrue(root.get("disponible")),
                criteriaBuilder.isTrue(categoria.get("activa"))
            )
            categoriaId?.let { predicates += criteriaBuilder.equal(categoria.get<java.util.UUID>("id"), it) }
            textoBusqueda?.let { texto ->
                predicates += criteriaBuilder.like(
                    criteriaBuilder.lower(root.get<String>("nombre")),
                    "%${texto.lowercase()}%"
                )
            }
            criteriaBuilder.and(*predicates.toTypedArray())
        }
        return productoRepository.findAll(specification, pageable).toPagina(AdministracionMapper::toResponse)
    }

    private fun <T : Any, R> org.springframework.data.domain.Page<T>.toPagina(transform: (T) -> R): PaginaResponse<R> =
        PaginaResponse(
            contenido = content.map(transform),
            pagina = number,
            tamano = size,
            totalElementos = totalElements,
            totalPaginas = totalPages
        )
}
