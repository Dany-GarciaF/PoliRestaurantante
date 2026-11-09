package com.restaurante.restaurantbackend.controller

import com.restaurante.restaurantbackend.dto.ProductoMasVendidoResponse
import com.restaurante.restaurantbackend.dto.ResumenCancelacionesResponse
import com.restaurante.restaurantbackend.dto.ResumenVentasResponse
import com.restaurante.restaurantbackend.dto.TiempoPreparacionResponse
import com.restaurante.restaurantbackend.service.ReporteService
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@Validated
@RequestMapping("/api/admin/reportes")
@PreAuthorize("hasRole('ADMINISTRADOR')")
class ReporteController(
    private val reporteService: ReporteService
) {
    @GetMapping("/ventas")
    fun resumenVentas(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) fechaDesde: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) fechaHasta: LocalDate
    ): ResumenVentasResponse = reporteService.resumenVentas(fechaDesde, fechaHasta)

    @GetMapping("/productos-mas-vendidos")
    fun productosMasVendidos(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) fechaDesde: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) fechaHasta: LocalDate,
        @RequestParam(defaultValue = "10") @Min(1) @Max(100) limite: Int
    ): List<ProductoMasVendidoResponse> =
        reporteService.productosMasVendidos(fechaDesde, fechaHasta, limite)

    @GetMapping("/cancelaciones")
    fun resumenCancelaciones(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) fechaDesde: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) fechaHasta: LocalDate
    ): ResumenCancelacionesResponse = reporteService.resumenCancelaciones(fechaDesde, fechaHasta)

    @GetMapping("/tiempo-preparacion")
    fun tiempoPromedioPreparacion(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) fechaDesde: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) fechaHasta: LocalDate
    ): TiempoPreparacionResponse = reporteService.tiempoPromedioPreparacion(fechaDesde, fechaHasta)
}
