package app.thamani.vetty.processor.schema

import kotlin.test.Test
import kotlin.test.assertEquals

private data class SchemaData(
    val method: String,
    val route: String,
)

class JsonSchemaTest {
    @Test
    fun `schema slug is generated correctly`() {
        listOf(
            SchemaData("GET", "/users") to "/users/GET",
            SchemaData("GET", "/users/{id}") to "/users/id/GET",
            SchemaData("GET", "/users/{id}/data") to "/users/id/data/GET",
            SchemaData("GET", "/users/{_id}/data") to "/users/id/data/GET",
        ).forEach { (data, expected) ->
            assertEquals(expected, schemaSlug(data.method, data.route))
        }
    }
}
