package jp.cordea.voiceclock

import dagger.Reusable
import java.time.Duration
import javax.inject.Inject

@Reusable
class ShouldReadTimerTextUseCase @Inject constructor() {
    fun execute(current: Duration, timing: Long): Boolean {
        val seconds = current.seconds
        if (current == Duration.ZERO || seconds < timing) {
            return false
        }
        if (timing == 0L) {
            return seconds % 60 == 0L
        }
        if (seconds == timing) {
            return true
        }
        val timingInMinutes = timing / 60L
        if (timingInMinutes < 1) {
            return (seconds - timing) % 60 == 0L
        }
        val timingInHours = timing / 3600L
        if (timingInHours < 1) {
            return (current.toMinutes() - timingInMinutes) % 60 == 0L &&
                    seconds % 60 == 0L
        }
        val hours = current.toHours()
        return (hours - timingInHours) % 24 == 0L &&
                current.toMinutes() % 60 == 0L &&
                seconds % 60 == 0L
    }
}
