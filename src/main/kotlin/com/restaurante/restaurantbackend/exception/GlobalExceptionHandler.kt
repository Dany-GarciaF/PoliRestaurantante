package com.restaurante.restaurantbackend.exception

import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.ConstraintViolationException
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.server.ResponseStatusException
import java.net.URI

@RestControllerAdvice
class GlobalExceptionHandler {
    private val logger = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleInvalidBody(
        exception: MethodArgumentNotValidException,
        request: HttpServletRequest
    ): ResponseEntity<ProblemDetail> {
        val errors = exception.bindingResult.fieldErrors
            .groupBy { it.field }
            .mapValues { (_, fieldErrors) ->
                fieldErrors.map { it.defaultMessage ?: "Valor inválido." }.distinct()
            }
        return response(HttpStatus.BAD_REQUEST, "La solicitud contiene campos inválidos.", "validation", request, errors)
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolation(
        exception: ConstraintViolationException,
        request: HttpServletRequest
    ): ResponseEntity<ProblemDetail> {
        val errors = exception.constraintViolations
            .groupBy { it.propertyPath.toString() }
            .mapValues { (_, violations) -> violations.map { it.message }.distinct() }
        return response(HttpStatus.BAD_REQUEST, "La solicitud contiene parámetros inválidos.", "validation", request, errors)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleUnreadableBody(
        exception: HttpMessageNotReadableException,
        request: HttpServletRequest
    ): ResponseEntity<ProblemDetail> =
        response(HttpStatus.BAD_REQUEST, "El cuerpo de la solicitud es inválido o está incompleto.", "malformed-request", request)

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatch(
        exception: MethodArgumentTypeMismatchException,
        request: HttpServletRequest
    ): ResponseEntity<ProblemDetail> =
        response(
            HttpStatus.BAD_REQUEST,
            "El parámetro '${exception.name}' tiene un formato inválido.",
            "type-mismatch",
            request
        )

    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingParameter(
        exception: MissingServletRequestParameterException,
        request: HttpServletRequest
    ): ResponseEntity<ProblemDetail> =
        response(
            HttpStatus.BAD_REQUEST,
            "Falta el parámetro obligatorio '${exception.parameterName}'.",
            "missing-parameter",
            request
        )

    @ExceptionHandler(ResponseStatusException::class)
    fun handleResponseStatus(
        exception: ResponseStatusException,
        request: HttpServletRequest
    ): ResponseEntity<ProblemDetail> =
        response(
            exception.statusCode,
            exception.reason ?: "La solicitud no se pudo completar.",
            "request-error",
            request
        )

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDenied(
        exception: AccessDeniedException,
        request: HttpServletRequest
    ): ResponseEntity<ProblemDetail> =
        response(HttpStatus.FORBIDDEN, "No tiene permisos para realizar esta operación.", "forbidden", request)

    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleDataIntegrity(
        exception: DataIntegrityViolationException,
        request: HttpServletRequest
    ): ResponseEntity<ProblemDetail> =
        response(HttpStatus.CONFLICT, "La operación entra en conflicto con datos existentes.", "data-conflict", request)

    @ExceptionHandler(Exception::class)
    fun handleUnexpected(
        exception: Exception,
        request: HttpServletRequest
    ): ResponseEntity<ProblemDetail> {
        logger.error("Error no controlado al procesar {} {}", request.method, request.requestURI, exception)
        return response(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Ocurrió un error interno. Intente nuevamente más tarde.",
            "internal-error",
            request
        )
    }

    private fun response(
        status: org.springframework.http.HttpStatusCode,
        detail: String,
        type: String,
        request: HttpServletRequest,
        errors: Map<String, List<String>>? = null
    ): ResponseEntity<ProblemDetail> {
        val problem = ProblemDetail.forStatusAndDetail(status, detail)
        problem.title = status.toString()
        problem.type = URI.create("https://polirestaurante.local/problems/$type")
        problem.instance = URI.create(request.requestURI)
        errors?.let { problem.setProperty("errors", it) }
        return ResponseEntity.status(status).body(problem)
    }
}
