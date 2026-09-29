package com.restaurante.restaurantbackend.config

import com.restaurante.restaurantbackend.security.JwtAuthenticationFilter
import com.restaurante.restaurantbackend.security.SecurityProblemHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableMethodSecurity
class SecurityConfiguration(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val securityProblemHandler: SecurityProblemHandler
) {
    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http.csrf { csrf -> csrf.disable() }
        http.sessionManagement { session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
        http.authorizeHttpRequests { authorization ->
            authorization.requestMatchers("/api/auth/**", "/actuator/health").permitAll()
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
