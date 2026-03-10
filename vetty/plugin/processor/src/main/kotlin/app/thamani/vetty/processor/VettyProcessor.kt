package app.thamani.vetty.processor

import app.thamani.vetty.processor.helpers.jsonString
import app.thamani.vetty.processor.klass.processClass
import app.thamani.vetty.processor.schema.SchemaEntry
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate
import java.io.OutputStream

private const val ANNOTATION_SCHEMA = "app.thamani.vetty.annotations.VettySchema"
private const val ANNOTATION_FIELD = "app.thamani.vetty.annotations.VettyField"

abstract class VettySymbolProcessor(
    internal val mCodeGenerator: CodeGenerator,
    internal val mLogger: KSPLogger,
) : SymbolProcessor

/**
 * KSP processor that scans classes annotated with @VettySchema and emits:
 *  - One JSON Schema file per annotated class  →  resources/vetty/<METHOD>_<route-slug>.json
 *  - An index file listing all (route, method, schemaFile) tuples  →  resources/vetty/vetty_index.json
 *
 * The generated files are bundled into the APK/JAR and loaded at runtime by vetty-core's SchemaLoader.
 */
class VettyProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>,
) : VettySymbolProcessor(
        mCodeGenerator = codeGenerator,
        mLogger = logger,
    ) {
    private val schemaEntries = mutableListOf<SchemaEntry>()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols =
            resolver
                .getSymbolsWithAnnotation(ANNOTATION_SCHEMA)
                .filterIsInstance<KSClassDeclaration>()

        val unprocessed = symbols.filterNot { it.validate() }.toList()

        symbols
            .filter { it.validate() }
            .forEach { classDecl ->
                schemaEntries += processClass(classDecl)
            }

        return unprocessed
    }

    override fun finish() {
        if (schemaEntries.isEmpty()) return
        writeIndexFile()
    }

    private fun writeIndexFile() {
        val file: OutputStream =
            codeGenerator.createNewFileByPath(
                dependencies = Dependencies(aggregating = true),
                path = "vetty/vetty_index",
                extensionName = "json",
            )

        val entries =
            schemaEntries.joinToString(",\n") { entry ->
                """    {"route": ${entry.route.jsonString()}, "method": ${entry.method.jsonString()}, "schemaFile": ${entry.file.jsonString()}, "className": ${entry.klass.jsonString()}}"""
            }

        file.writer().use {
            it.write("[\n$entries\n]")
        }
    }
}
