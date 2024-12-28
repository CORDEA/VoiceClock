package jp.cordea.voiceclock

import dagger.Reusable
import javax.inject.Inject

@Reusable
class StartTtsUseCase @Inject constructor(
    private val ttsService: TtsService
) {
    fun execute() {
        ttsService.start()
    }
}
