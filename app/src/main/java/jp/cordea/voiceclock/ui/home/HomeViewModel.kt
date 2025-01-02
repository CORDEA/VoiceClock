package jp.cordea.voiceclock.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.cordea.voiceclock.CancelTtsUseCase
import jp.cordea.voiceclock.StartTtsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val startTtsUseCase: StartTtsUseCase,
    private val cancelTtsUseCase: CancelTtsUseCase
) : ViewModel() {
    private val isTtsRequired = MutableStateFlow(false)

    val uiState = isTtsRequired.map { HomeUiState(it) }.stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        HomeUiState(false)
    )

    fun onTtsReceived() {
        startTtsUseCase.execute()
    }

    fun onTtsCancelled() {
        cancelTtsUseCase.execute()
    }

    fun onTtsRequired() {
        isTtsRequired.value = true
    }

    fun onTtsRequested() {
        isTtsRequired.value = false
    }
}
