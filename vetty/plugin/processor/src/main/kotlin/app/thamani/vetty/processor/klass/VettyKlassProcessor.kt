package app.thamani.vetty.processor.klass

import app.thamani.vetty.processor.VettySymbolProcessor
import app.thamani.vetty.processor.helpers.arg
import app.thamani.vetty.processor.schema.SchemaEntry
import app.thamani.vetty.processor.schema.buildJsonSchema
import app.thamani.vetty.processor.schema.schemaSlug
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSClassDeclaration
import java.io.OutputStream

internal fun VettySymbolProcessor.processClass(classDecl: KSClassDeclaration): SchemaEntry {
    val annotation = classDecl.annotations.first { it.shortName.asString() == "VettySchema" }

    val route = annotation.arg<String>("route").orEmpty()
    val method = annotation.arg<String>("method").toString()
    val strict = annotation.arg<Boolean>("strict") ?: false
    val description = annotation.arg<String>("description").orEmpty()

    mLogger.info("[Vetty] Generating schema: $method $route → ${classDecl.simpleName.asString()}")

    val schema = buildJsonSchema(classDecl, route, method, strict, description)
    val slug = schemaSlug(method, route)

    val file: OutputStream =
        mCodeGenerator.createNewFileByPath(
            dependencies = Dependencies(aggregating = false, classDecl.containingFile!!),
            path = "vetty/$slug",
            extensionName = "json",
        )

    file.writer().use { it.write(schema) }

    return SchemaEntry(
        route = route,
        method = method.uppercase(),
        file = "vetty/$slug.json",
        klass = classDecl.qualifiedName?.asString() ?: classDecl.simpleName.asString(),
    )
}
