package jp.cordea.voiceclock.ui.clock

import jp.cordea.voiceclock.TtsState

data class ClockUiState(
    val time: String,
    val ttsState: TtsState,
    val showController: Boolean,
    val isValueExpanded: Boolean,
    val isUnitExpanded: Boolean,
    val unit: ClockUnit,
    val value: Int,
    val timerState: TimerState
)

enum class ClockUnit {
    HOUR,
    MINUTE,
    SECOND
}

enum class TimerState {
    IDLE,
    STARTED,
    STOPPED,
}
