package jp.cordea.voiceclock

import android.content.Context
import android.speech.tts.TextToSpeech
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TtsService @Inject constructor(
    @ApplicationContext private val context: Context
) : TextToSpeech.OnInitListener {
    private val _state = MutableSharedFlow<TtsState>(replay = 1)
    val state = _state.asSharedFlow()

    lateinit var tts: TextToSpeech

    override fun onInit(result: Int) {
        if (result == TextToSpeech.SUCCESS) {
            _state.tryEmit(TtsState.INITIALIZED)
        } else {
            _state.tryEmit(TtsState.ERROR)
        }
    }

    fun start() {
        tts = TextToSpeech(context, this)
    }
}

enum class TtsState {
    INITIALIZED,
    ERROR,
    LOADING
}
