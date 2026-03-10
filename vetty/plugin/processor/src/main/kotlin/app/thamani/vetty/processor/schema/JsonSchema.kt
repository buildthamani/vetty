package app.thamani.vetty.processor.schema

import app.thamani.vetty.processor.helpers.jsonString
import com.google.devtools.ksp.symbol.KSClassDeclaration

internal fun buildJsonSchema(
    classDecl: KSClassDeclaration,
    route: String,
    method: String,
    strict: Boolean,
    description: String,
): String {
    val sb = StringBuilder()
    sb.appendLine("{")
    sb.appendLine("""  "${"$"}schema": "https://json-schema.org/draft/2020-12/schema",""")
    sb.appendLine("""  "${"$"}id": "${method.uppercase()}_$route",""")
    if (description.isNotBlank()) sb.appendLine("""  "description": ${description.jsonString()},""")
    sb.appendLine("""  "type": "object",""")
    if (strict) sb.appendLine("""  "additionalProperties": false,""")

    val properties = mutableMapOf<String, String>()
    val required = mutableListOf<String>()

    classDecl.getAllProperties().forEach { prop ->
        val name = prop.simpleName.asString()
        val type = prop.type.resolve()
        val field = prop.getVettyField()
        val nullable = type.isMarkedNullable

        if (!nullable) required += name

        properties[name] =
            buildPropertySchema(
                type = type,
                field = field,
                nullable = nullable,
            )
    }

    sb.appendLine("""  "properties": {""")
    properties.entries.forEachIndexed { i, (name, schema) ->
        val comma = if (i < properties.size - 1) "," else ""
        sb.appendLine("""    ${name.jsonString()}: $schema$comma""")
    }
    sb.appendLine("  },")

    sb.append("""  "required": [""")
    sb.append(required.joinToString(", ") { it.jsonString() })
    sb.appendLine("]")
    sb.append("}")

    return sb.toString()
}

internal fun schemaSlug(
    method: String,
    route: String,
): String =
    buildString {
        append(
            route
                .replace("_", "")
                .replace("{", "")
                .replace("}", ""),
        )
        append("/")
        append(method)
    }
