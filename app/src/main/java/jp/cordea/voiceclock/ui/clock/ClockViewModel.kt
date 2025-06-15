package jp.cordea.voiceclock.ui.clock

import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.cordea.voiceclock.GetTtsStateUseCase
import jp.cordea.voiceclock.TimerServiceProvider
import jp.cordea.voiceclock.TtsState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class ClockViewModel @Inject constructor(
    getTtsStateUseCase: GetTtsStateUseCase,
    timerServiceProvider: TimerServiceProvider,
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

    private val request = Channel<Boolean>()

    init {
        viewModelScope.launch {
            while (true) {
                delay(1000L)
                val calendar = Calendar.getInstance()
                val now = FORMAT.format(calendar.time)
                time.value = now
            }
        }
        request
            .consumeAsFlow()
            .flatMapLatest { start ->
                timerServiceProvider.get()
                    .map { start to it }
            }
            .onEach { (start, service) ->
                if (start) {
                    service.startClock(value.value, unit.value)
                    state.value = TimerState.STARTED
                } else {
                    service.stop()
                    state.value = TimerState.STOPPED
                }
            }
            .launchIn(viewModelScope)
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
            request.trySend(false)
            return
        }
        showController.value = true
    }

    fun onPlayClicked() {
        showController.value = false
        request.trySend(true)
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

    override fun onCleared() {
        super.onCleared()
        request.cancel()
    }
}
