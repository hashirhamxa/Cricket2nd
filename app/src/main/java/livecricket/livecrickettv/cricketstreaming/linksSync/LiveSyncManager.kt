package livecricket.livecrickettv.cricketstreaming.linksSync

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap


data class EventSyncSignal(
    val eventId: Int,
    val syncToken: String,
    val receivedAt: Long = System.currentTimeMillis()
)

object LiveSyncManager {

    private val _syncEvents = MutableSharedFlow<EventSyncSignal>(extraBufferCapacity = 16)
    val syncEvents: SharedFlow<EventSyncSignal> = _syncEvents.asSharedFlow()

    private val eventMutexes = ConcurrentHashMap<Int, Mutex>()

    fun notifyEventSync(eventId: Int, syncToken: String) {
        _syncEvents.tryEmit(EventSyncSignal(eventId = eventId, syncToken = syncToken))
    }

    private fun getMutex(eventId: Int): Mutex {
        return eventMutexes.computeIfAbsent(eventId) { Mutex() }
    }

    /**
     * Executes the sync block ensuring that concurrent sync requests for the same eventId
     * are serialized and deduplicated safely via Mutex.
     */
    suspend fun <T> runWithDeduplication(
        eventId: Int,
        block: suspend () -> T
    ): T {
        val mutex = getMutex(eventId)
        return mutex.withLock {
            block()
        }
    }
}