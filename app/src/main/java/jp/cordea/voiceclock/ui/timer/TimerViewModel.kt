package jp.cordea.voiceclock.ui.timer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.cordea.voiceclock.GetTtsStateUseCase
import jp.cordea.voiceclock.TtsState
import jp.cordea.voiceclock.ui.clock.ClockUnit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class TimerViewModel @Inject constructor(
    getTtsStateUseCase: GetTtsStateUseCase
) : ViewModel() {
    private val remaining = MutableStateFlow("")
    private val showController = MutableStateFlow(false)
    private val isValueExpanded = MutableStateFlow(false)
    private val isUnitExpanded = MutableStateFlow(false)
    private val value = MutableStateFlow(1)
    private val unit = MutableStateFlow(ClockUnit.MINUTE)
    private val isStarted = MutableStateFlow(false)

    val uiState =
        combine(
            remaining,
            showController,
            combine(
                isValueExpanded,
                isUnitExpanded
            ) { isValueExpanded, isUnitExpanded ->
                isValueExpanded to isUnitExpanded
            },
            isStarted,
            getTtsStateUseCase.execute()
        ) { remaining, showController, expanded, isStarted, state ->
            TimerUiState(
                remaining,
                state,
                showController,
                expanded.first,
                expanded.second,
                unit.value,
                value.value,
                isStarted
            )
        }.stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            TimerUiState(
                "",
                TtsState.LOADING,
                showController = false,
                isValueExpanded = false,
                isUnitExpanded = false,
                ClockUnit.MINUTE,
                1,
                isStarted = false
            )
        )

    fun onFabClicked() {
        showController.value = true
    }

    fun onPlayClicked() {
        showController.value = false
        isStarted.value = true
    }

    fun onValueExpandChanged(it: Boolean) {
        isValueExpanded.value = it
    }

    fun onValueChanged(it: Int) {
        value.value = it
    }

    fun onUnitExpandChanged(it: Boolean) {
        isUnitExpanded.value = it
    }

    fun onUnitChanged(it: ClockUnit) {
        unit.value = it
    }

    fun onDismissController() {
        showController.value = false
    }
}
