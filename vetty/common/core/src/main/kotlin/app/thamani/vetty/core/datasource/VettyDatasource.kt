package app.thamani.vetty.core.datasource

import app.thamani.vetty.core.models.ValidationResult
import app.thamani.vetty.core.models.VettyEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/**
 * Persistence contract for Vetty events.
 *
 * The default implementation [InMemoryVettyDataSource] keeps everything in a
 * StateFlow — zero dependencies, works on JVM and Android alike.
 *
 * For longer-lived persistence across process restarts you can supply your own
 * implementation backed by Room, SQLDelight, or any other store:
 *
 * ```kotlin
 * class RoomVettyDataSource(private val dao: VettyEventDao) : VettyDataSource {
 *     override fun save(event: VettyEvent) {
 *         // dao.insert(event.toEntity())  — call from a coroutine scope you own
 *     }
 *     override fun observeAll(): Flow<List<VettyEvent>> = dao.observeAll().map { ... }
 *     override fun getAll(): List<VettyEvent> = dao.getAll().map { ... }
 *     override fun find(id: Long): VettyEvent? = dao.find(id)?.toDomain()
 *     override fun clear() = dao.deleteAll()
 * }
 * ```
 *
 * Then pass it during init:
 * ```kotlin
 * Vetty.init {
 *     dataSource = RoomVettyDataSource(db.vettyEventDao())
 * }
 * ```
 */
interface VettyDataSource {
    /** Persist a single event. Called on the thread that processed the response — must be fast. */
    fun save(event: VettyEvent)

    /** Observe all events as a reactive stream, newest first. */
    fun observeAll(): Flow<List<VettyEvent>>

    /** Observe only failed validation events. */
    fun observeFailed(): Flow<List<VettyEvent>>

    /** Synchronous snapshot — useful in tests or non-Compose contexts. */
    fun getAll(): List<VettyEvent>

    /** Retrieve a single event by its [VettyEvent.id]. */
    fun find(id: Long): VettyEvent?

    /** Wipe all stored events. */
    fun clear()
}

/**
 * Default in-memory implementation.
 *
 * Backed by a [MutableStateFlow] so any Compose or coroutine observer gets
 * incremental updates without polling.  Capped at [maxEvents] entries to avoid
 * unbounded memory growth during long-running debug sessions.
 */
internal class InMemoryVettyDataSource(
    private val maxEvents: Int = 500,
) : VettyDataSource {
    private val mutableEvents = MutableStateFlow<List<VettyEvent>>(emptyList())

    override fun save(event: VettyEvent) {
        mutableEvents.update { current ->
            val updated = listOf(event) + current // newest first
            if (updated.size > maxEvents) updated.take(maxEvents) else updated
        }
    }

    override fun observeAll(): Flow<List<VettyEvent>> = mutableEvents.asStateFlow()

    override fun observeFailed(): Flow<List<VettyEvent>> =
        mutableEvents.map { list ->
            list.filter { it.validationResult is ValidationResult.Fail }
        }

    override fun getAll(): List<VettyEvent> = mutableEvents.value

    override fun find(id: Long): VettyEvent? = mutableEvents.value.firstOrNull { it.id == id }

    override fun clear() {
        mutableEvents.value = emptyList()
    }
}
