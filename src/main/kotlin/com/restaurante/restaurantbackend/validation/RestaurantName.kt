package com.restaurante.restaurantbackend.validation

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [RestaurantNameValidator::class])
annotation class RestaurantName(
    val message: String = "Contiene caracteres no permitidos.",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Any>> = []
)

class RestaurantNameValidator : ConstraintValidator<RestaurantName, CharSequence> {
    override fun isValid(value: CharSequence?, context: ConstraintValidatorContext): Boolean =
        value == null || NAME_PATTERN.matches(value)

    companion object {
        private val NAME_PATTERN = Regex("""^[\p{L}\p{N}][\p{L}\p{N} .,'&()/%+#-]*$""")
    }
}
