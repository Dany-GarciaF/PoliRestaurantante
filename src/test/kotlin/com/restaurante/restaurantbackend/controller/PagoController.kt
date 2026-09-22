package com.restaurante.restaurantbackend.controller

import com.restaurante.restaurantbackend.entity.Pago
import com.restaurante.restaurantbackend.service.PagoService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/pagos")
class PagoController(
    private val pagoService: PagoService
) {

    @PostMapping
    fun registrar(@RequestBody pago: Pago): ResponseEntity<Pago> =
        ResponseEntity.ok(pagoService.registrarPago(pago))

    @GetMapping("/{id}")
    fun obtener(@PathVariable id: Long): ResponseEntity<Pago> =
        ResponseEntity.ok(pagoService.obtenerPago(id))

    @GetMapping("/domicilio/{idDomicilio}")
    fun porDomicilio(@PathVariable idDomicilio: Long): ResponseEntity<List<Pago>> =
        ResponseEntity.ok(pagoService.obtenerPagoPorDomicilio(idDomicilio))

    @GetMapping("/pedido/{idPedido}")
    fun porPedido(@PathVariable idPedido: Long): ResponseEntity<List<Pago>> =
        ResponseEntity.ok(pagoService.obtenerPagoPorPedido(idPedido))
}