package app.thamani.vetty.core.models

/**
 * A fully parsed schema ready for validation — pairs the route template with
 * the deserialized [JsonSchemaNode] tree built by [SchemaLoader].
 */
data class LoadedSchema(
    val route: String,
    val method: String,
    val schema: JsonSchemaNode,
)
