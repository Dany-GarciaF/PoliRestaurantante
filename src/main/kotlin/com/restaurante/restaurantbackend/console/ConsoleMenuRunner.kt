package com.restaurante.restaurantbackend.console

import com.restaurante.restaurantbackend.dto.CambiarEstadoUsuarioRequest
import com.restaurante.restaurantbackend.dto.ActualizarDetallePedidoRequest
import com.restaurante.restaurantbackend.dto.AgregarDetallePedidoRequest
import com.restaurante.restaurantbackend.dto.CategoriaRequest
import com.restaurante.restaurantbackend.dto.CrearPedidoRequest
import com.restaurante.restaurantbackend.dto.CrearUsuarioRequest
import com.restaurante.restaurantbackend.dto.DetallePedidoRequest
import com.restaurante.restaurantbackend.dto.LoginRequest
import com.restaurante.restaurantbackend.dto.MesaRequest
import com.restaurante.restaurantbackend.dto.ProductoRequest
import com.restaurante.restaurantbackend.entity.EstadoPedido
import com.restaurante.restaurantbackend.entity.RolUsuario
import com.restaurante.restaurantbackend.entity.TipoPedido
import com.restaurante.restaurantbackend.repository.UsuarioRepository
import com.restaurante.restaurantbackend.service.AuthService
import com.restaurante.restaurantbackend.service.CatalogoService
import com.restaurante.restaurantbackend.service.CategoriaService
import com.restaurante.restaurantbackend.service.MesaService
import com.restaurante.restaurantbackend.service.PedidoService
import com.restaurante.restaurantbackend.service.ProductoService
import com.restaurante.restaurantbackend.service.UsuarioService
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal
import java.util.Scanner
import java.util.UUID

@Profile("console")
@Component
class ConsoleMenuRunner(
    private val authService: AuthService,
    private val catalogoService: CatalogoService,
    private val usuarioRepository: UsuarioRepository,
    private val usuarioService: UsuarioService,
    private val categoriaService: CategoriaService,
    private val productoService: ProductoService,
    private val mesaService: MesaService,
    private val pedidoService: PedidoService
) : ApplicationRunner {
    private val scanner = Scanner(System.`in`)

    override fun run(args: ApplicationArguments) {
        println("\n=== POLIRESTAURANTE - CONSOLA ===")
        println("Modo consola activo. La API REST no se inicia en este perfil.")
        try {
            while (true) {
                val usuario = iniciarSesion() ?: break
                ejecutarMenuPorRol(usuario.correo, usuario.rol)
            }
        } catch (_: ConsoleInputClosedException) {
            println("\nEntrada de consola cerrada.")
        }
        println("Sesion de consola finalizada.")
    }

    private fun iniciarSesion(): Sesion? {
        while (true) {
            println("\n1. Iniciar sesion")
            println("0. Salir")
            when (leerOpcion("Opcion: ", setOf("0", "1"))) {
                "0" -> return null
                "1" -> try {
                    val correo = leerRequerido("Correo: ")
                    val contrasena = leerRequerido("Contrasena: ")
                    authService.iniciarSesion(LoginRequest(correo, contrasena))
                    val usuario = requireNotNull(usuarioRepository.findByCorreoIgnoreCase(correo.trim()))
                    println("Bienvenido, ${usuario.nombre}. Rol: ${usuario.rol}.")
                    return Sesion(usuario.correo, usuario.rol)
                } catch (exception: ResponseStatusException) {
                    println("Error ${exception.statusCode.value()}: ${exception.reason}")
                } catch (_: Exception) {
                    println("No fue posible iniciar sesion.")
                }
            }
        }
    }

    private fun ejecutarMenuPorRol(correo: String, rol: RolUsuario) {
        when (rol) {
            RolUsuario.ADMINISTRADOR -> menuAdministrador()
            RolUsuario.MESERO -> menuMesero(correo)
            RolUsuario.COCINA -> menuCocina(correo)
        }
    }

    private fun menuAdministrador() {
        while (true) {
            println(
                """
                
                === MENU ADMINISTRADOR ===
                1. Gestionar usuarios
                2. Gestionar categorias
                3. Gestionar productos
                4. Gestionar mesas
                0. Cerrar sesion
                """.trimIndent()
            )
            when (leerOpcion("Opcion: ", setOf("0", "1", "2", "3", "4"))) {
                "1" -> menuUsuarios()
                "2" -> menuCategorias()
                "3" -> menuProductos()
                "4" -> menuMesas()
                "0" -> return
            }
        }
    }

    private fun menuUsuarios() {
        while (true) {
            println("\n--- USUARIOS ---")
            println("1. Crear usuario")
            println("2. Listar usuarios")
            println("3. Activar o desactivar usuario")
            println("0. Volver")
            when (leerOpcion("Opcion: ", setOf("0", "1", "2", "3"))) {
                "1" -> ejecutar {
                    val usuario = usuarioService.crear(
                        CrearUsuarioRequest(
                            nombre = leerRequerido("Nombre: "),
                            correo = leerRequerido("Correo: "),
                            contrasena = leerRequerido("Contrasena: "),
                            rol = leerEnum("Rol (ADMINISTRADOR, MESERO, COCINA): ")
                        )
                    )
                    println("Usuario creado: ${usuario.id}")
                }

                "2" -> usuarioRepository.findAll().forEach {
                    println("${it.id} | ${it.nombre} | ${it.correo} | ${it.rol} | activo=${it.activo}")
                }

                "3" -> ejecutar {
                    val id = leerUuid("ID del usuario: ")
                    val activo = leerSiNo("Activar usuario")
                    val usuario = usuarioService.cambiarEstado(id, CambiarEstadoUsuarioRequest(activo))
                    println("Usuario ${usuario.nombre}: activo=${usuario.activo}")
                }

                "0" -> return
            }
        }
    }

    private fun menuCategorias() {
        while (true) {
            println("\n--- CATEGORIAS ---")
            println("1. Listar categorias")
            println("2. Crear categoria")
            println("3. Actualizar categoria")
            println("4. Desactivar categoria")
            println("0. Volver")
            when (leerOpcion("Opcion: ", setOf("0", "1", "2", "3", "4"))) {
                "1" -> categoriaService.listar().forEach {
                    println("${it.id} | ${it.nombre} | activa=${it.activa} | ${it.descripcion.orEmpty()}")
                }

                "2" -> ejecutar {
                    val categoria = categoriaService.crear(
                        CategoriaRequest(
                            nombre = leerRequerido("Nombre: "),
                            descripcion = leerOpcional("Descripcion: "),
                            activa = leerSiNo("Activa")
                        )
                    )
                    println("Categoria creada: ${categoria.id}")
                }

                "3" -> ejecutar {
                    val id = leerUuid("ID de la categoria: ")
                    val categoria = categoriaService.actualizar(
                        id,
                        CategoriaRequest(
                            nombre = leerRequerido("Nombre: "),
                            descripcion = leerOpcional("Descripcion: "),
                            activa = leerSiNo("Activa")
                        )
                    )
                    println("Categoria actualizada: ${categoria.nombre}")
                }

                "4" -> ejecutar {
                    categoriaService.desactivar(leerUuid("ID de la categoria: "))
                    println("Categoria desactivada.")
                }

                "0" -> return
            }
        }
    }

    private fun menuProductos() {
        while (true) {
            println("\n--- PRODUCTOS ---")
            println("1. Listar productos")
            println("2. Crear producto")
            println("3. Actualizar producto")
            println("4. Desactivar producto")
            println("0. Volver")
            when (leerOpcion("Opcion: ", setOf("0", "1", "2", "3", "4"))) {
                "1" -> productoService.listar().forEach {
                    println("${it.id} | ${it.nombre} | $${it.precio} | ${it.categoria.nombre} | disponible=${it.disponible}")
                }

                "2" -> ejecutar {
                    val producto = productoService.crear(leerProductoRequest())
                    println("Producto creado: ${producto.id}")
                }

                "3" -> ejecutar {
                    val producto = productoService.actualizar(leerUuid("ID del producto: "), leerProductoRequest())
                    println("Producto actualizado: ${producto.nombre}")
                }

                "4" -> ejecutar {
                    productoService.desactivar(leerUuid("ID del producto: "))
                    println("Producto desactivado.")
                }

                "0" -> return
            }
        }
    }

    private fun menuMesas() {
        while (true) {
            println("\n--- MESAS ---")
            println("1. Listar mesas")
            println("2. Crear mesa")
            println("3. Actualizar mesa")
            println("4. Desactivar mesa")
            println("0. Volver")
            when (leerOpcion("Opcion: ", setOf("0", "1", "2", "3", "4"))) {
                "1" -> mesaService.listar().forEach {
                    println("${it.id} | mesa ${it.numero} | ${it.estado} | activa=${it.activa}")
                }

                "2" -> ejecutar {
                    val mesa = mesaService.crear(MesaRequest(leerEnteroPositivo("Numero de mesa: "), leerSiNo("Activa")))
                    println("Mesa creada: ${mesa.id}")
                }

                "3" -> ejecutar {
                    val mesa = mesaService.actualizar(
                        leerUuid("ID de la mesa: "),
                        MesaRequest(leerEnteroPositivo("Numero de mesa: "), leerSiNo("Activa"))
                    )
                    println("Mesa actualizada: ${mesa.numero}")
                }

                "4" -> ejecutar {
                    mesaService.desactivar(leerUuid("ID de la mesa: "))
                    println("Mesa desactivada.")
                }

                "0" -> return
            }
        }
    }

    private fun menuMesero(correo: String) {
        while (true) {
            println("\n=== MENU MESERO ===")
            println("1. Crear pedido")
            println("2. Consultar menu disponible")
            println("3. Ver mis pedidos")
            println("4. Editar pedido pendiente")
            println("5. Entregar pedido")
            println("6. Cancelar pedido")
            println("7. Ver historial de pedido")
            println("0. Cerrar sesion")
            when (leerOpcion("Opcion: ", setOf("0", "1", "2", "3", "4", "5", "6", "7"))) {
                "1" -> ejecutar {
                    val pedido = pedidoService.crear(leerPedido(), correo)
                    println("Pedido creado: ${pedido.id} | total=$${pedido.total} | ${pedido.estado}")
                }

                "2" -> mostrarMenuDisponible()
                "3" -> pedidoService.listarPedidosMesero(correo).forEach(::mostrarPedido)
                "4" -> editarPedidoPendiente(correo)
                "5" -> cambiarEstado(correo, EstadoPedido.ENTREGADO)
                "6" -> cambiarEstado(correo, EstadoPedido.CANCELADO)
                "7" -> mostrarHistorial(correo)
                "0" -> return
            }
        }
    }

    private fun mostrarMenuDisponible() {
        val pageable = PageRequest.of(0, 100)
        println("\n--- CATEGORIAS ACTIVAS ---")
        catalogoService.listarCategorias(pageable).contenido.forEach {
            println("${it.id} | ${it.nombre}")
        }
        println("\n--- PRODUCTOS DISPONIBLES ---")
        catalogoService.listarProductos(null, null, pageable).contenido.forEach {
            println("${it.id} | ${it.nombre} | $${it.precio} | ${it.categoria.nombre}")
        }
    }

    private fun editarPedidoPendiente(correo: String) = ejecutar {
        val pedidoId = leerUuid("ID del pedido pendiente: ")
        while (true) {
            val pedido = pedidoService.obtenerPedidoMesero(pedidoId, correo)
            if (pedido.estado != EstadoPedido.PENDIENTE) {
                println("Solo se puede editar un pedido PENDIENTE.")
                return@ejecutar
            }
            mostrarPedido(pedido)
            println("1. Agregar producto")
            println("2. Actualizar cantidad o notas")
            println("3. Eliminar producto")
            println("0. Volver")
            when (leerOpcion("Opcion: ", setOf("0", "1", "2", "3"))) {
                "1" -> {
                    pedidoService.agregarDetalle(
                        pedidoId,
                        AgregarDetallePedidoRequest(
                            productoId = leerUuid("ID del producto: "),
                            cantidad = leerEnteroPositivo("Cantidad: "),
                            notas = leerOpcional("Notas (opcional): ")
                        ),
                        correo
                    )
                }

                "2" -> {
                    pedidoService.actualizarDetalle(
                        pedidoId,
                        leerUuid("ID del detalle: "),
                        ActualizarDetallePedidoRequest(
                            cantidad = leerEnteroPositivo("Nueva cantidad: "),
                            notas = leerOpcional("Nuevas notas (vacio para eliminar): ")
                        ),
                        correo
                    )
                }

                "3" -> pedidoService.eliminarDetalle(
                    pedidoId,
                    leerUuid("ID del detalle: "),
                    correo
                )

                "0" -> return@ejecutar
            }
        }
    }

    private fun menuCocina(correo: String) {
        while (true) {
            println("\n=== MENU COCINA ===")
            println("1. Ver cola de pedidos")
            println("2. Tomar pedido para preparar")
            println("3. Marcar pedido como listo")
            println("4. Ver historial de pedido")
            println("0. Cerrar sesion")
            when (leerOpcion("Opcion: ", setOf("0", "1", "2", "3", "4"))) {
                "1" -> pedidoService.listarColaCocina().forEach(::mostrarPedido)
                "2" -> cambiarEstado(correo, EstadoPedido.EN_PREPARACION)
                "3" -> cambiarEstado(correo, EstadoPedido.LISTO)
                "4" -> mostrarHistorial(correo)
                "0" -> return
            }
        }
    }

    private fun leerPedido(): CrearPedidoRequest {
        val tipo = leerEnum<TipoPedido>("Tipo (SALON, PARA_LLEVAR, DOMICILIO): ")
        val mesaId = if (tipo == TipoPedido.SALON) leerUuid("ID de la mesa: ") else null
        if (tipo == TipoPedido.DOMICILIO) {
            throw UnsupportedOperationException("Los pedidos a domicilio se gestionan desde la API REST por ahora.")
        }
        val detalles = mutableListOf<DetallePedidoRequest>()
        do {
            detalles += DetallePedidoRequest(
                productoId = leerUuid("ID del producto: "),
                cantidad = leerEnteroPositivo("Cantidad: "),
                notas = leerOpcional("Notas (opcional): ")
            )
        } while (leerSiNo("Desea agregar otro producto"))
        return CrearPedidoRequest(tipo = tipo, mesaId = mesaId, detalles = detalles)
    }

    private fun leerProductoRequest(): ProductoRequest =
        ProductoRequest(
            nombre = leerRequerido("Nombre: "),
            descripcion = leerOpcional("Descripcion: "),
            precio = leerDecimalNoNegativo("Precio: "),
            categoriaId = leerUuid("ID de la categoria: "),
            disponible = leerSiNo("Disponible")
        )

    private fun cambiarEstado(correo: String, estado: EstadoPedido) = ejecutar {
        val pedido = pedidoService.cambiarEstado(leerUuid("ID del pedido: "), estado, correo)
        println("Pedido ${pedido.id} actualizado a ${pedido.estado}.")
    }

    private fun mostrarHistorial(correo: String) = ejecutar {
        pedidoService.historial(leerUuid("ID del pedido: "), correo).forEach {
            println("${it.fecha} | ${it.estado} | ${it.usuarioNombre ?: "Sistema"}")
        }
    }

    private fun mostrarPedido(pedido: com.restaurante.restaurantbackend.dto.PedidoResponse) {
        println("${pedido.id} | ${pedido.tipo} | ${pedido.estado} | total=$${pedido.total}")
        pedido.detalles.forEach { detalle ->
            println("  - ${detalle.cantidad} x ${detalle.nombreProducto} ($${detalle.precioUnitario}) ${detalle.notas.orEmpty()}")
        }
    }

    private fun ejecutar(accion: () -> Unit) {
        try {
            accion()
        } catch (exception: ResponseStatusException) {
            println("Error ${exception.statusCode.value()}: ${exception.reason}")
        } catch (exception: UnsupportedOperationException) {
            println("Operacion no disponible: ${exception.message}")
        } catch (exception: IllegalArgumentException) {
            println("Entrada invalida: ${exception.message}")
        } catch (exception: Exception) {
            println("No fue posible completar la operacion.")
        }
    }

    private fun leerOpcion(mensaje: String, opciones: Set<String>): String {
        while (true) {
            val opcion = leerRequerido(mensaje)
            if (opcion in opciones) return opcion
            println("Opcion invalida.")
        }
    }

    private fun leerRequerido(mensaje: String): String {
        while (true) {
            print(mensaje)
            if (!scanner.hasNextLine()) {
                throw ConsoleInputClosedException()
            }
            val valor = scanner.nextLine().trim()
            if (valor.isNotEmpty()) return valor
            println("Este campo es obligatorio.")
        }
    }

    private fun leerOpcional(mensaje: String): String? {
        print(mensaje)
        return scanner.nextLine().trim().ifBlank { null }
    }

    private fun leerSiNo(mensaje: String): Boolean {
        while (true) {
            print("$mensaje (S/N): ")
            when (scanner.nextLine().trim().uppercase()) {
                "S", "SI" -> return true
                "N", "NO" -> return false
                else -> println("Responda S o N.")
            }
        }
    }

    private fun leerUuid(mensaje: String): UUID {
        while (true) {
            try {
                return UUID.fromString(leerRequerido(mensaje))
            } catch (_: IllegalArgumentException) {
                println("El valor debe ser un UUID valido.")
            }
        }
    }

    private fun leerEnteroPositivo(mensaje: String): Int {
        while (true) {
            val valor = leerRequerido(mensaje).toIntOrNull()
            if (valor != null && valor > 0) return valor
            println("Ingrese un numero entero mayor que cero.")
        }
    }

    private fun leerDecimalNoNegativo(mensaje: String): BigDecimal {
        while (true) {
            val valor = leerRequerido(mensaje).replace(',', '.').toBigDecimalOrNull()
            if (valor != null && valor >= BigDecimal.ZERO) return valor
            println("Ingrese un precio numerico mayor o igual a cero.")
        }
    }

    private inline fun <reified T : Enum<T>> leerEnum(mensaje: String): T {
        while (true) {
            val valor = leerRequerido(mensaje).uppercase()
            enumValues<T>().firstOrNull { it.name == valor }?.let { return it }
            println("Valor invalido. Opciones: ${enumValues<T>().joinToString { it.name }}")
        }
    }

    private data class Sesion(val correo: String, val rol: RolUsuario)

    private class ConsoleInputClosedException : RuntimeException()
}
