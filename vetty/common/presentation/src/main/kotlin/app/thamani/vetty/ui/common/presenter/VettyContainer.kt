package app.thamani.vetty.ui.common.presenter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import app.thamani.vetty.ui.common.container.VettyViewModel
import app.thamani.vetty.ui.common.presenter.screens.RequestScreen
import app.thamani.vetty.ui.common.presenter.screens.RequestsScreen

/**
 * Root navigation composable shared between [vetty-ui:coupled] and [vetty-ui:decoupled].
 *
 * Owns no navigation back-stack — toggles between the list and detail views by
 * updating [VettyViewModel.selectEvent]. This keeps the logic identical whether
 * Vetty is rendered as an overlay or inside a dedicated Activity.
 */
@Composable
fun VettyPanel(
    modifier: Modifier = Modifier,
    vm: VettyViewModel = viewModel(),
) {
    val state by vm.state.collectAsState()

    if (state.selectedEvent != null) {
        RequestScreen(
            event = state.selectedEvent!!,
            onBack = { vm.selectEvent(null) },
            modifier = modifier,
        )
    } else {
        RequestsScreen(
            uiState = state,
            onSelectEvent = vm::selectEvent,
            onSetFilter = vm::setFilter,
            onSetSearch = vm::setSearch,
            onClearAll = vm::clearAll,
            modifier = modifier,
        )
    }
}
