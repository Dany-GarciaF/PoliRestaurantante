package com.restaurante.restaurantbackend.service

import com.restaurante.restaurantbackend.dto.ProductoMasVendidoResponse
import com.restaurante.restaurantbackend.dto.ResumenCancelacionesResponse
import com.restaurante.restaurantbackend.dto.ResumenVentasResponse
import com.restaurante.restaurantbackend.dto.TiempoPreparacionResponse
import com.restaurante.restaurantbackend.report.ReporteJdbcRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate

@Service
@Transactional(readOnly = true)
class ReporteService(
    private val reporteJdbcRepository: ReporteJdbcRepository
) {
    fun resumenVentas(fechaDesde: LocalDate, fechaHasta: LocalDate): ResumenVentasResponse {
        validarRango(fechaDesde, fechaHasta)
        return reporteJdbcRepository.resumenVentas(fechaDesde, fechaHasta)
    }

    fun productosMasVendidos(
        fechaDesde: LocalDate,
        fechaHasta: LocalDate,
        limite: Int
    ): List<ProductoMasVendidoResponse> {
        validarRango(fechaDesde, fechaHasta)
        return reporteJdbcRepository.productosMasVendidos(fechaDesde, fechaHasta, limite)
    }

    fun resumenCancelaciones(fechaDesde: LocalDate, fechaHasta: LocalDate): ResumenCancelacionesResponse {
        validarRango(fechaDesde, fechaHasta)
        return reporteJdbcRepository.resumenCancelaciones(fechaDesde, fechaHasta)
    }

    fun tiempoPromedioPreparacion(fechaDesde: LocalDate, fechaHasta: LocalDate): TiempoPreparacionResponse {
        validarRango(fechaDesde, fechaHasta)
        return reporteJdbcRepository.tiempoPromedioPreparacion(fechaDesde, fechaHasta)
    }

    private fun validarRango(fechaDesde: LocalDate, fechaHasta: LocalDate) {
        if (fechaDesde > fechaHasta) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "fechaDesde no puede ser posterior a fechaHasta.")
        }
    }
}
