package jp.cordea.voiceclock

import dagger.Reusable
import javax.inject.Inject

@Reusable
class CancelTtsUseCase @Inject constructor(
    private val ttsService: TtsService
) {
    fun execute() {
        ttsService.cancel()
    }
}
