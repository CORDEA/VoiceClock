package jp.cordea.voiceclock

import android.app.*
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import dagger.hilt.android.AndroidEntryPoint
import jp.cordea.voiceclock.ui.timer.formattedString
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import java.time.Duration
import javax.inject.Inject

@AndroidEntryPoint
class TimerService : Service() {
    companion object {
        private const val NOTIFICATION_ID = 1
    }

    @Inject
    lateinit var readTextUseCase: ReadTextUseCase

    @Inject
    lateinit var observeCurrentTimeUseCase: ObserveCurrentTimeUseCase

    @Inject
    lateinit var shouldReadTimerTextUseCase: ShouldReadTimerTextUseCase

    private val binder = TimerBinder()
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    val timerChannel: ReceiveChannel<Duration>
        get() = _timerChannel
    private val _timerChannel = Channel<Duration>(Channel.CONFLATED)

    override fun onCreate() {
        super.onCreate()
        val channel = NotificationChannel(
            getString(R.string.timer_service_channel_id),
            getString(R.string.timer_service_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        )
        getSystemService<NotificationManager>()?.createNotificationChannel(channel)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(
            NOTIFICATION_ID,
            createNotification(getString(R.string.timer_service_notification_title_initial))
        )
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onDestroy() {
        super.onDestroy()
        _timerChannel.close()
        stop()
    }

    fun start(duration: Duration, timing: Long) {
        var next = duration
        Intent(applicationContext, TimerService::class.java).also { intent ->
            startForegroundService(intent)
        }
        serviceJob.cancelChildren()
        serviceScope.launch {
            while (true) {
                delay(1000L)
                next = next.minusSeconds(1)
                _timerChannel.send(next)
                if (next.isZero) {
                    readTextUseCase.execute(
                        getString(R.string.timer_finished_message)
                    )
                    stop()
                    return@launch
                }
                if (shouldReadTimerTextUseCase.execute(next, timing)) {
                    readTextUseCase.execute(next.formattedString())
                }
                getSystemService<NotificationManager>()?.notify(
                    NOTIFICATION_ID, createNotification(next.formattedString())
                )
            }
        }
    }

    fun stop() {
        serviceJob.cancelChildren()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotification(contentText: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, getString(R.string.timer_service_channel_id))
            .setContentTitle(contentText)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setOnlyAlertOnce(true)
            .build()
    }

    inner class TimerBinder : Binder() {
        fun getService(): TimerService = this@TimerService
    }
}
