package app.thamani.vetty.ui.common.presenter.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import app.thamani.vetty.core.models.VettyEvent
import app.thamani.vetty.core.models.VettyStatus
import app.thamani.vetty.ui.common.container.VettyUiState
import app.thamani.vetty.ui.common.presenter.components.FilterChip
import app.thamani.vetty.ui.common.presenter.components.MethodBadge
import app.thamani.vetty.ui.common.presenter.components.StatTile
import app.thamani.vetty.ui.common.presenter.components.StatusBadge
import app.thamani.vetty.ui.common.presenter.components.VettyColors
import app.thamani.vetty.ui.common.presenter.components.VettySpacing
import app.thamani.vetty.ui.common.presenter.components.VettyTopBar
import app.thamani.vetty.ui.common.presenter.components.VettyTypography
import app.thamani.vetty.ui.common.presenter.components.toColor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val timeFmt = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())

@Composable
fun RequestsScreen(
    uiState: VettyUiState,
    onSelectEvent: (VettyEvent) -> Unit,
    onSetFilter: (VettyStatus?) -> Unit,
    onSetSearch: (String) -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.background(VettyColors.Surface)) {
        // ── Top bar ────────────────────────────────────────────────────────────
        VettyTopBar(title = "Vetty  ·  ${uiState.events.size} requests") {
            Text(
                text = "✕",
                style = VettyTypography.labelMedium,
                color = VettyColors.TextSecondary,
                modifier = Modifier.clickable(onClick = onClearAll),
            )
        }

        // ── Summary stats ──────────────────────────────────────────────────────
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(VettyColors.SurfaceElevated)
                    .padding(horizontal = VettySpacing.lg, vertical = VettySpacing.sm),
            horizontalArrangement = Arrangement.spacedBy(VettySpacing.sm),
        ) {
            StatTile(uiState.totalPass, "PASS", VettyColors.Pass, Modifier.weight(1f))
            StatTile(uiState.totalFail, "FAIL", VettyColors.Fail, Modifier.weight(1f))
            StatTile(uiState.totalNoSchema, "NO SCHEMA", VettyColors.NoSchema, Modifier.weight(1f))
        }

        Spacer(
            Modifier
                .height(1.dp)
                .fillMaxWidth()
                .background(VettyColors.SurfaceBorder),
        )

        // ── Filter chips ───────────────────────────────────────────────────────
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(VettyColors.SurfaceElevated)
                    .padding(horizontal = VettySpacing.lg, vertical = VettySpacing.sm),
            horizontalArrangement = Arrangement.spacedBy(VettySpacing.sm),
        ) {
            FilterChip("All", uiState.filterStatus == null, { onSetFilter(null) })
            FilterChip(
                "Pass",
                uiState.filterStatus == VettyStatus.PASS,
                { onSetFilter(VettyStatus.PASS) },
            )
            FilterChip(
                "Fail",
                uiState.filterStatus == VettyStatus.FAIL,
                { onSetFilter(VettyStatus.FAIL) },
            )
            FilterChip(
                "No Schema",
                uiState.filterStatus == VettyStatus.NO_SCHEMA,
                { onSetFilter(VettyStatus.NO_SCHEMA) },
            )
        }

        // ── Search ─────────────────────────────────────────────────────────────
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(VettyColors.SurfaceElevated)
                    .padding(horizontal = VettySpacing.lg, vertical = VettySpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("⌕  ", color = VettyColors.TextSecondary, style = VettyTypography.codeMedium)
            BasicTextField(
                value = uiState.searchQuery,
                onValueChange = onSetSearch,
                singleLine = true,
                textStyle = VettyTypography.bodySmall.copy(color = VettyColors.TextPrimary),
                decorationBox = { inner ->
                    if (uiState.searchQuery.isEmpty()) {
                        Text(
                            "Filter routes…",
                            style = VettyTypography.bodySmall,
                            color = VettyColors.TextTertiary,
                        )
                    }
                    inner()
                },
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(
            Modifier
                .height(1.dp)
                .fillMaxWidth()
                .background(VettyColors.SurfaceBorder),
        )

        // ── List ───────────────────────────────────────────────────────────────
        if (uiState.filtered.isEmpty()) {
            EmptyState(modifier = Modifier.fillMaxSize())
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = VettySpacing.sm),
            ) {
                items(items = uiState.filtered, key = { it.id }) { event ->
                    RequestRow(
                        event = event,
                        onClick = { onSelectEvent(event) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun RequestRow(
    event: VettyEvent,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val statusColor = event.displayStatus.toColor()

    Row(
        modifier =
            modifier
                .clickable(onClick = onClick)
                .background(VettyColors.Surface)
                .border(0.5.dp, VettyColors.SurfaceBorder, RoundedCornerShape(0.dp)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Status gutter stripe
        Box(
            modifier =
                Modifier
                    .width(3.dp)
                    .height(56.dp)
                    .background(statusColor),
        )

        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .padding(horizontal = VettySpacing.md, vertical = VettySpacing.sm),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(VettySpacing.sm),
            ) {
                MethodBadge(event.method)
                Text(
                    text = event.route,
                    style = VettyTypography.codeMedium,
                    color = VettyColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(3.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(VettySpacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = timeFmt.format(Date(event.timestamp)),
                    style = VettyTypography.labelSmall,
                    color = VettyColors.TextTertiary,
                )
                Text(
                    text = "${event.durationMs}ms",
                    style = VettyTypography.labelSmall,
                    color = VettyColors.TextTertiary,
                )
                Text(
                    text = "${event.statusCode}",
                    style = VettyTypography.labelSmall,
                    color =
                        if (event.statusCode in 200..299) {
                            VettyColors.Pass.copy(alpha = 0.8f)
                        } else {
                            VettyColors.Fail.copy(alpha = 0.8f)
                        },
                )
            }
        }

        StatusBadge(
            status = event.displayStatus,
            modifier = Modifier.padding(end = VettySpacing.md),
        )
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "◎",
            style =
                VettyTypography.title.copy(
                    fontSize =
                        TextUnit(
                            36f,
                            TextUnitType.Sp,
                        ),
                ),
            color = VettyColors.TextTertiary,
        )
        Spacer(Modifier.height(VettySpacing.sm))
        Text("No requests yet", style = VettyTypography.title, color = VettyColors.TextSecondary)
        Text(
            "Make a network call to see it here",
            style = VettyTypography.bodySmall,
            color = VettyColors.TextTertiary,
        )
    }
}
