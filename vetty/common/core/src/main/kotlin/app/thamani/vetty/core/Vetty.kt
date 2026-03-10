package app.thamani.vetty.core

import app.thamani.vetty.core.config.VettyConfig
import app.thamani.vetty.core.config.VettyConfigBuilder
import app.thamani.vetty.core.config.VettyValidationException
import app.thamani.vetty.core.config.VettyValidator
import app.thamani.vetty.core.datasource.VettyDataSource
import app.thamani.vetty.core.models.ValidationResult
import app.thamani.vetty.core.models.VettyEvent
import app.thamani.vetty.core.schema.SchemaLoader
import app.thamani.vetty.core.schema.SchemaValidator

/**
 * Main entry point for vetty-core.
 *
 * ## Setup (once, in Application.onCreate):
 * ```kotlin
 * Vetty.init {
 *     enabled        = BuildConfig.DEBUG
 *     throwOnFailure = BuildConfig.DEBUG
 *     dataSource     = InMemoryVettyDataSource()   // or your Room implementation
 *     validator      = object : VettyValidator {
 *         override fun onValidationFailed(event: VettyEvent) {
 *             Crashlytics.log("Schema breach: ${event.method} ${event.route}")
 *         }
 *     }
 *     ignoredRoutes  = listOf("/api/health", "/api/metrics/" + "*")
 * }
 * ```
 *
 * ## Observing data from anywhere:
 * ```kotlin
 * Vetty.dataSource.observeAll().collect { events -> ... }
 * Vetty.dataSource.observeFailed().collect { failures -> ... }
 * ```
 */
object Vetty {
    @Volatile
    private var config: VettyConfig = VettyConfig()

    /**
     * Direct access to the configured [VettyDataSource].
     * Use this to observe or query events from any layer — ViewModel, use case, test.
     *
     * ```kotlin
     * viewModelScope.launch {
     *     Vetty.dataSource.observeAll().collect { events ->
     *         _uiState.update { it.copy(events = events) }
     *     }
     * }
     * ```
     */
    val dataSource: VettyDataSource get() = config.dataSource

    /** Configure and activate Vetty. Safe to call multiple times — last write wins. */
    fun init(block: VettyConfigBuilder.() -> Unit = {}) {
        config = VettyConfigBuilder().apply(block).build()
        if (config.enabled) {
            SchemaLoader.load(config.classLoader ?: Vetty::class.java.classLoader!!)
        }
    }

    /**
     * Called by interceptors after receiving a response.
     *
     * Execution stays on the calling thread throughout — the save to [VettyDataSource]
     * is a synchronous in-memory write (O(1)), and the [VettyValidator] callback is
     * invoked inline.  No coroutine is launched here so there is zero scheduler overhead
     * on the hot path.
     */
    fun validate(
        responseBody: String,
        route: String,
        method: String,
        requestUrl: String,
        statusCode: Int,
        requestBody: String? = null,
        durationMs: Long = 0L,
    ) {
        if (!config.enabled) return
        if (isIgnored(route)) return

        val result: ValidationResult =
            when (val loaded = SchemaLoader.findSchema(method, route)) {
                null -> ValidationResult.NoSchema
                else -> SchemaValidator.validate(responseBody, loaded.schema)
            }

        val event =
            VettyEvent(
                route = route,
                method = method.uppercase(),
                statusCode = statusCode,
                requestUrl = requestUrl,
                requestBody = requestBody,
                responseBody = responseBody,
                validationResult = result,
                durationMs = durationMs,
            )

        // 1. Persist — StateFlow.update is lock-free and returns immediately
        config.dataSource.save(event)

        // 2. Notify validator callback (on calling thread — keep it fast)
        config.validator?.let { v ->
            when (result) {
                is ValidationResult.Pass -> v.onValidationPassed(event)
                is ValidationResult.Fail -> v.onValidationFailed(event)
                is ValidationResult.NoSchema -> v.onNoSchema(event)
            }
        }

        // 3. Optionally throw during development
        if (config.throwOnFailure && result is ValidationResult.Fail) {
            throw VettyValidationException(event)
        }
    }

    fun currentConfig(): VettyConfig = config

    private fun isIgnored(route: String): Boolean =
        config.ignoredRoutes.any { pattern ->
            if (pattern.endsWith("*")) {
                route.startsWith(pattern.dropLast(1))
            } else {
                route == pattern
            }
        }
}
