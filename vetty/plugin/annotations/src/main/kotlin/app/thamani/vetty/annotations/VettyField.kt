package app.thamani.vetty.annotations

/**
 * Fine-grained control over a single field in a [VettySchema] class.
 *
 * Example:
 * ```kotlin
 * @VettySchema(route = "/api/users", method = RouteMethod.POST)
 * data class CreateUserResponse(
 *     @VettyField(key = "_name")
 *     val name: String,
 * )
 * ```
 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class VettyField(
    val key: String = "",
)
