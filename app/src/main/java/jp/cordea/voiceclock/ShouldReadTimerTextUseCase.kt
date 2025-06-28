package jp.cordea.voiceclock

import dagger.Reusable
import java.time.Duration
import javax.inject.Inject

@Reusable
class ShouldReadTimerTextUseCase @Inject constructor() {
    fun execute(current: Duration, timing: Long): Boolean {
        if (current == Duration.ZERO || timing <= 0L) {
            return false
        }
        return current.seconds % timing == 0L
    }
}
