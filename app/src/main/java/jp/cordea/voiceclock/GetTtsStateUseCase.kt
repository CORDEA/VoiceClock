package jp.cordea.voiceclock

import dagger.Reusable
import javax.inject.Inject

@Reusable
class GetTtsStateUseCase @Inject constructor(
    private val ttsService: TtsService
) {
    fun execute() = ttsService.state
}
