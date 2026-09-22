package com.restaurante.restaurantbackend.validation

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [NoControlCharactersValidator::class])
annotation class NoControlCharacters(
    val message: String = "No puede contener caracteres de control.",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Any>> = []
)

class NoControlCharactersValidator : ConstraintValidator<NoControlCharacters, CharSequence> {
    override fun isValid(value: CharSequence?, context: ConstraintValidatorContext): Boolean =
        value == null || value.codePoints().noneMatch { codePoint ->
            Character.isISOControl(codePoint) || Character.getType(codePoint) == Character.FORMAT.toInt()
        }
}
