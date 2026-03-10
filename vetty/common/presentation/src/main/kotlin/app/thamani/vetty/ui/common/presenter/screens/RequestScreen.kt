package app.thamani.vetty.ui.common.presenter.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import app.thamani.vetty.core.models.SchemaViolation
import app.thamani.vetty.core.models.ValidationResult
import app.thamani.vetty.core.models.VettyEvent
import app.thamani.vetty.core.models.VettyStatus
import app.thamani.vetty.ui.common.helpers.PrettyPrint
import app.thamani.vetty.ui.common.presenter.components.StatusBadge
import app.thamani.vetty.ui.common.presenter.components.VettyColors
import app.thamani.vetty.ui.common.presenter.components.VettySpacing
import app.thamani.vetty.ui.common.presenter.components.VettyTopBar
import app.thamani.vetty.ui.common.presenter.components.VettyTypography

@Composable
fun RequestScreen(
    event: VettyEvent,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Computed once per event — pure function, safe inside remember
    val lines: List<AnnotatedString> =
        remember(event.id) {
            PrettyPrint.prettyLines(event.responseBody)
        }

    val failedPaths: Set<String> =
        remember(event.id) {
            (event.validationResult as? ValidationResult.Fail)
                ?.violations
                ?.map { it.path }
                ?.toSet() ?: emptySet()
        }

    Column(modifier = modifier.background(VettyColors.Surface)) {
        // ── Header ─────────────────────────────────────────────────────────────
        VettyTopBar(title = "${event.method}  ${event.route}") {
            Text(
                text = "← Back",
                style = VettyTypography.labelMedium,
                color = VettyColors.Accent,
                modifier = Modifier.clickable(onClick = onBack),
            )
        }

        // ── Meta row ───────────────────────────────────────────────────────────
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(VettyColors.SurfaceElevated)
                    .padding(horizontal = VettySpacing.lg, vertical = VettySpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(VettySpacing.md),
        ) {
            StatusBadge(event.displayStatus)
            Text(
                text = "${event.statusCode}",
                style = VettyTypography.labelMedium,
                color = if (event.statusCode in 200..299) VettyColors.Pass else VettyColors.Fail,
            )
            Text(
                text = "${event.durationMs}ms",
                style = VettyTypography.labelSmall,
                color = VettyColors.TextSecondary,
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "${lines.size} lines",
                style = VettyTypography.labelSmall,
                color = VettyColors.TextTertiary,
            )
        }

        // ── Violation summary ──────────────────────────────────────────────────
        val result = event.validationResult
        if (result is ValidationResult.Fail) {
            ViolationSummary(
                violations = result.violations,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(
            Modifier
                .height(1.dp)
                .fillMaxWidth()
                .background(VettyColors.SurfaceBorder),
        )

        // ── JSON viewer ────────────────────────────────────────────────────────
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = VettySpacing.xs),
        ) {
            itemsIndexed(lines) { index, line ->
                JsonLineRow(
                    lineNumber = index + 1,
                    line = line,
                    status = event.displayStatus,
                    isViolated = isLineViolated(line.text, failedPaths),
                )
            }
        }
    }
}

@Composable
private fun JsonLineRow(
    lineNumber: Int,
    line: AnnotatedString,
    status: VettyStatus,
    isViolated: Boolean,
) {
    val bg = if (isViolated) VettyColors.FailDim.copy(alpha = 0.5f) else VettyColors.Surface
    val gutterColor =
        when {
            isViolated -> VettyColors.Fail
            status == VettyStatus.PASS -> VettyColors.Pass.copy(alpha = 0.45f)
            status == VettyStatus.FAIL -> VettyColors.Fail.copy(alpha = 0.2f)
            else -> VettyColors.NoSchema.copy(alpha = 0.3f)
        }

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(bg),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier =
                Modifier
                    .width(3.dp)
                    .height(22.dp)
                    .background(gutterColor),
        )

        Text(
            text = lineNumber.toString(),
            style = VettyTypography.codeSmall,
            color = VettyColors.TextTertiary,
            modifier =
                Modifier
                    .width(36.dp)
                    .padding(start = VettySpacing.sm),
        )

        Text(
            text = line,
            style = VettyTypography.codeSmall,
            modifier =
                Modifier
                    .weight(1f)
                    .padding(end = VettySpacing.md),
        )
    }
}

@Composable
private fun ViolationSummary(
    violations: List<SchemaViolation>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .background(VettyColors.FailDim.copy(alpha = 0.6f))
                .padding(horizontal = VettySpacing.lg, vertical = VettySpacing.sm),
    ) {
        Text(
            text = "${violations.size} violation${if (violations.size == 1) "" else "s"}",
            style = VettyTypography.labelMedium,
            color = VettyColors.Fail,
        )
        Spacer(Modifier.height(VettySpacing.xs))
        violations.forEach { v ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(VettySpacing.sm),
                modifier = Modifier.padding(vertical = 2.dp),
            ) {
                Box(
                    modifier =
                        Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(VettyColors.Fail.copy(alpha = 0.2f))
                            .padding(horizontal = 4.dp, vertical = 1.dp),
                ) {
                    Text(v.rule, style = VettyTypography.labelSmall, color = VettyColors.Fail)
                }
                Text(
                    text = "${v.path}  ·  expected ${v.expected}  ·  got ${v.actual}",
                    style = VettyTypography.codeSmall,
                    color = VettyColors.TextSecondary,
                )
            }
        }
    }
}

fun isLineViolated(
    lineText: String,
    failedPaths: Set<String>,
): Boolean {
    if (failedPaths.isEmpty()) return false
    return failedPaths.any { path ->
        val key =
            path
                .substringAfterLast('.')
                .substringAfterLast('[')
                .trimEnd(']')
                .trimStart('$')
        lineText.contains("\"$key\"")
    }
}
