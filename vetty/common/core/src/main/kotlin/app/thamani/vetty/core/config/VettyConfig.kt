package app.thamani.vetty.core.config

import app.thamani.vetty.core.datasource.InMemoryVettyDataSource
import app.thamani.vetty.core.datasource.VettyDataSource
import app.thamani.vetty.core.models.ValidationResult
import app.thamani.vetty.core.models.VettyEvent

/**
 * Callback interface for validation results.
 *
 * Implement this when you need to react to failures outside of the UI —
 * Crashlytics reporting, Slack webhooks, CI assertions, etc.
 *
 * All methods have default no-op implementations so you only override what
 * you care about.
 *
 * ```kotlin
 * Vetty.init {
 *     validator = object : VettyValidator {
 *         override fun onValidationFailed(event: VettyEvent) {
 *             FirebaseCrashlytics.getInstance()
 *                 .log("Schema failure: ${event.method} ${event.route}")
 *         }
 *     }
 * }
 * ```
 */
interface VettyValidator {
    /** Every field in the response matched the registered schema. */
    fun onValidationPassed(event: VettyEvent) {}

    /** One or more fields violated the schema.  Violations are in [event.validationResult]. */
    fun onValidationFailed(event: VettyEvent) {}

    /** No schema was found for this route. */
    fun onNoSchema(event: VettyEvent) {}
}

/**
 * Immutable configuration snapshot.  Built via the [VettyConfigBuilder] DSL
 * inside [Vetty.init].
 */
data class VettyConfig(
    /**
     * Master switch.  When false, every intercept call is a no-op — zero
     * allocations, zero I/O.  Always set to [BuildConfig.DEBUG] in production.
     */
    val enabled: Boolean = true,
    /**
     * Where validated events are stored.  Defaults to [InMemoryVettyDataSource].
     * Swap for a Room-backed implementation if you need persistence across
     * process restarts.
     */
    val dataSource: VettyDataSource = InMemoryVettyDataSource(),
    /**
     * Optional callback interface.  Called synchronously on the interceptor
     * thread immediately after validation — keep it fast.
     */
    val validator: VettyValidator? = null,
    /**
     * When true, a [VettyValidationException] is thrown on validation failure.
     * Useful during development to surface contract breaks immediately.
     * Must never be true in a release build.
     */
    val throwOnFailure: Boolean = false,
    val ignoredRoutes: List<String> = emptyList(),
    /** ClassLoader used to find generated schema resources. */
    val classLoader: ClassLoader? = null,
)

/** DSL builder — every field has a sensible default so partial config is fine. */
class VettyConfigBuilder {
    var enabled: Boolean = true
    var dataSource: VettyDataSource = InMemoryVettyDataSource()
    var validator: VettyValidator? = null
    var throwOnFailure: Boolean = false
    var ignoredRoutes: List<String> = emptyList()
    var classLoader: ClassLoader? = null

    internal fun build() =
        VettyConfig(
            enabled = enabled,
            dataSource = dataSource,
            validator = validator,
            throwOnFailure = throwOnFailure,
            ignoredRoutes = ignoredRoutes,
            classLoader = classLoader,
        )
}

class VettyValidationException(
    event: VettyEvent,
) : Exception(
        buildString {
            appendLine("Vetty: validation failed — ${event.method} ${event.route}")
            (event.validationResult as? ValidationResult.Fail)?.violations?.forEach { v ->
                appendLine("  [${v.rule}] ${v.path}  expected: ${v.expected}  got: ${v.actual}")
            }
        },
    )
