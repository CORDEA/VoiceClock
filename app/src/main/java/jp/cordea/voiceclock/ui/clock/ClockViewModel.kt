package jp.cordea.voiceclock.ui.clock

import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.cordea.voiceclock.GetTtsStateUseCase
import jp.cordea.voiceclock.TtsState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ClockViewModel @Inject constructor(
    getTtsStateUseCase: GetTtsStateUseCase,
) : ViewModel() {
    companion object {
        private val FORMAT = SimpleDateFormat.getTimeInstance(SimpleDateFormat.MEDIUM)
    }

    private val time = MutableStateFlow(FORMAT.format(Date()))
    private val showController = MutableStateFlow(false)
    private val isValueExpanded = MutableStateFlow(false)
    private val isUnitExpanded = MutableStateFlow(false)
    private val value = MutableStateFlow(1)
    private val unit = MutableStateFlow(ClockUnit.MINUTE)
    private val state = MutableStateFlow(TimerState.IDLE)

    init {
        viewModelScope.launch {
            while (true) {
                delay(1000L)
                val calendar = Calendar.getInstance()
                val now = FORMAT.format(calendar.time)
                time.value = now
            }
        }
    }

    val uiState =
        combine(
            time,
            showController,
            combine(
                isValueExpanded,
                isUnitExpanded
            ) { isValueExpanded, isUnitExpanded ->
                isValueExpanded to isUnitExpanded
            },
            combine(
                getTtsStateUseCase.execute(),
                state,
            ) { ttsState, state ->
                ttsState to state
            }
        ) { time, showController, expanded, state ->
            ClockUiState(
                time,
                state.first,
                showController,
                expanded.first,
                expanded.second,
                unit.value,
                value.value,
                state.second
            )
        }.stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            ClockUiState(
                "",
                TtsState.LOADING,
                showController = false,
                isValueExpanded = false,
                isUnitExpanded = false,
                ClockUnit.MINUTE,
                1,
                TimerState.IDLE
            )
        )

    fun onFabClicked() {
        if (state.value == TimerState.STARTED) {
            state.value = TimerState.STOPPING
            return
        }
        showController.value = true
    }

    fun onPlayClicked() {
        showController.value = false
        state.value = TimerState.STARTING
    }

    fun onTimerCalled() {
        if (state.value == TimerState.STARTING) {
            state.value = TimerState.STARTED
        }
        if (state.value == TimerState.STOPPING) {
            state.value = TimerState.STOPPED
        }
    }

    fun onValueExpandChanged(it: Boolean) {
        isValueExpanded.value = it
    }

    fun onValueChanged(it: Int) {
        value.value = it
        isValueExpanded.value = false
    }

    fun onUnitExpandChanged(it: Boolean) {
        isUnitExpanded.value = it
    }

    fun onUnitChanged(it: ClockUnit) {
        unit.value = it
        isUnitExpanded.value = false
    }

    fun onDismissController() {
        showController.value = false
    }
}
