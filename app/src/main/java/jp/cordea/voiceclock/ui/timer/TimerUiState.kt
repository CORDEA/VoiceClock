package jp.cordea.voiceclock.ui.timer

import jp.cordea.voiceclock.TtsState
import jp.cordea.voiceclock.ui.clock.ClockUnit

data class TimerUiState(
    val remaining: String,
    val ttsState: TtsState,
    val showController: Boolean,
    val isValueExpanded: Boolean,
    val isUnitExpanded: Boolean,
    val isHoursExpanded: Boolean,
    val isMinutesExpanded: Boolean,
    val isSecondsExpanded: Boolean,
    val hours: Int,
    val minutes: Int,
    val seconds: Int,
    val unit: ClockUnit,
    val value: Int,
    val state: TimerState
)

enum class TimerState {
    STARTED,
    PAUSED,
    STOPPED
}
