package com.restaurante.restaurantbackend.service

import com.restaurante.restaurantbackend.dto.CrearUsuarioRequest
import com.restaurante.restaurantbackend.dto.CambiarEstadoUsuarioRequest
import com.restaurante.restaurantbackend.dto.ActualizarUsuarioRequest
import com.restaurante.restaurantbackend.dto.RestablecerContrasenaRequest
import com.restaurante.restaurantbackend.dto.UsuarioResponse
import com.restaurante.restaurantbackend.entity.RolUsuario
import com.restaurante.restaurantbackend.entity.Usuario
import com.restaurante.restaurantbackend.mapper.UsuarioMapper
import com.restaurante.restaurantbackend.repository.UsuarioRepository
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@Service
class UsuarioService(
    private val usuarioRepository: UsuarioRepository,
    private val passwordEncoder: PasswordEncoder
) {
    @Transactional(readOnly = true)
    fun listar(
        rol: RolUsuario?,
        activo: Boolean?,
        busqueda: String?,
        pageable: Pageable
    ): com.restaurante.restaurantbackend.dto.PaginaResponse<UsuarioResponse> {
        val specification = Specification<Usuario> { root, _, criteriaBuilder ->
            val predicates = mutableListOf<jakarta.persistence.criteria.Predicate>()
            rol?.let { predicates += criteriaBuilder.equal(root.get<RolUsuario>("rol"), it) }
            activo?.let { predicates += criteriaBuilder.equal(root.get<Boolean>("activo"), it) }
            busqueda?.trim()?.takeIf { it.isNotEmpty() }?.let { texto ->
                val pattern = "%${texto.lowercase()}%"
                predicates += criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("nombre")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("correo")), pattern)
                )
            }
            criteriaBuilder.and(*predicates.toTypedArray())
        }
        val pagina = usuarioRepository.findAll(specification, pageable)
        return com.restaurante.restaurantbackend.dto.PaginaResponse(
            contenido = pagina.content.map(UsuarioMapper::toResponse),
            pagina = pagina.number,
            tamano = pagina.size,
            totalElementos = pagina.totalElements,
            totalPaginas = pagina.totalPages
        )
    }

    @Transactional
    fun crear(solicitud: CrearUsuarioRequest): UsuarioResponse {
        val correo = solicitud.correo.trim().lowercase()
        if (usuarioRepository.existsByCorreoIgnoreCase(correo)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un usuario con ese correo.")
        }

        val usuario = Usuario(
            nombre = solicitud.nombre.trim(),
            correo = correo,
            contrasenaHash = requireNotNull(passwordEncoder.encode(solicitud.contrasena)),
            rol = solicitud.rol
        )
        return UsuarioMapper.toResponse(usuarioRepository.save(usuario))
    }

    @Transactional
    fun cambiarEstado(id: java.util.UUID, solicitud: CambiarEstadoUsuarioRequest): UsuarioResponse {
        val usuario = obtenerEntidad(id)
        validarAdministradorActivoRestante(usuario, usuario.rol, solicitud.activo)
        usuario.activo = solicitud.activo
        return UsuarioMapper.toResponse(usuario)
    }

    @Transactional
    fun actualizar(id: UUID, solicitud: ActualizarUsuarioRequest): UsuarioResponse {
        val usuario = obtenerEntidad(id)
        val correo = solicitud.correo.trim().lowercase()
        if (!usuario.correo.equals(correo, ignoreCase = true) && usuarioRepository.existsByCorreoIgnoreCase(correo)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un usuario con ese correo.")
        }
        validarAdministradorActivoRestante(usuario, solicitud.rol, usuario.activo)
        usuario.nombre = solicitud.nombre.trim()
        usuario.correo = correo
        usuario.rol = solicitud.rol
        return UsuarioMapper.toResponse(usuario)
    }

    @Transactional
    fun restablecerContrasena(id: UUID, solicitud: RestablecerContrasenaRequest) {
        obtenerEntidad(id).contrasenaHash = requireNotNull(passwordEncoder.encode(solicitud.contrasena))
    }

    private fun obtenerEntidad(id: UUID): Usuario =
        usuarioRepository.findById(id).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado.")
        }

    private fun validarAdministradorActivoRestante(
        usuario: Usuario,
        nuevoRol: RolUsuario,
        activo: Boolean
    ) {
        if (usuario.rol == RolUsuario.ADMINISTRADOR &&
            usuario.activo &&
            (nuevoRol != RolUsuario.ADMINISTRADOR || !activo) &&
            usuarioRepository.countByRolAndActivoTrue(RolUsuario.ADMINISTRADOR) == 1L
        ) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "No se puede dejar el sistema sin un administrador activo.")
        }
    }
}
