package jp.cordea.voiceclock

import dagger.Reusable
import javax.inject.Inject

@Reusable
class ReadTextUseCase @Inject constructor(
    private val ttsService: TtsService
) {
    fun execute(text: String) {
        ttsService.speak(text)
    }
}
