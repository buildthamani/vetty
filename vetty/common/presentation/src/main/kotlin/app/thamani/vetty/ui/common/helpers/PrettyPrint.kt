package app.thamani.vetty.ui.common.helpers

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import app.thamani.vetty.ui.common.presenter.components.VettyColors

/**
 * Converts a raw JSON string into a syntax-highlighted [AnnotatedString].
 * This runs on the calling thread and is intentionally kept allocation-light.
 * For large payloads, call from a Compose derivedStateOf or background dispatcher.
 */
object PrettyPrint {
    private val INDENT = "  "

    /**
     * Returns a [List] of annotated lines so the UI can render each line independently
     * and apply per-line status colour (green/red/gray) at the start of each line.
     */
    fun prettyLines(raw: String): List<AnnotatedString> {
        val formatted = formatJson(raw)
        return formatted.lines().map { line -> highlightLine(line) }
    }

    // ─── Formatting ───────────────────────────────────────────────────────────

    private fun formatJson(raw: String): String {
        val sb = StringBuilder()
        var indent = 0
        var inString = false
        var i = 0

        fun appendIndent() = repeat(indent) { sb.append(INDENT) }

        while (i < raw.length) {
            val c = raw[i]
            when {
                c == '"' && (i == 0 || raw[i - 1] != '\\') -> {
                    inString = !inString
                    sb.append(c)
                }

                inString -> {
                    sb.append(c)
                }

                c == '{' || c == '[' -> {
                    sb.append(c)
                    if (i + 1 < raw.length && raw[i + 1] != '}' && raw[i + 1] != ']') {
                        sb.append('\n')
                        indent++
                        appendIndent()
                    }
                }

                c == '}' || c == ']' -> {
                    if (i > 0 && raw[i - 1] != '{' && raw[i - 1] != '[') {
                        sb.append('\n')
                        indent = maxOf(0, indent - 1)
                        appendIndent()
                    } else {
                        indent = maxOf(0, indent - 1)
                    }
                    sb.append(c)
                }

                c == ',' -> {
                    sb.append(c)
                    sb.append('\n')
                    appendIndent()
                }

                c == ':' -> {
                    sb.append(": ")
                }

                c != ' ' && c != '\n' && c != '\r' && c != '\t' -> {
                    sb.append(c)
                }
            }
            i++
        }

        return sb.toString()
    }

    // ─── Syntax highlighting ──────────────────────────────────────────────────

    private val keyPattern = Regex("""^\s*"([^"]+)"\s*:""")
    private val stringPattern = Regex(""":\s*"([^"]*)"$""")
    private val numberPattern = Regex(""":\s*(-?\d+\.?\d*([eE][+-]?\d+)?)$""")
    private val boolNullPattern = Regex(""":\s*(true|false|null)$""")

    private fun highlightLine(line: String): AnnotatedString =
        buildAnnotatedString {
            val trimmed = line.trimStart()

            // Pure structural line (brace/bracket only)
            if (trimmed == "{" || trimmed == "}" || trimmed == "[" || trimmed == "]" ||
                trimmed == "}," || trimmed == "],"
            ) {
                withStyle(SpanStyle(color = VettyColors.JsonBrace)) { append(line) }
                return@buildAnnotatedString
            }

            // Key extraction
            val keyMatch = keyPattern.find(line)
            val keyEnd = keyMatch?.range?.last ?: -1

            if (keyEnd >= 0) {
                // Leading whitespace
                val wsEnd = line.indexOfFirst { !it.isWhitespace() }
                append(line.substring(0, wsEnd))

                // Key token: "key":
                val keyToken = line.substring(wsEnd, keyEnd + 1)
                withStyle(SpanStyle(color = VettyColors.JsonKey)) { append(keyToken) }

                val rest = line.substring(keyEnd + 1).trimStart()
                appendValuePart(rest)
            } else {
                // Array value line
                appendValuePart(line.trimStart())
            }
        }

    private fun AnnotatedString.Builder.appendValuePart(value: String) {
        when {
            value.startsWith("\"") -> {
                withStyle(SpanStyle(color = VettyColors.JsonString)) { append(value) }
            }

            value == "true" || value == "false" ||
                value.trimEnd(',') == "true" || value.trimEnd(',') == "false" -> {
                val clean = value.trimEnd(',')
                withStyle(
                    SpanStyle(
                        color = VettyColors.JsonBoolean,
                        fontWeight = FontWeight.Bold,
                    ),
                ) {
                    append(clean)
                }
                if (value.endsWith(",")) {
                    withStyle(SpanStyle(color = VettyColors.JsonBrace)) {
                        append(
                            ",",
                        )
                    }
                }
            }

            value == "null" || value.trimEnd(',') == "null" -> {
                val clean = value.trimEnd(',')
                withStyle(SpanStyle(color = VettyColors.JsonNull)) { append(clean) }
                if (value.endsWith(",")) {
                    withStyle(SpanStyle(color = VettyColors.JsonBrace)) {
                        append(
                            ",",
                        )
                    }
                }
            }

            value.trimEnd(',').let { it.toDoubleOrNull() != null } -> {
                val clean = value.trimEnd(',')
                withStyle(SpanStyle(color = VettyColors.JsonNumber)) { append(clean) }
                if (value.endsWith(",")) {
                    withStyle(SpanStyle(color = VettyColors.JsonBrace)) {
                        append(
                            ",",
                        )
                    }
                }
            }

            else -> {
                withStyle(SpanStyle(color = VettyColors.JsonBrace)) { append(value) }
            }
        }
    }
}

/** Simple but accurate: counts JSON depth at a given line index. */
fun List<AnnotatedString>.lineDepth(index: Int): Int {
    var depth = 0
    for (i in 0 until index) {
        val text = this[i].text
        depth += text.count { it == '{' || it == '[' }
        depth -= text.count { it == '}' || it == ']' }
    }
    return maxOf(0, depth)
}
