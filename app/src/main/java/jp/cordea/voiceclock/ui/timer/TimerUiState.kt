package jp.cordea.voiceclock.ui.timer

import jp.cordea.voiceclock.TtsState
import jp.cordea.voiceclock.ui.clock.ClockUnit

data class TimerUiState(
    val remaining: String,
    val ttsState: TtsState,
    val showController: Boolean,
    val isValueExpanded: Boolean,
    val isUnitExpanded: Boolean,
    val unit: ClockUnit,
    val value: Int,
    val isStarted: Boolean
)
