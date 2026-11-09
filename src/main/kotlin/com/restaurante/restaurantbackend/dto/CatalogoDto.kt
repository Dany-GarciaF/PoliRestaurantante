package com.restaurante.restaurantbackend.dto

data class PaginaResponse<T>(
    val contenido: List<T>,
    val pagina: Int,
    val tamano: Int,
    val totalElementos: Long,
    val totalPaginas: Int
)
