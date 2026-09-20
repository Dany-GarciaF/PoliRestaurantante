package com.restaurante.restaurantbackend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class RestaurantBackendApplication

fun main(args: Array<String>) {
    runApplication<RestaurantBackendApplication>(*args)
}
