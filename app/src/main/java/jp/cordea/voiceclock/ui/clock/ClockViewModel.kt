package jp.cordea.voiceclock.ui.clock

import android.icu.text.SimpleDateFormat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class ClockViewModel @Inject constructor() : ViewModel() {
    companion object {
        private val FORMAT = SimpleDateFormat.getTimeInstance(SimpleDateFormat.MEDIUM)
    }

    private val time = MutableStateFlow(FORMAT.format(Date()))

    init {
        viewModelScope.launch {
            while (true) {
                delay(1000L)
                time.value = FORMAT.format(Date())
            }
        }
    }

    val uiState = time.map { ClockUiState(it) }
        .stateIn(viewModelScope, started = SharingStarted.WhileSubscribed(), ClockUiState(""))
}
