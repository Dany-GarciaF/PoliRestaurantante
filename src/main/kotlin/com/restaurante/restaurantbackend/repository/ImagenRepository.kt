package com.restaurante.restaurantbackend.repository

import com.restaurante.restaurantbackend.entity.Imagen
import org.springframework.data.jpa.repository.JpaRepository

interface ImagenRepository : JpaRepository<Imagen, String>
