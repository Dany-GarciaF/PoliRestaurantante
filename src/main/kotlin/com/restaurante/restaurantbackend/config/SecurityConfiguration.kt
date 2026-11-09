package com.restaurante.restaurantbackend.config

import com.restaurante.restaurantbackend.security.JwtAuthenticationFilter
import com.restaurante.restaurantbackend.security.SecurityProblemHandler
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.context.annotation.Profile
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.context.annotation.Bean
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@Profile("!console")
@EnableMethodSecurity
class SecurityConfiguration(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val securityProblemHandler: SecurityProblemHandler,
    @Value("\${app.cors.allowed-origins}") private val allowedOrigins: List<String>
) {
    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration().apply {
            this.allowedOrigins = this@SecurityConfiguration.allowedOrigins
            allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            allowedHeaders = listOf("Authorization", "Content-Type", "Accept")
            maxAge = 3600
        }
        return UrlBasedCorsConfigurationSource().apply { registerCorsConfiguration("/api/**", configuration) }
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http.csrf { csrf -> csrf.disable() }
        http.cors { }
        http.sessionManagement { session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
        http.authorizeHttpRequests { authorization ->
            authorization.requestMatchers("/api/auth/**", "/api/publico/**", "/actuator/health").permitAll()
            authorization.requestMatchers(org.springframework.http.HttpMethod.GET, "/uploads/**").permitAll()
            authorization.anyRequest().authenticated()
        }
        http.exceptionHandling { exceptions ->
            exceptions.authenticationEntryPoint(securityProblemHandler)
            exceptions.accessDeniedHandler(securityProblemHandler)
        }
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
        return http.build()
    }
}
