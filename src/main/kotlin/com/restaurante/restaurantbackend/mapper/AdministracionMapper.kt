package com.restaurante.restaurantbackend.mapper

import com.restaurante.restaurantbackend.dto.CategoriaResponse
import com.restaurante.restaurantbackend.dto.MesaResponse
import com.restaurante.restaurantbackend.dto.ProductoResponse
import com.restaurante.restaurantbackend.entity.Categoria
import com.restaurante.restaurantbackend.entity.Mesa
import com.restaurante.restaurantbackend.entity.Producto

object AdministracionMapper {
    fun toResponse(categoria: Categoria): CategoriaResponse =
        CategoriaResponse(
            id = requireNotNull(categoria.id),
            nombre = categoria.nombre,
            descripcion = categoria.descripcion,
            activa = categoria.activa
        )

    fun toResponse(producto: Producto): ProductoResponse =
        ProductoResponse(
            id = requireNotNull(producto.id),
            nombre = producto.nombre,
            descripcion = producto.descripcion,
            precio = producto.precio,
            disponible = producto.disponible,
            categoria = toResponse(requireNotNull(producto.categoria))
        )

    fun toResponse(mesa: Mesa): MesaResponse =
        MesaResponse(
            id = requireNotNull(mesa.id),
            numero = mesa.numero,
            estado = mesa.estado,
            activa = mesa.activa
        )
}
