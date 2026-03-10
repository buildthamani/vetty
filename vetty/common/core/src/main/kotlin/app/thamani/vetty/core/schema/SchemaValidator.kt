package app.thamani.vetty.core.schema

import app.thamani.vetty.core.models.JsonSchemaNode
import app.thamani.vetty.core.models.SchemaViolation
import app.thamani.vetty.core.models.ValidationResult
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.longOrNull

/**
 * Validates a raw JSON string against a [JsonSchemaNode] produced by [SchemaLoader].
 *
 * This is a deliberate lightweight implementation:
 *  - No external dependencies
 *  - No exceptions on parse errors (returns Fail instead)
 *  - Recursion is bounded by response depth (always finite for real APIs)
 *  - All violations are collected, not short-circuited
 */
object SchemaValidator {
    fun validate(
        responseBody: String,
        schema: JsonSchemaNode,
    ): ValidationResult {
        val element =
            try {
                Json.parseToJsonElement(responseBody)
            } catch (e: Exception) {
                return ValidationResult.Fail(
                    listOf(
                        SchemaViolation(
                            "$",
                            "valid JSON",
                            "parse error: ${e.message}",
                            "parse",
                        ),
                    ),
                )
            }
        val violations = mutableListOf<SchemaViolation>()
        validateNode(element, schema, "$", violations)
        return if (violations.isEmpty()) ValidationResult.Pass else ValidationResult.Fail(violations)
    }

    // ─── Recursive node validator ─────────────────────────────────────────────

    private fun validateNode(
        element: JsonElement,
        schema: JsonSchemaNode,
        path: String,
        violations: MutableList<SchemaViolation>,
    ) {
        val actualType = jsonTypeName(element)

        // ── Type check ─────────────────────────────────────────────────────────
        if (!schema.type.any { it == actualType || (it == "number" && actualType == "integer") }) {
            violations +=
                SchemaViolation(
                    path = path,
                    expected = schema.type.joinToString(" | "),
                    actual = actualType,
                    rule = "type",
                )
            return // no point validating further if the type is wrong
        }

        when (element) {
            is JsonObject -> {
                validateObject(element, schema, path, violations)
            }

            is JsonArray -> {
                validateArray(element, schema, path, violations)
            }

            is JsonPrimitive -> {
                validatePrimitive(element, schema, path, violations)
            }

            is JsonNull -> {
                // null is allowed if "null" is in schema.type, already checked
            }
        }
    }

    // ─── Object ───────────────────────────────────────────────────────────────

    private fun validateObject(
        obj: JsonObject,
        schema: JsonSchemaNode,
        path: String,
        violations: MutableList<SchemaViolation>,
    ) {
        // Required fields
        schema.required.forEach { key ->
            if (!obj.containsKey(key)) {
                violations +=
                    SchemaViolation(
                        path = "$path.$key",
                        expected = "present",
                        actual = "missing",
                        rule = "required",
                    )
            }
        }

        // Additional properties
        if (!schema.additionalProperties) {
            obj.keys.minus(schema.properties.keys).forEach { extra ->
                violations +=
                    SchemaViolation(
                        path = "$path.$extra",
                        expected = "no additional property",
                        actual = "extra field",
                        rule = "additionalProperties",
                    )
            }
        }

        // Recurse into known properties
        schema.properties.forEach { (key, childSchema) ->
            val childElement = obj[key] ?: return@forEach // absent optional → skip
            validateNode(childElement, childSchema, "$path.$key", violations)
        }
    }

    // ─── Array ────────────────────────────────────────────────────────────────

    private fun validateArray(
        arr: JsonArray,
        schema: JsonSchemaNode,
        path: String,
        violations: MutableList<SchemaViolation>,
    ) {
        schema.minItems?.let { min ->
            if (arr.size < min) {
                violations +=
                    SchemaViolation(
                        path = path,
                        expected = "≥ $min items",
                        actual = "${arr.size} items",
                        rule = "minItems",
                    )
            }
        }
        schema.maxItems?.let { max ->
            if (arr.size > max) {
                violations +=
                    SchemaViolation(
                        path = path,
                        expected = "≤ $max items",
                        actual = "${arr.size} items",
                        rule = "maxItems",
                    )
            }
        }
        schema.items?.let { itemSchema ->
            arr.forEachIndexed { i, child ->
                validateNode(child, itemSchema, "$path[$i]", violations)
            }
        }
    }

    // ─── Primitives ───────────────────────────────────────────────────────────

    private fun validatePrimitive(
        prim: JsonPrimitive,
        schema: JsonSchemaNode,
        path: String,
        violations: MutableList<SchemaViolation>,
    ) {
        val schemaType = schema.type.firstOrNull { it != "null" } ?: return

        when (schemaType) {
            "string" -> {
                val value = prim.contentOrNull ?: return
                schema.minLength?.let { min ->
                    if (value.length < min) {
                        violations +=
                            SchemaViolation(
                                path = path,
                                expected = "length ≥ $min",
                                actual = "length ${value.length}",
                                rule = "minLength",
                            )
                    }
                }
                schema.maxLength?.let { max ->
                    if (value.length > max) {
                        violations +=
                            SchemaViolation(
                                path = path,
                                expected = "length ≤ $max",
                                actual = "length ${value.length}",
                                rule = "maxLength",
                            )
                    }
                }
                schema.pattern?.let { pat ->
                    if (!value.matches(Regex(pat))) {
                        violations +=
                            SchemaViolation(
                                path = path,
                                expected = "matches /$pat/",
                                actual = value.take(64),
                                rule = "pattern",
                            )
                    }
                }
                schema.enum?.let { allowed ->
                    if (value !in allowed) {
                        violations +=
                            SchemaViolation(
                                path = path,
                                expected = "one of [${allowed.joinToString()}]",
                                actual = value,
                                rule = "enum",
                            )
                    }
                }
            }

            "integer", "number" -> {
                val value = prim.doubleOrNull ?: return
                schema.minimum?.let { min ->
                    if (value < min) {
                        violations +=
                            SchemaViolation(
                                path = path,
                                expected = "≥ $min",
                                actual = "$value",
                                rule = "minimum",
                            )
                    }
                }
                schema.maximum?.let { max ->
                    if (value > max) {
                        violations +=
                            SchemaViolation(
                                path = path,
                                expected = "≤ $max",
                                actual = "$value",
                                rule = "maximum",
                            )
                    }
                }
            }
        }
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private fun jsonTypeName(element: JsonElement): String =
        when {
            element is JsonNull -> "null"
            element is JsonObject -> "object"
            element is JsonArray -> "array"
            element is JsonPrimitive && element.isString -> "string"
            element is JsonPrimitive && element.booleanOrNull != null -> "boolean"
            element is JsonPrimitive && element.longOrNull != null -> "integer"
            element is JsonPrimitive && element.doubleOrNull != null -> "number"
            else -> "unknown"
        }
}
