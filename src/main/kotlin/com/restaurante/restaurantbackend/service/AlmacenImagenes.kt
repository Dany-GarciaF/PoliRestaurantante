package com.restaurante.restaurantbackend.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.UUID

/** Guarda imágenes de productos en disco. El tipo se decide por el contenido, no por el nombre enviado. */
@Component
class AlmacenImagenes(
    @Value("\${app.uploads.directory:uploads}") directorio: String
) {
    private val raiz: Path = Path.of(directorio).toAbsolutePath().normalize()
    private val carpeta: Path = raiz.resolve("productos")

    fun guardar(archivo: MultipartFile): String {
        if (archivo.isEmpty || archivo.size > MAX_BYTES) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "La imagen es obligatoria y no puede superar 3 MB.")
        }
        val bytes = archivo.bytes
        val extension = detectarExtension(bytes)
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Formato no permitido. Usa JPG, PNG o WEBP.")
        Files.createDirectories(carpeta)
        val nombre = "${UUID.randomUUID()}.$extension"
        val destino = carpeta.resolve(nombre).normalize()
        Files.copy(bytes.inputStream(), destino, StandardCopyOption.REPLACE_EXISTING)
        return "$PREFIJO$nombre"
    }

    fun eliminar(url: String?) {
        if (url == null || !url.startsWith(PREFIJO)) return
        val archivo = carpeta.resolve(url.removePrefix(PREFIJO)).normalize()
        if (archivo.startsWith(carpeta)) Files.deleteIfExists(archivo)
    }

    fun raizAbsoluta(): Path = raiz

    private fun detectarExtension(b: ByteArray): String? = when {
        b.size > 3 && b[0] == 0xFF.toByte() && b[1] == 0xD8.toByte() && b[2] == 0xFF.toByte() -> "jpg"
        b.size > 8 && b[0] == 0x89.toByte() && b[1] == 'P'.code.toByte() && b[2] == 'N'.code.toByte() && b[3] == 'G'.code.toByte() -> "png"
        b.size > 12 && String(b, 0, 4, Charsets.US_ASCII) == "RIFF" && String(b, 8, 4, Charsets.US_ASCII) == "WEBP" -> "webp"
        else -> null
    }

    companion object {
        const val PREFIJO = "/uploads/productos/"
        private const val MAX_BYTES = 3L * 1024 * 1024
    }
}
