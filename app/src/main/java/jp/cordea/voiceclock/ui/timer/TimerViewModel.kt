package jp.cordea.voiceclock.ui.timer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.cordea.voiceclock.GetTtsStateUseCase
import jp.cordea.voiceclock.TimerServiceProvider
import jp.cordea.voiceclock.TtsState
import jp.cordea.voiceclock.ui.clock.ClockUnit
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import java.time.Duration
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class TimerViewModel @Inject constructor(
    getTtsStateUseCase: GetTtsStateUseCase,
    timerServiceProvider: TimerServiceProvider,
) : ViewModel() {
    private val remaining = MutableStateFlow(Duration.ZERO)
    private val sweepAngle = MutableStateFlow(360f)
    private val showController = MutableStateFlow(false)
    private val isValueExpanded = MutableStateFlow(false)
    private val isUnitExpanded = MutableStateFlow(false)
    private val isHoursExpanded = MutableStateFlow(false)
    private val isMinutesExpanded = MutableStateFlow(false)
    private val isSecondsExpanded = MutableStateFlow(false)
    private val hours = MutableStateFlow(0)
    private val minutes = MutableStateFlow(0)
    private val seconds = MutableStateFlow(0)
    private val value = MutableStateFlow(1)
    private val unit = MutableStateFlow(ClockUnit.MINUTE)
    private val state = MutableStateFlow(TimerState.STOPPED)

    private val request = Channel<Duration>()

    init {
        timerServiceProvider.get()
            .flatMapLatest { service ->
                request
                    .receiveAsFlow()
                    .map { it to service }
            }
            .map { (duration, service) ->
                val total =
                    Duration.ofSeconds(hours.value * 3600L + minutes.value * 60L + seconds.value)
                Triple(duration, service, total)
            }
            .onEach { (duration, service) ->
                if (duration.isZero) {
                    service.stop()
                } else {
                    val timer = value.value *
                            when (unit.value) {
                                ClockUnit.HOUR -> 3600L
                                ClockUnit.MINUTE -> 60L
                                ClockUnit.SECOND -> 1L
                            }
                    service.start(duration, timer)
                }
            }
            .flatMapLatest { (_, service, total) ->
                service.timerChannel
                    .receiveAsFlow()
                    .map { it to total }
            }
            .onEach { (r, t) ->
                remaining.value = r
                sweepAngle.value = -(r.seconds / t.seconds.toFloat()) * 360f
                if (r.isZero) {
                    state.value = TimerState.STOPPED
                }
            }
            .launchIn(viewModelScope)
    }

    val uiState =
        combine(
            combine(
                isUnitExpanded,
                isValueExpanded,
                isHoursExpanded,
                isMinutesExpanded,
                isSecondsExpanded
            ) { unit, value, hours, minutes, seconds ->
                Expanded(unit, value, hours, minutes, seconds)
            },
            combine(
                unit,
                value,
                hours,
                minutes,
                seconds,
            ) { unit, value, hours, minutes, seconds ->
                Values(unit, value, hours, minutes, seconds)
            },
            combine(remaining, sweepAngle, state) { remaining, angle, state ->
                Triple(remaining, angle, state)
            },
            showController,
            getTtsStateUseCase.execute()
        ) { expanded, values, state, showController, ttsState ->
            TimerUiState(
                remaining = state.first,
                sweepAngle = state.second,
                ttsState = ttsState,
                showController = showController,
                isValueExpanded = expanded.isValueExpanded,
                isUnitExpanded = expanded.isUnitExpanded,
                isHoursExpanded = expanded.isHoursExpanded,
                isMinutesExpanded = expanded.isMinutesExpanded,
                isSecondsExpanded = expanded.isSecondsExpanded,
                hours = values.hours,
                minutes = values.minutes,
                seconds = values.seconds,
                unit = values.unit,
                value = values.value,
                state = state.third
            )
        }.stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            TimerUiState(
                Duration.ZERO,
                360f,
                TtsState.LOADING,
                showController = false,
                isValueExpanded = false,
                isUnitExpanded = false,
                isHoursExpanded = false,
                isMinutesExpanded = false,
                isSecondsExpanded = false,
                hours = 0,
                minutes = 0,
                seconds = 0,
                unit = ClockUnit.MINUTE,
                value = 1,
                state = TimerState.STOPPED
            )
        )

    fun onFabClicked() {
        if (state.value == TimerState.STARTED) {
            request.trySend(Duration.ZERO)
            state.value = TimerState.PAUSED
            return
        }
        if (state.value == TimerState.PAUSED) {
            request.trySend(remaining.value)
            state.value = TimerState.STARTED
            return
        }
        showController.value = true
    }

    fun onPlayClicked() {
        if (hours.value == 0 && minutes.value == 0 && seconds.value == 0) {
            return
        }
        showController.value = false
        state.value = TimerState.STARTED
        val duration = Duration.ofSeconds(hours.value * 3600L + minutes.value * 60L + seconds.value)
        request.trySend(duration)
    }

    fun onHoursChanged(it: Int) {
        hours.value = it
        isHoursExpanded.value = false
    }

    fun onMinutesChanged(it: Int) {
        minutes.value = it
        isMinutesExpanded.value = false
    }

    fun onSecondsChanged(it: Int) {
        seconds.value = it
        isSecondsExpanded.value = false
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
        value.value = 0
        isUnitExpanded.value = false
    }

    fun onDismissController() {
        showController.value = false
    }

    fun onHoursExpandChanged(it: Boolean) {
        isHoursExpanded.value = it
    }

    fun onMinutesExpandChanged(it: Boolean) {
        isMinutesExpanded.value = it
    }

    fun onSecondsExpandChanged(it: Boolean) {
        isSecondsExpanded.value = it
    }

    fun onResetClicked() {
        sweepAngle.value = 360f
        remaining.value = Duration.ZERO
        request.trySend(Duration.ZERO)
        state.value = TimerState.STOPPED
    }
}

private data class Expanded(
    val isUnitExpanded: Boolean,
    val isValueExpanded: Boolean,
    val isHoursExpanded: Boolean,
    val isMinutesExpanded: Boolean,
    val isSecondsExpanded: Boolean
)

private data class Values(
    val unit: ClockUnit,
    val value: Int,
    val hours: Int,
    val minutes: Int,
    val seconds: Int
)

fun Duration.formattedString(): String {
    return "%d:%02d:%02d".format(
        seconds / 3600,
        (seconds % 3600) / 60,
        seconds % 60
    )
}
