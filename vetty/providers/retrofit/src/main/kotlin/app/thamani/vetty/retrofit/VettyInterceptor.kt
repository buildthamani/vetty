package app.thamani.vetty.retrofit

import app.thamani.vetty.core.Vetty
import app.thamani.vetty.core.models.ValidationResult
import app.thamani.vetty.core.models.VettyEvent
import app.thamani.vetty.core.schema.SchemaLoader
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer

/**
 * OkHttp interceptor that pipes every response through Vetty validation.
 *
 * ## Instantiate directly with a config lambda:
 * ```kotlin
 * val client = OkHttpClient.Builder()
 *     .addInterceptor(VettyInterceptor {
 *         enabled = BuildConfig.DEBUG
 *         verbose = true
 *     })
 *     .build()
 * ```
 *
 * ## Or subclass to add your own behaviour:
 * ```kotlin
 * class MyInterceptor : VettyInterceptor({
 *     enabled = BuildConfig.DEBUG
 *     verbose = true
 * }) {
 *     override fun onValidationFailed(event: VettyEvent) {
 *         MyAnalytics.track("schema_breach", mapOf(
 *             "route"      to event.route,
 *             "violations" to (event.validationResult as ValidationResult.Fail).violations.size
 *         ))
 *     }
 *
 *     override fun onBeforeRequest(request: Request): Request {
 *         // mutate headers, add auth, etc.
 *         return request.newBuilder()
 *             .addHeader("X-Vetty", "1")
 *             .build()
 *     }
 * }
 * ```
 *
 * Subclassing deliberately mirrors the OkHttp interceptor pattern — you override
 * only the hooks you care about and the base handles all the plumbing.
 */
open class VettyInterceptor(
    config: VettyInterceptorConfig.() -> Unit = {},
) : Interceptor {
    private val interceptorConfig = VettyInterceptorConfig().apply(config)

    val enabled: Boolean get() = interceptorConfig.enabled
    val verbose: Boolean get() = interceptorConfig.verbose

    final override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        if (!enabled || !Vetty.currentConfig().enabled) {
            return chain.proceed(originalRequest)
        }

        // Hook: allow subclasses to mutate the request before it goes out
        val request = onBeforeRequest(originalRequest)

        val startMs = System.currentTimeMillis()
        val response = chain.proceed(request)
        val durationMs = System.currentTimeMillis() - startMs

        val rawBody = response.body
        val bodyString = rawBody.string().orEmpty()
        val path = request.url.encodedPath
        val route = resolveRoute(path) ?: path

        if (verbose) logVerbose(request, response, bodyString, durationMs)

        Vetty.validate(
            responseBody = bodyString,
            route = route,
            method = request.method,
            requestUrl = request.url.toString(),
            statusCode = response.code,
            requestBody = peekRequestBody(request),
            durationMs = durationMs,
        )

        // Retrieve the event that was just saved so subclass hooks get the full picture
        val event = Vetty.dataSource.getAll().firstOrNull()
        if (event != null) {
            when (event.validationResult) {
                is ValidationResult.Pass -> onValidationPassed(event)
                is ValidationResult.Fail -> onValidationFailed(event)
                is ValidationResult.NoSchema -> onNoSchema(event)
            }
        }

        // Rebuild the response — OkHttp body can only be read once
        val newBody = bodyString.toResponseBody(rawBody.contentType())
        val builtResponse = response.newBuilder().body(newBody).build()

        return onBeforeReturn(builtResponse)
    }

    // ─── Override points ──────────────────────────────────────────────────────

    /**
     * Called before the request is dispatched.  Return a (possibly mutated)
     * request.  Default returns the request unchanged.
     */
    open fun onBeforeRequest(request: Request): Request = request

    /**
     * Called after validation when the result is [ValidationResult.Pass].
     * Default is a no-op — override in subclasses to add custom behaviour.
     */
    open fun onValidationPassed(event: VettyEvent) {}

    /**
     * Called after validation when the result is [ValidationResult.Fail].
     * This is the most commonly overridden hook — log to Crashlytics, post to
     * Slack, throw in CI, etc.
     */
    open fun onValidationFailed(event: VettyEvent) {}

    /**
     * Called when no schema is registered for this route + method.
     */
    open fun onNoSchema(event: VettyEvent) {}

    /**
     * Last hook before the response is returned to the Retrofit call adapter.
     * Return a (possibly mutated) response.  Default returns unchanged.
     */
    open fun onBeforeReturn(response: Response): Response = response

    // ─── Private helpers ──────────────────────────────────────────────────────

    private fun resolveRoute(path: String): String? =
        SchemaLoader.allSchemas().firstOrNull { SchemaLoader.routeMatches(it.route, path) }?.route

    private fun peekRequestBody(request: Request): String? =
        try {
            val buffer = Buffer()
            request.body?.writeTo(buffer)
            buffer.readUtf8().takeIf { it.isNotBlank() }
        } catch (_: Exception) {
            null
        }

    private fun logVerbose(
        request: Request,
        response: Response,
        body: String,
        durationMs: Long,
    ) {
        val tag = "Vetty"
        println("[$tag] --> ${request.method} ${request.url}")
        println("[$tag] <-- ${response.code} (${durationMs}ms)")
        if (body.length > 2000) {
            println("[$tag] Body (truncated): ${body.take(2000)}…")
        } else {
            println("[$tag] Body: $body")
        }
    }
}

/**
 * Configuration for [VettyInterceptor].
 *
 * ```kotlin
 * VettyInterceptor {
 *     enabled = BuildConfig.DEBUG
 *     verbose = true
 * }
 * ```
 */
class VettyInterceptorConfig {
    /**
     * When false, the interceptor passes requests through untouched.
     * Defaults to true — you almost always want to gate this on BuildConfig.DEBUG.
     */
    var enabled: Boolean = true

    /**
     * When true, logs each request URL, response code, duration, and body to stdout.
     * Never enable this in a release build — bodies may contain PII.
     */
    var verbose: Boolean = false
}
