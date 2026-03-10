package app.thamani.vetty.annotations

/**
 * Marks a class as a Vetty-validated response schema.
 *
 * Apply to data classes that represent JSON response bodies.
 * The plugin will generate a JSON Schema file at build time
 * and register it under [route] + [method] for runtime validation.
 *
 * Example:
 * ```kotlin
 * @VettySchema(route = "/api/v1/users/{id}", method = RouteMethod.GET)
 * data class User(
 *     val id: Int,
 *     val name: String,
 *     val email: String,
 * )
 * ```
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class VettySchema(
    val route: String,
    val method: RouteMethod,
    val strict: Boolean = false, // if true, extra fields = failure
    val description: String = "",
)
