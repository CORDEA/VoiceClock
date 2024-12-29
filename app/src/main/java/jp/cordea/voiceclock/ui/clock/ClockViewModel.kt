package jp.cordea.voiceclock.ui.clock

import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.cordea.voiceclock.GetTtsStateUseCase
import jp.cordea.voiceclock.ReadTextUseCase
import jp.cordea.voiceclock.TtsState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class ClockViewModel @Inject constructor(
    getTtsStateUseCase: GetTtsStateUseCase,
    readTextUseCase: ReadTextUseCase
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
    private val isStarted = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            while (true) {
                delay(1000L)
                val calendar = Calendar.getInstance()
                val now = FORMAT.format(calendar.time)
                time.value = now
                if (isStarted.value) {
                    val field = when (unit.value) {
                        ClockUnit.HOUR -> Calendar.HOUR_OF_DAY
                        ClockUnit.MINUTE -> Calendar.MINUTE
                        ClockUnit.SECOND -> Calendar.SECOND
                    }
                    if (calendar.get(field) == value.value) {
                        readTextUseCase.execute(now)
                    }
                }
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
            isStarted,
            getTtsStateUseCase.execute()
        ) { time, showController, expanded, isStarted, state ->
            ClockUiState(
                time,
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
            ClockUiState(
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
