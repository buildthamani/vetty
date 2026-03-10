package app.thamani.vetty.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import app.thamani.vetty.ui.common.container.VettyViewModel
import app.thamani.vetty.ui.common.presenter.VettyPanel
import app.thamani.vetty.ui.common.presenter.components.VettyColors

/**
 * Renders the Vetty inspector as a composable that slides in over your existing
 * UI — no separate Activity, no Intent, no context needed.
 *
 * ## Minimal setup
 * ```kotlin
 * Box(modifier = Modifier.fillMaxSize()) {
 *     YourApp()
 *     VettyOverlay()
 * }
 * ```
 *
 * ## Controlled visibility
 * ```kotlin
 * var showVetty by remember { mutableStateOf(false) }
 *
 * Box(modifier = Modifier.fillMaxSize()) {
 *     YourApp()
 *     VettyOverlay(
 *         visible   = showVetty,
 *         onDismiss = { showVetty = false },
 *         fraction  = 0.9f   // side-drawer feel
 *     )
 * }
 * ```
 *
 * Flip [visible] from a debug shake detector, a floating button, or a
 * notification action — anything that makes sense in your debug workflow.
 *
 * @param visible   Whether the overlay is currently shown.
 * @param onDismiss Called when the user performs a back gesture or any action
 *                  that should close the panel.  If null, no dismiss affordance
 *                  is provided — useful when visible is always true during dev.
 * @param fraction  Fraction of the screen width the panel occupies.
 *                  1f = full screen, 0.85f = side drawer with a peek of the app.
 * @param vm        Shared [VettyViewModel].  Defaults to the nearest
 *                  [ViewModelStoreOwner] so it survives recomposition.
 */
@Composable
fun Vetty(
    visible: Boolean = true,
    onDismiss: (() -> Unit)? = null,
    fraction: Float = 1f,
) {
    AnimatedVisibility(
        visible = visible,
        enter =
            slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)) +
                fadeIn(tween(300)),
        exit =
            slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(250)) +
                fadeOut(tween(250)),
    ) {
        val topPadding = WindowInsets.statusBars.asPaddingValues()
        val bottomPadding = WindowInsets.navigationBars.asPaddingValues()
        Box(
            contentAlignment = Alignment.CenterEnd,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(topPadding)
                    .padding(bottomPadding),
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraction)
                        .clip(
                            if (fraction < 1f) {
                                RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp)
                            } else {
                                RoundedCornerShape(0.dp)
                            },
                        ).background(VettyColors.Surface),
            ) {
                VettyPanel(modifier = Modifier.fillMaxSize())
            }
        }
    }
}
