package app.thamani.vetty.processor.helpers

internal fun String.jsonString(): String = "\"${replace("\\", "\\\\").replace("\"", "\\\"")}\""
