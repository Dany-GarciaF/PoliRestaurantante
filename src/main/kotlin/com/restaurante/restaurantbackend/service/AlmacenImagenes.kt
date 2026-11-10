package com.restaurante.restaurantbackend.service

import com.restaurante.restaurantbackend.entity.Imagen
import com.restaurante.restaurantbackend.repository.ImagenRepository
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
    @Value("\${app.uploads.directory:uploads}") directorio: String,
    @Value("\${app.uploads.provider:local}") private val proveedor: String,
    private val imagenRepository: ImagenRepository
) {
    private val raiz: Path = Path.of(directorio).toAbsolutePath().normalize()
    private val carpeta: Path = raiz.resolve("productos")
    private val usaBaseDatos = proveedor.equals("db", ignoreCase = true)

    fun guardar(archivo: MultipartFile): String {
        if (archivo.isEmpty || archivo.size > MAX_BYTES) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "La imagen es obligatoria y no puede superar 3 MB.")
        }
        val bytes = archivo.bytes
        val extension = detectarExtension(bytes)
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Formato no permitido. Usa JPG, PNG o WEBP.")
        val nombre = "${UUID.randomUUID()}.$extension"
        if (usaBaseDatos) {
            imagenRepository.save(Imagen(nombre, tipoContenido(extension), bytes))
        } else {
            Files.createDirectories(carpeta)
            Files.copy(bytes.inputStream(), carpeta.resolve(nombre).normalize(), StandardCopyOption.REPLACE_EXISTING)
        }
        return "$PREFIJO$nombre"
    }

    fun eliminar(url: String?) {
        if (url == null || !url.startsWith(PREFIJO)) return
        val nombre = url.removePrefix(PREFIJO)
        if (!NOMBRE_VALIDO.matches(nombre)) return
        imagenRepository.deleteById(nombre)
        val archivo = carpeta.resolve(nombre).normalize()
        if (archivo.startsWith(carpeta)) Files.deleteIfExists(archivo)
    }

    /** Busca primero en la base de datos y, si no está, en disco (imágenes antiguas). */
    fun leer(nombre: String): Pair<ByteArray, String>? {
        if (!NOMBRE_VALIDO.matches(nombre)) return null
        imagenRepository.findById(nombre).orElse(null)?.let { return it.datos to it.tipoContenido }
        val archivo = carpeta.resolve(nombre).normalize()
        if (!archivo.startsWith(carpeta) || !Files.isRegularFile(archivo)) return null
        return Files.readAllBytes(archivo) to tipoContenido(nombre.substringAfterLast('.'))
    }

    private fun tipoContenido(extension: String) = when (extension) {
        "png" -> "image/png"
        "webp" -> "image/webp"
        else -> "image/jpeg"
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
        private val NOMBRE_VALIDO = Regex("^[0-9a-f-]{36}\\.(jpg|png|webp)$")
    }
}
