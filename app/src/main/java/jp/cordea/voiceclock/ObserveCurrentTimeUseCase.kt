package jp.cordea.voiceclock

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.shareIn
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ObserveCurrentTimeUseCase @Inject constructor() : CoroutineScope by MainScope() {
    private val observer = flow {
        while (true) {
            emit(Calendar.getInstance())
            delay(1000L)
        }
    }.shareIn(this, SharingStarted.Lazily, replay = 1)

    fun execute() = observer
}
