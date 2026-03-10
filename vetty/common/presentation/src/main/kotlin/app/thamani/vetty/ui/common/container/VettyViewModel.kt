package app.thamani.vetty.ui.common.container

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.thamani.vetty.core.Vetty
import app.thamani.vetty.core.models.ValidationResult
import app.thamani.vetty.core.models.VettyEvent
import app.thamani.vetty.core.models.VettyStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class VettyUiState(
    val events: List<VettyEvent> = emptyList(),
    val selectedEvent: VettyEvent? = null,
    val filterStatus: VettyStatus? = null,
    val searchQuery: String = "",
    val totalPass: Int = 0,
    val totalFail: Int = 0,
    val totalNoSchema: Int = 0,
) {
    val filtered: List<VettyEvent>
        get() =
            events
                .let { list ->
                    if (filterStatus != null) list.filter { it.displayStatus == filterStatus } else list
                }.let { list ->
                    if (searchQuery.isBlank()) {
                        list
                    } else {
                        list.filter {
                            it.route.contains(searchQuery, ignoreCase = true) ||
                                it.requestUrl.contains(searchQuery, ignoreCase = true)
                        }
                    }
                }
}

class VettyViewModel : ViewModel() {
    private val mFilter = MutableStateFlow<VettyStatus?>(null)
    private val mSearch = MutableStateFlow("")
    private val mSelected = MutableStateFlow<VettyEvent?>(null)

    /**
     * Observe the datasource directly — no bus, no extra indirection.
     * [Vetty.dataSource] is the single source of truth.  When the dataSource
     * is swapped (e.g. Room vs in-memory) the UI automatically follows.
     */
    val state: StateFlow<VettyUiState> =
        combine(
            Vetty.dataSource.observeAll(),
            mFilter,
            mSearch,
            mSelected,
        ) { events, filter, search, selected ->
            VettyUiState(
                events = events,
                selectedEvent = selected,
                filterStatus = filter,
                searchQuery = search,
                totalPass = events.count { it.validationResult is ValidationResult.Pass },
                totalFail = events.count { it.validationResult is ValidationResult.Fail },
                totalNoSchema = events.count { it.validationResult is ValidationResult.NoSchema },
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = VettyUiState(),
        )

    fun selectEvent(event: VettyEvent?) {
        mSelected.value = event
    }

    fun setFilter(status: VettyStatus?) {
        mFilter.value = status
    }

    fun setSearch(query: String) {
        mSearch.value = query
    }

    fun clearAll() {
        Vetty.dataSource.clear()
        mSelected.value = null
    }
}
