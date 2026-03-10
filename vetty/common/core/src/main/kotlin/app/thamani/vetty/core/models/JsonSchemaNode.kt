package app.thamani.vetty.core.models

/**
 * Lightweight in-memory representation of a JSON Schema node.
 *
 * Deliberately minimal — no external JSON Schema library dependency.
 * Covers everything emitted by the KSP plugin: types, required fields,
 * string/number/array constraints, enums, and nested objects.
 *
 * Recursive for nested objects: a property of type `"object"` will itself
 * have a populated [properties] map parsed from the generated schema file.
 */
data class JsonSchemaNode(
    /** JSON Schema type(s). Multiple allowed for nullable: `["string", "null"]`. */
    val type: List<String> = listOf("object"),
    /** Child property schemas — only populated when [type] contains `"object"`. */
    val properties: Map<String, JsonSchemaNode> = emptyMap(),
    /** Property names that must be present in the response object. */
    val required: List<String> = emptyList(),
    /** When false, any field not listed in [properties] is a violation. */
    val additionalProperties: Boolean = true,
    // ── String constraints ────────────────────────────────────────────────────
    val minLength: Int? = null,
    val maxLength: Int? = null,
    val pattern: String? = null,
    val enum: List<String>? = null,
    // ── Numeric constraints ───────────────────────────────────────────────────
    val minimum: Double? = null,
    val maximum: Double? = null,
    // ── Array constraints ─────────────────────────────────────────────────────
    val minItems: Int? = null,
    val maxItems: Int? = null,
    /** Schema applied to every element when [type] contains `"array"`. */
    val items: JsonSchemaNode? = null,
    // ── Metadata ──────────────────────────────────────────────────────────────
    val description: String? = null,
)
