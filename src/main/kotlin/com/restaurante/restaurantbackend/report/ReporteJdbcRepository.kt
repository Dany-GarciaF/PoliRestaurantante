package com.restaurante.restaurantbackend.report

import com.restaurante.restaurantbackend.dto.ProductoMasVendidoResponse
import com.restaurante.restaurantbackend.dto.ResumenCancelacionesResponse
import com.restaurante.restaurantbackend.dto.ResumenVentasResponse
import com.restaurante.restaurantbackend.dto.TiempoPreparacionResponse
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.sql.Timestamp
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.Duration
import java.util.UUID

@Repository
class ReporteJdbcRepository(
    private val jdbcTemplate: JdbcTemplate
) {
    fun resumenVentas(fechaDesde: LocalDate, fechaHasta: LocalDate): ResumenVentasResponse {
        val sql =
            """
            SELECT
                COUNT(*) AS pedidos_entregados,
                COALESCE(SUM(total), 0) AS ventas_totales,
                COALESCE(AVG(total), 0) AS ticket_promedio
            FROM pedidos
            WHERE estado = 'ENTREGADO'
              AND fecha_creacion >= ?
              AND fecha_creacion < ?
            """.trimIndent()

        return jdbcTemplate.queryForObject(sql, { resultSet, _ ->
            ResumenVentasResponse(
                fechaDesde = fechaDesde,
                fechaHasta = fechaHasta,
                pedidosEntregados = resultSet.getLong("pedidos_entregados"),
                ventasTotales = resultSet.getBigDecimal("ventas_totales"),
                ticketPromedio = resultSet.getBigDecimal("ticket_promedio")
            )
        }, inicio(fechaDesde), finExclusivo(fechaHasta))
    }

    fun productosMasVendidos(
        fechaDesde: LocalDate,
        fechaHasta: LocalDate,
        limite: Int
    ): List<ProductoMasVendidoResponse> {
        val sql =
            """
            SELECT
                detalle.producto_id,
                detalle.nombre_producto,
                SUM(detalle.cantidad) AS unidades_vendidas,
                SUM(detalle.precio_unitario * detalle.cantidad) AS ingresos_generados
            FROM detalles_pedido detalle
            INNER JOIN pedidos pedido ON pedido.id = detalle.pedido_id
            WHERE pedido.estado = 'ENTREGADO'
              AND pedido.fecha_creacion >= ?
              AND pedido.fecha_creacion < ?
            GROUP BY detalle.producto_id, detalle.nombre_producto
            ORDER BY unidades_vendidas DESC, ingresos_generados DESC, detalle.nombre_producto ASC
            LIMIT ?
            """.trimIndent()

        return jdbcTemplate.query(sql, { resultSet, _ ->
            ProductoMasVendidoResponse(
                productoId = resultSet.getObject("producto_id", UUID::class.java),
                nombreProducto = resultSet.getString("nombre_producto"),
                unidadesVendidas = resultSet.getLong("unidades_vendidas"),
                ingresosGenerados = resultSet.getBigDecimal("ingresos_generados")
            )
        }, inicio(fechaDesde), finExclusivo(fechaHasta), limite)
    }

    fun resumenCancelaciones(fechaDesde: LocalDate, fechaHasta: LocalDate): ResumenCancelacionesResponse {
        val sql =
            """
            SELECT
                COUNT(*) AS pedidos_cancelados,
                COALESCE(SUM(total), 0) AS valor_total_cancelado
            FROM pedidos
            WHERE estado = 'CANCELADO'
              AND fecha_creacion >= ?
              AND fecha_creacion < ?
            """.trimIndent()

        return jdbcTemplate.queryForObject(sql, { resultSet, _ ->
            ResumenCancelacionesResponse(
                fechaDesde = fechaDesde,
                fechaHasta = fechaHasta,
                pedidosCancelados = resultSet.getLong("pedidos_cancelados"),
                valorTotalCancelado = resultSet.getBigDecimal("valor_total_cancelado")
            )
        }, inicio(fechaDesde), finExclusivo(fechaHasta))
    }

    fun tiempoPromedioPreparacion(fechaDesde: LocalDate, fechaHasta: LocalDate): TiempoPreparacionResponse {
        val sql =
            """
            WITH etapas AS (
                SELECT
                    historial.pedido_id,
                    MIN(CASE WHEN historial.estado = 'EN_PREPARACION' THEN historial.fecha END) AS inicio_preparacion,
                    MIN(CASE WHEN historial.estado = 'LISTO' THEN historial.fecha END) AS fin_preparacion
                FROM historial_estados historial
                INNER JOIN pedidos pedido ON pedido.id = historial.pedido_id
                WHERE pedido.fecha_creacion >= ?
                  AND pedido.fecha_creacion < ?
                GROUP BY historial.pedido_id
            )
            SELECT inicio_preparacion, fin_preparacion
            FROM etapas
            WHERE inicio_preparacion IS NOT NULL
              AND fin_preparacion IS NOT NULL
            """.trimIndent()

        val duraciones = jdbcTemplate.query(sql, { resultSet, _ ->
            Duration.between(
                resultSet.getTimestamp("inicio_preparacion").toInstant(),
                resultSet.getTimestamp("fin_preparacion").toInstant()
            ).seconds
        }, inicio(fechaDesde), finExclusivo(fechaHasta))
        return TiempoPreparacionResponse(
            pedidosAnalizados = duraciones.size.toLong(),
            tiempoPromedioSegundos = duraciones.takeIf { it.isNotEmpty() }?.average()?.toLong()
        )
    }

    private fun inicio(fecha: LocalDate): Timestamp =
        Timestamp.from(fecha.atStartOfDay().toInstant(ZoneOffset.UTC))

    private fun finExclusivo(fecha: LocalDate): Timestamp =
        Timestamp.from(fecha.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC))
}
