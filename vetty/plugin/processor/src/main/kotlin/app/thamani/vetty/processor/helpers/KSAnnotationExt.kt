package app.thamani.vetty.processor.helpers

import com.google.devtools.ksp.symbol.KSAnnotation

@Suppress("UNCHECKED_CAST")
internal inline fun <reified T> KSAnnotation.arg(name: String): T? =
    arguments
        .firstOrNull { it.name?.asString() == name }
        ?.value as? T
