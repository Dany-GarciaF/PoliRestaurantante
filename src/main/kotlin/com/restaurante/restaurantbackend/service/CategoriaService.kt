package com.restaurante.restaurantbackend.service

import com.restaurante.restaurantbackend.dto.CategoriaRequest
import com.restaurante.restaurantbackend.dto.CategoriaResponse
import com.restaurante.restaurantbackend.entity.Categoria
import com.restaurante.restaurantbackend.mapper.AdministracionMapper
import com.restaurante.restaurantbackend.repository.CategoriaRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@Service
@Transactional(readOnly = true)
class CategoriaService(
    private val categoriaRepository: CategoriaRepository
) {
    fun listar(): List<CategoriaResponse> =
        categoriaRepository.findAll().map(AdministracionMapper::toResponse)

    @Transactional
    fun crear(solicitud: CategoriaRequest): CategoriaResponse {
        val nombre = solicitud.nombre.trim()
        if (categoriaRepository.existsByNombreIgnoreCase(nombre)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una categoría con ese nombre.")
        }
        return AdministracionMapper.toResponse(
            categoriaRepository.save(
                Categoria(
                    nombre = nombre,
                    descripcion = solicitud.descripcion?.trim(),
                    activa = solicitud.activa
                )
            )
        )
    }

    @Transactional
    fun actualizar(id: UUID, solicitud: CategoriaRequest): CategoriaResponse {
        val categoria = obtenerEntidad(id)
        val nombre = solicitud.nombre.trim()
        if (!categoria.nombre.equals(nombre, ignoreCase = true) &&
            categoriaRepository.existsByNombreIgnoreCase(nombre)
        ) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una categoría con ese nombre.")
        }
        categoria.nombre = nombre
        categoria.descripcion = solicitud.descripcion?.trim()
        categoria.activa = solicitud.activa
        return AdministracionMapper.toResponse(categoria)
    }

    @Transactional
    fun desactivar(id: UUID) {
        obtenerEntidad(id).activa = false
    }

    fun obtenerEntidad(id: UUID): Categoria =
        categoriaRepository.findById(id).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Categoría no encontrada.")
        }
}
