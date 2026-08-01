package livecricket.livecrickettv.cricketstreaming.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import livecricket.livecrickettv.cricketstreaming.database.MatchEntity
import livecricket.livecrickettv.cricketstreaming.network.MatchRepository
import javax.inject.Inject

@HiltViewModel
class ScoreDetailViewModel @Inject constructor(
    private val repository: MatchRepository
) : ViewModel() {

    private val _match = MutableStateFlow<MatchEntity?>(null)
    val match: StateFlow<MatchEntity?> = _match

    fun loadMatch(matchId: String) {
        viewModelScope.launch {
            _match.value = repository.getMatchById(matchId)
        }
    }
}
