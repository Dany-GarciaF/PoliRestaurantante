package com.restaurante.restaurantbackend.security

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ProblemDetail
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper
import java.net.URI

@Component
class SecurityProblemHandler(
    private val objectMapper: ObjectMapper
) : AuthenticationEntryPoint, AccessDeniedHandler {
    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authenticationException: AuthenticationException
    ) {
        writeProblem(
            request,
            response,
            HttpStatus.UNAUTHORIZED,
            "Debe autenticarse para acceder a este recurso.",
            "unauthorized"
        )
    }

    override fun handle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        accessDeniedException: AccessDeniedException
    ) {
        writeProblem(
            request,
            response,
            HttpStatus.FORBIDDEN,
            "No tiene permisos para realizar esta operación.",
            "forbidden"
        )
    }

    fun invalidToken(request: HttpServletRequest, response: HttpServletResponse) {
        writeProblem(
            request,
            response,
            HttpStatus.UNAUTHORIZED,
            "El token JWT es inválido o expiró.",
            "invalid-token"
        )
    }

    private fun writeProblem(
        request: HttpServletRequest,
        response: HttpServletResponse,
        status: HttpStatus,
        detail: String,
        type: String
    ) {
        if (response.isCommitted) {
            return
        }
        val problem = ProblemDetail.forStatusAndDetail(status, detail)
        problem.title = status.reasonPhrase
        problem.type = URI.create("https://polirestaurante.local/problems/$type")
        problem.instance = URI.create(request.requestURI)

        response.status = status.value()
        response.contentType = MediaType.APPLICATION_PROBLEM_JSON_VALUE
        objectMapper.writeValue(response.outputStream, problem)
    }
}
