package livecricket.livecrickettv.cricketstreaming.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import livecricket.livecrickettv.cricketstreaming.network.AppRepository
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {

    private val _showHighlights = MutableStateFlow(true)
    val showHighlights: StateFlow<Boolean> = _showHighlights

    init {
        observeHighlightsVisibility()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeHighlightsVisibility() {
        viewModelScope.launch {
            repository.getAppFlow().flatMapLatest { app ->
                if (app != null) {
                    repository.getStreamingDataFlow(app.id)
                } else {
                    flowOf(emptyList())
                }
            }.collectLatest { streamingDataList ->
                if (streamingDataList.isNotEmpty()) {
                    val streaming = streamingDataList[0].streaming
                    _showHighlights.value = streaming.showCricketHighlights == true ||
                            streaming.showFootballHighlights == true ||
                            streaming.showOtherSportsHighlights == true
                }
            }
        }
    }
}
