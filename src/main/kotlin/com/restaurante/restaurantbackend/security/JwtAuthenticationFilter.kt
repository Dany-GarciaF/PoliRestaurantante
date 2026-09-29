package com.restaurante.restaurantbackend.security

import com.nimbusds.jose.JOSEException
import com.restaurante.restaurantbackend.repository.UsuarioRepository
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.text.ParseException

@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtService,
    private val usuarioRepository: UsuarioRepository,
    private val securityProblemHandler: SecurityProblemHandler
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authorization = request.getHeader(HttpHeaders.AUTHORIZATION)
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response)
            return
        }

        try {
            val correo = jwtService.extractCorreo(authorization.removePrefix("Bearer ").trim())
            val usuario = usuarioRepository.findByCorreoIgnoreCase(correo)
            if (usuario == null || !usuario.activo) {
                securityProblemHandler.invalidToken(request, response)
                return
            }

            val authentication = UsernamePasswordAuthenticationToken(
                usuario.correo,
                null,
                listOf(SimpleGrantedAuthority("ROLE_${usuario.rol.name}"))
            )
            authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
            SecurityContextHolder.getContext().authentication = authentication
            filterChain.doFilter(request, response)
        } catch (_: ParseException) {
            securityProblemHandler.invalidToken(request, response)
        } catch (_: JOSEException) {
            securityProblemHandler.invalidToken(request, response)
        } catch (_: IllegalArgumentException) {
            securityProblemHandler.invalidToken(request, response)
        }
    }
}
