package app.thamani.vetty.core.models

data class VettyEvent(
    // Unique ID — nanosecond timestamp, used as the stable key in LazyColumn
    val id: Long = System.nanoTime(),
    // Request identity
    val route: String, // template: /api/users/{id}
    val method: String, // uppercase: GET, POST, …
    val requestUrl: String, // full URL: https://api.example.com/api/users/42
    val requestBody: String?, // null for GET / bodyless requests
    // Response
    val statusCode: Int,
    val responseBody: String,
    // Validation outcome — sealed class: Pass | Fail(violations) | NoSchema
    val validationResult: ValidationResult,
    // Timing
    val durationMs: Long,
    val timestamp: Long = System.currentTimeMillis(),
) {
    // Convenience projection used by the UI for filtering and colour coding
    val displayStatus: VettyStatus
        get() =
            when (validationResult) {
                is ValidationResult.Pass -> VettyStatus.PASS
                is ValidationResult.Fail -> VettyStatus.FAIL
                is ValidationResult.NoSchema -> VettyStatus.NO_SCHEMA
            }
}
