package livecricket.livecrickettv.cricketstreaming.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlin.random.Random
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import livecricket.livecrickettv.cricketstreaming.database.HighlightEntity
import livecricket.livecrickettv.cricketstreaming.database.LinkEntity
import livecricket.livecrickettv.cricketstreaming.database.StreamingEntity
import livecricket.livecrickettv.cricketstreaming.linksSync.LiveSyncManager
import livecricket.livecrickettv.cricketstreaming.network.AppRepository
import javax.inject.Inject

@HiltViewModel
class LinksViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {

    private val _links = MutableStateFlow<List<LinkEntity>>(emptyList())
    val links: StateFlow<List<LinkEntity>> = _links

    private val _highlights = MutableStateFlow<List<HighlightEntity>>(emptyList())
    val highlights: StateFlow<List<HighlightEntity>> = _highlights

    private val _streaming = MutableStateFlow<StreamingEntity?>(null)
    val streaming: StateFlow<StreamingEntity?> = _streaming

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing
    private var liveSyncJob: Job? = null

    init {
        loadStreamingData()
    }

    private fun loadStreamingData() {
        viewModelScope.launch {
            repository.getAppFlow().collectLatest { app ->
                if (app != null) {
                    repository.getStreamingDataFlow(app.id).collectLatest { list ->
                        if (list.isNotEmpty()) {
                            _streaming.value = list[0].streaming
                        }
                    }
                }
            }
        }
    }

    fun refresh(eventId: Int, isHighlights: Boolean) {
        viewModelScope.launch {
            _isRefreshing.value = true
            if (isHighlights) {
                repository.fetchAndSaveConfig { _, _ ->
                    _isRefreshing.value = false
                }
            } else {
                repository.fetchAndSyncEventLinks(eventId, null)
                _isRefreshing.value = false
            }
        }
    }



    /**
     * Listens to LiveSyncManager fast path for this specific eventId.
     * Applies jitter delay (250ms - 5000ms) to smooth traffic spikes across active clients.
     */
    fun observeLiveSync(eventId: Int) {
        if (liveSyncJob?.isActive == true) return
        liveSyncJob = viewModelScope.launch {
            LiveSyncManager.syncEvents.collect { signal ->
                if (signal.eventId == eventId) {
                    val jitterMs = Random.nextLong(250L, 5000L)
                    delay(jitterMs)
                    repository.fetchAndSyncEventLinks(eventId, signal.syncToken)
                }
            }
        }
    }

    /**
     * Screen-entry recovery: checks for pending invalidations or performs
     * a 2-minute staleness freshness check.
     */
    fun checkStalenessAndRecover(eventId: Int) {
        viewModelScope.launch {
            val pending = repository.getPendingSyncForEvent(eventId)
            if (pending != null) {
                repository.fetchAndSyncEventLinks(eventId, pending.syncToken)
                return@launch
            }

            val lastSync = repository.getLastSyncTimestamp(eventId)
            val now = System.currentTimeMillis()
            if (lastSync == null || (now - lastSync) > 120_000L) { // 2 minutes
                repository.fetchAndSyncEventLinks(eventId, null)
            }
        }
    }
    fun loadLinks(eventId: Int) {
        viewModelScope.launch {
            repository.getLinksForEventFlow(eventId).collectLatest { linkList ->
                _links.value = linkList
            }
        }
    }

    fun loadHighlights(eventId: Int) {
        viewModelScope.launch {
            repository.getHighlightsForEventFlow(eventId).collectLatest { highlightList ->
                _highlights.value = highlightList
            }
        }
    }
}
