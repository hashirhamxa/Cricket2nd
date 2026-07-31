package livecricket.livecrickettv.cricketstreaming.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import livecricket.livecrickettv.cricketstreaming.database.AppEntity
import livecricket.livecrickettv.cricketstreaming.network.AppRepository
import javax.inject.Inject

/**
 * ViewModel for the Settings screen.
 * Observes the application configuration to reactively update settings-related data.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {

    private val _appConfig = MutableStateFlow<AppEntity?>(null)
    val appConfig: StateFlow<AppEntity?> = _appConfig

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    init {
        observeSettings()
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            repository.fetchAndSaveConfig { _, _ ->
                _isRefreshing.value = false
            }
        }
    }

    private fun observeSettings() {
        viewModelScope.launch {
            repository.getAppFlow().collectLatest { app ->
                _appConfig.value = app
            }
        }
    }
}
