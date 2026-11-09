package com.restaurante.restaurantbackend.controller

import com.restaurante.restaurantbackend.dto.CategoriaResponse
import com.restaurante.restaurantbackend.dto.PaginaResponse
import com.restaurante.restaurantbackend.dto.ProductoResponse
import com.restaurante.restaurantbackend.service.CatalogoService
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/menu")
@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'MESERO', 'COCINA')")
class CatalogoController(
    private val catalogoService: CatalogoService
) {
    @GetMapping("/categorias")
    fun listarCategorias(
        @RequestParam(defaultValue = "0") @Min(0) pagina: Int,
        @RequestParam(defaultValue = "20") @Min(1) @Max(MAXIMUM_PAGE_SIZE.toLong()) tamano: Int
    ): PaginaResponse<CategoriaResponse> =
        catalogoService.listarCategorias(crearPaginacion(pagina, tamano))

    @GetMapping("/productos")
    fun listarProductos(
        @RequestParam(required = false) categoriaId: UUID?,
        @RequestParam(required = false) busqueda: String?,
        @RequestParam(defaultValue = "0") @Min(0) pagina: Int,
        @RequestParam(defaultValue = "20") @Min(1) @Max(MAXIMUM_PAGE_SIZE.toLong()) tamano: Int
    ): PaginaResponse<ProductoResponse> =
        catalogoService.listarProductos(categoriaId, busqueda, crearPaginacion(pagina, tamano))

    private fun crearPaginacion(pagina: Int, tamano: Int): PageRequest =
        PageRequest.of(
            pagina,
            tamano,
            Sort.by(Sort.Direction.ASC, "nombre")
        )

    companion object {
        private const val MAXIMUM_PAGE_SIZE = 100
    }
}
