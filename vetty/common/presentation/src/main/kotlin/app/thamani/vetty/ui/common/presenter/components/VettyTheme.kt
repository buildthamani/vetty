package app.thamani.vetty.ui.common.presenter.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─── Colours ──────────────────────────────────────────────────────────────────

object VettyColors {
    // ── Surface layers (darkest → lightest elevation) ─────────────────────────
    val Surface = Color(0xFF0D0F14) // base background
    val SurfaceElevated = Color(0xFF161A23) // top bars, toolbars
    val SurfaceCard = Color(0xFF1C2130) // cards, chips
    val SurfaceOverlay = Color(0xFF222840) // modal sheets, popups
    val SurfaceBorder = Color(0xFF252C3D) // dividers, outlines

    // ── Validation status ─────────────────────────────────────────────────────
    val Pass = Color(0xFF36E87C)
    val PassDim = Color(0xFF1A4D33) // muted background tint for pass rows
    val PassSubtle = Color(0xFF0F2E1E) // very faint tint for gutter on passing lines

    val Fail = Color(0xFFFF4B6E)
    val FailDim = Color(0xFF4D1A23) // muted background tint for fail rows
    val FailSubtle = Color(0xFF2E0F15) // very faint tint for gutter on non-violated lines

    val NoSchema = Color(0xFF8B93A8)
    val NoSchemaDim = Color(0xFF252C3D)

    // ── Text ──────────────────────────────────────────────────────────────────
    val TextPrimary = Color(0xFFF0F2F8) // main readable content
    val TextSecondary = Color(0xFF8B93A8) // supporting labels, metadata
    val TextTertiary = Color(0xFF505869) // de-emphasised — line numbers, timestamps
    val TextDisabled = Color(0xFF313749) // placeholder / empty state
    val TextCode = Color(0xFFABD4FF) // inline code snippets outside JSON viewer

    // ── Accent ────────────────────────────────────────────────────────────────
    val Accent = Color(0xFF4D7CFE) // selected state, active chips, links
    val AccentDim = Color(0xFF1A2C5E) // accent background tint
    val AccentPressed = Color(0xFF3A63D4) // ripple / pressed feedback

    // ── JSON syntax tokens ────────────────────────────────────────────────────
    val JsonKey = Color(0xFF88B4FF) // object key strings
    val JsonString = Color(0xFF98E06B) // string values
    val JsonNumber = Color(0xFFFFB347) // integer / float values
    val JsonBoolean = Color(0xFFFF7BAC) // true / false
    val JsonNull = Color(0xFF8B93A8) // null
    val JsonBrace = Color(0xFFD0D6E8) // { } [ ] , :

    // ── HTTP method badge colours ─────────────────────────────────────────────
    val MethodGet = Color(0xFF36E87C)
    val MethodPost = Color(0xFF4D7CFE)
    val MethodPut = Color(0xFFFFB347)
    val MethodDelete = Color(0xFFFF4B6E)
    val MethodPatch = Color(0xFFBD7DFF)
    val MethodHead = Color(0xFF8B93A8)
    val MethodOptions = Color(0xFF8B93A8)
    val MethodUnknown = Color(0xFF505869)
}

// ─── Typography ───────────────────────────────────────────────────────────────

object VettyTypography {
    // ── Monospace — JSON viewer, URLs, raw values ─────────────────────────────

    /** Compact code text — JSON line content, 11sp. */
    val codeSmall =
        TextStyle(
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.Normal,
        )

    /** Standard code text — request URLs, body snippets, 13sp. */
    val codeMedium =
        TextStyle(
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Normal,
        )

    /** Bold code emphasis — highlighted violation paths. */
    val codeMediumBold =
        TextStyle(
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Bold,
        )

    // ── Sans-serif — UI chrome, labels, metadata ──────────────────────────────

    /**
     * Tiny uppercase label — badge text, column headers, rule names.
     * Has tracking applied for readability at small sizes.
     */
    val labelSmall =
        TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontSize = 10.sp,
            lineHeight = 14.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.8.sp,
        )

    /** Medium label — section titles, filter chip text, stat tile labels. */
    val labelMedium =
        TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.2.sp,
        )

    /** Small body copy — violation descriptions, metadata rows. */
    val bodySmall =
        TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontSize = 12.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.Normal,
        )

    /** Standard body copy — list item secondary text, descriptions. */
    val bodyMedium =
        TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Normal,
        )

    /** Primary title — top bar title, screen headings. */
    val title =
        TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Bold,
        )

    /** Large heading — stat tile numbers, prominent counts. */
    val heading =
        TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontSize = 20.sp,
            lineHeight = 26.sp,
            fontWeight = FontWeight.Bold,
        )

    /** Empty state headline. */
    val headingLarge =
        TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontSize = 28.sp,
            lineHeight = 34.sp,
            fontWeight = FontWeight.ExtraBold,
        )
}

// ─── Spacing ──────────────────────────────────────────────────────────────────

/**
 * Consistent spacing scale used for padding, margins, and gaps.
 * Always use these values rather than hardcoded dp literals so the
 * UI can be scaled uniformly if needed.
 */
object VettySpacing {
    val xxs: Dp = 2.dp
    val xs: Dp = 4.dp
    val sm: Dp = 8.dp
    val md: Dp = 12.dp
    val lg: Dp = 16.dp
    val xl: Dp = 24.dp
    val xxl: Dp = 32.dp
    val xxxl: Dp = 48.dp
}

// ─── Shapes ───────────────────────────────────────────────────────────────────

/**
 * Corner radius tokens.  Use these for `clip()`, `border()`, and `shape =`
 * parameters throughout the UI so radii are changed in one place.
 */
object VettyShapes {
    /** Tags, badges, rule chips — tight 4dp radius. */
    val badge: Shape = RoundedCornerShape(4.dp)

    /** Filter chips, small pills — 20dp fully rounded. */
    val chip: Shape = RoundedCornerShape(20.dp)

    /** Cards, stat tiles — 8dp radius. */
    val card: Shape = RoundedCornerShape(8.dp)

    /** Side-drawer panel top-start / bottom-start rounded edge. */
    val drawer: Shape = RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp)

    /** Bottom sheet — top corners only. */
    val sheet: Shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)

    /** Fully square — used for full-screen panels where no rounding is needed. */
    val none: Shape = RoundedCornerShape(0.dp)

    /** Fully circular — status dot. */
    val circle: Shape = RoundedCornerShape(50)
}

// ─── MaterialTheme wrapper ────────────────────────────────────────────────────

/**
 * Wraps content in a [MaterialTheme] configured with Vetty's dark colour scheme.
 *
 * Apply this at the root of every Vetty composable tree so that Material3
 * components (Text, Surface, etc.) inherit the correct colours automatically.
 *
 * ```kotlin
 * // In VettyActivity:
 * setContent {
 *     VettyTheme {
 *         VettyPanel(modifier = Modifier.fillMaxSize())
 *     }
 * }
 *
 * // In VettyOverlay:
 * VettyTheme {
 *     Box(...) { VettyPanel(...) }
 * }
 * ```
 */
@Composable
fun VettyTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme =
            darkColorScheme(
                background = VettyColors.Surface,
                surface = VettyColors.SurfaceElevated,
                surfaceVariant = VettyColors.SurfaceCard,
                outline = VettyColors.SurfaceBorder,
                primary = VettyColors.Accent,
                onPrimary = VettyColors.TextPrimary,
                onBackground = VettyColors.TextPrimary,
                onSurface = VettyColors.TextPrimary,
                onSurfaceVariant = VettyColors.TextSecondary,
                error = VettyColors.Fail,
                onError = VettyColors.TextPrimary,
            ),
        content = content,
    )
}
