package app.thamani.vetty.core.models

data class SchemaViolation(
    val path: String, // dot-notation field path  — e.g. "$.user.email"
    val expected: String, // human-readable expectation — e.g. "length ≥ 1"
    val actual: String, // what was actually found   — e.g. "length 0"
    val rule: String, // the schema rule that fired — e.g. "minLength"
)
