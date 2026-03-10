package app.thamani.vetty.processor.helpers

import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType

internal fun KSType.resolveJsonType(): String {
    val fqn = declaration.qualifiedName?.asString() ?: return "string"
    return when (fqn) {
        "kotlin.String" -> {
            "string"
        }

        "kotlin.Int", "kotlin.Long",
        "kotlin.Short", "kotlin.Byte",
        -> {
            "integer"
        }

        "kotlin.Float", "kotlin.Double" -> {
            "number"
        }

        "kotlin.Boolean" -> {
            "boolean"
        }

        "kotlin.collections.List",
        "kotlin.collections.MutableList",
        "kotlin.Array",
        -> {
            "array"
        }

        "kotlin.Unit", "kotlin.Nothing" -> {
            "null"
        }

        else -> {
            val decl = declaration
            if (decl is KSClassDeclaration && decl.classKind == ClassKind.ENUM_CLASS) {
                "string"
            } else {
                "object"
            }
        }
    }
}
