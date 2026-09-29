package com.restaurante.restaurantbackend.security

import com.nimbusds.jose.JOSEException
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.MACSigner
import com.nimbusds.jose.crypto.MACVerifier
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import com.restaurante.restaurantbackend.config.JwtProperties
import com.restaurante.restaurantbackend.entity.Usuario
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.Base64
import java.util.Date

@Service
class JwtService(properties: JwtProperties) {
    private val secret: ByteArray = Base64.getDecoder().decode(properties.secretBase64).also {
        require(it.size >= MINIMUM_SECRET_LENGTH) {
            "JWT_SECRET debe codificar al menos $MINIMUM_SECRET_LENGTH bytes aleatorios."
        }
    }
    private val expirationMinutes = properties.expirationMinutes

    fun generateToken(usuario: Usuario): String {
        val now = Instant.now()
        val claims = JWTClaimsSet.Builder()
            .subject(usuario.correo)
            .claim("rol", usuario.rol.name)
            .issueTime(Date.from(now))
            .expirationTime(Date.from(now.plusSeconds(expirationMinutes * 60)))
            .build()
        val signedJwt = SignedJWT(JWSHeader(JWSAlgorithm.HS256), claims)
        signedJwt.sign(MACSigner(secret))
        return signedJwt.serialize()
    }

    @Throws(java.text.ParseException::class, JOSEException::class)
    fun extractCorreo(token: String): String {
        val signedJwt = SignedJWT.parse(token)
        if (!signedJwt.verify(MACVerifier(secret))) {
            throw JOSEException("La firma JWT no es válida.")
        }

        val claims = signedJwt.jwtClaimsSet
        if (claims.expirationTime.before(Date())) {
            throw JOSEException("El token JWT expiró.")
        }
        return requireNotNull(claims.subject) { "El token JWT no contiene un sujeto." }
    }

    companion object {
        private const val MINIMUM_SECRET_LENGTH = 32
    }
}
