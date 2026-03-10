package app.thamani.vetty.ui.common.presenter.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import app.thamani.vetty.core.models.VettyStatus

@Composable
fun MethodBadge(
    method: String,
    modifier: Modifier = Modifier,
) {
    val color =
        when (method.uppercase()) {
            "GET" -> VettyColors.MethodGet
            "POST" -> VettyColors.MethodPost
            "PUT" -> VettyColors.MethodPut
            "DELETE" -> VettyColors.MethodDelete
            "PATCH" -> VettyColors.MethodPatch
            else -> VettyColors.NoSchema
        }
    Box(
        modifier =
            modifier
                .clip(RoundedCornerShape(4.dp))
                .background(color.copy(alpha = 0.18f))
                .border(1.dp, color.copy(alpha = 0.45f), RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = method.uppercase(), style = VettyTypography.labelSmall, color = color)
    }
}

@Composable
fun StatusDot(
    status: VettyStatus,
    modifier: Modifier = Modifier,
) {
    val color = status.toColor()
    Box(
        modifier =
            modifier
                .size(8.dp)
                .clip(RoundedCornerShape(50))
                .background(color),
    )
}

@Composable
fun StatusBadge(
    status: VettyStatus,
    modifier: Modifier = Modifier,
) {
    val label =
        when (status) {
            VettyStatus.PASS -> "PASS"
            VettyStatus.FAIL -> "FAIL"
            VettyStatus.NO_SCHEMA -> "NO SCHEMA"
        }
    val fg = status.toColor()
    val bg = status.toDimColor()

    Box(
        modifier =
            modifier
                .clip(RoundedCornerShape(4.dp))
                .background(bg)
                .border(1.dp, fg.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        Text(text = label, style = VettyTypography.labelSmall, color = fg)
    }
}

@Composable
fun VettyTopBar(
    title: String,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(VettyColors.SurfaceElevated)
                .border(1.dp, VettyColors.SurfaceBorder, RoundedCornerShape(0.dp))
                .padding(horizontal = VettySpacing.lg),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = title,
            style = VettyTypography.title,
            color = VettyColors.TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(VettySpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            content = actions,
        )
    }
}

/** Coloured 3dp left-border gutter used in the JSON line viewer. */
@Composable
fun LineGutter(
    status: VettyStatus,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .width(3.dp)
                .fillMaxHeight()
                .background(status.toColor()),
    )
}

/** Stat summary tile: large count number + small label underneath. */
@Composable
fun StatTile(
    count: Int,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .clip(RoundedCornerShape(8.dp))
                .background(color.copy(alpha = 0.1f))
                .border(1.dp, color.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                .padding(horizontal = VettySpacing.md, vertical = VettySpacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = count.toString(),
            style = VettyTypography.title.copy(fontSize = TextUnit(20f, TextUnitType.Sp)),
            color = color,
        )
        Text(
            text = label,
            style = VettyTypography.labelSmall,
            color = color.copy(alpha = 0.75f),
        )
    }
}

/** Active/inactive filter pill used in the list toolbar. */
@Composable
fun FilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bg = if (selected) VettyColors.Accent.copy(alpha = 0.2f) else VettyColors.SurfaceCard
    val border = if (selected) VettyColors.Accent else VettyColors.SurfaceBorder
    val text = if (selected) VettyColors.Accent else VettyColors.TextSecondary

    Box(
        modifier =
            modifier
                .clip(RoundedCornerShape(20.dp))
                .background(bg)
                .border(1.dp, border, RoundedCornerShape(20.dp))
                .clickable(onClick = onClick)
                .padding(horizontal = VettySpacing.md, vertical = 4.dp),
    ) {
        Text(label, style = VettyTypography.labelSmall, color = text)
    }
}

// ─── _root_ide_package_.app.thamani.vetty.core.models.VettyStatus colour helpers ───────────────────────────────────────────────

fun VettyStatus.toColor(): Color =
    when (this) {
        VettyStatus.PASS -> VettyColors.Pass
        VettyStatus.FAIL -> VettyColors.Fail
        VettyStatus.NO_SCHEMA -> VettyColors.NoSchema
    }

fun VettyStatus.toDimColor(): Color =
    when (this) {
        VettyStatus.PASS -> VettyColors.PassDim
        VettyStatus.FAIL -> VettyColors.FailDim
        VettyStatus.NO_SCHEMA -> VettyColors.NoSchemaDim
    }
