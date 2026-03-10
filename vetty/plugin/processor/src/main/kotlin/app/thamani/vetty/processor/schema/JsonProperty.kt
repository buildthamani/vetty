package app.thamani.vetty.processor.schema

import app.thamani.vetty.processor.helpers.arg
import app.thamani.vetty.processor.helpers.jsonString
import app.thamani.vetty.processor.helpers.resolveJsonType
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType

internal const val VETTY_FIELD_NAME = "VettyField"

internal fun KSPropertyDeclaration.getVettyField(): KSAnnotation? = annotations.firstOrNull { it.shortName.asString() == VETTY_FIELD_NAME }

internal fun KSAnnotation.getVettyFieldValue(
    property: KSPropertyDeclaration,
    key: String,
): String {
    val providedKey =
        arguments
            .first { it.name?.asString() == key }
            .value as String

    // 3. Fallback to the property variable name if the default "" was used
    return providedKey.ifEmpty { property.simpleName.asString() }
}

internal fun buildPropertySchema(
    type: KSType,
    field: KSAnnotation?,
    nullable: Boolean,
): String {
    val sb = StringBuilder("{")
    val parts = mutableListOf<String>()

    val baseType = type.resolveJsonType()
    parts +=
        if (nullable) {
            """"type": ["$baseType", "null"]"""
        } else {
            """"type": "$baseType""""
        }

    field?.let { ann ->
        ann
            .arg<String>("description")
            ?.takeIf { it.isNotBlank() }
            ?.let { parts += """"description": ${it.jsonString()}""" }
    }

    // Nested object: recurse into its properties
    if (baseType == "object") {
        val declaration = type.declaration
        if (declaration is KSClassDeclaration) {
            val nested = declaration.buildNestedObjectSchema()
            parts += nested
        }
    }

    // Array: build items schema from the type argument
    if (baseType == "array") {
        val itemType = type.arguments.firstOrNull()?.type?.resolve()
        if (itemType != null) {
            val itemSchema = buildPropertySchema(
                type = itemType,
                field = null,
                nullable = itemType.isMarkedNullable,
            )
            parts += """"items": $itemSchema"""
        }
    }

    sb.append(parts.joinToString(", "))
    sb.append("}")
    return sb.toString()
}

private fun KSClassDeclaration.buildNestedObjectSchema(): String {
    val nestedProps = mutableMapOf<String, String>()
    val nestedRequired = mutableListOf<String>()

    getAllProperties()
        .forEach { prop ->

            val field = prop.getVettyField()
            val name =
                field
                    ?.getVettyFieldValue(prop, "key")
                    ?: prop.simpleName.asString()

            val type = prop.type.resolve()
            val nullable = type.isMarkedNullable

            if (!nullable) nestedRequired += name
            nestedProps[name] =
                buildPropertySchema(
                    type = type,
                    field = field,
                    nullable = nullable,
                )
        }

    val sb = StringBuilder()
    sb.append(""""properties": {""")
    nestedProps.entries.forEachIndexed { i, (k, v) ->
        val comma = if (i < nestedProps.size - 1) "," else ""
        sb.append("${k.jsonString()}: $v$comma")
    }
    sb.append("}, ")
    sb.append(""""required": [${nestedRequired.joinToString(", ") { it.jsonString() }}]""")
    return sb.toString()
}
