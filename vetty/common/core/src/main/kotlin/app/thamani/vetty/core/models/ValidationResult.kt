package app.thamani.vetty.core.models

sealed class ValidationResult {
    /** Every field in the response matched the registered schema. */
    data object Pass : ValidationResult()

    /** One or more fields violated the schema constraints. */
    data class Fail(
        val violations: List<SchemaViolation>,
    ) : ValidationResult()

    /** No schema was registered for this route + method combination. */
    data object NoSchema : ValidationResult()

    // Convenience properties for when you just need a boolean
    val isPassed: Boolean get() = this is Pass
    val isFailed: Boolean get() = this is Fail
    val isNoSchema: Boolean get() = this is NoSchema
}
