package jp.cordea.voiceclock

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import dagger.hilt.android.AndroidEntryPoint
import jp.cordea.voiceclock.ui.clock.ClockUnit
import jp.cordea.voiceclock.ui.timer.formattedString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Duration
import javax.inject.Inject

@AndroidEntryPoint
class TimerService : Service() {
    companion object {
        private const val NOTIFICATION_ID = 1
        private val FORMAT = SimpleDateFormat.getTimeInstance(SimpleDateFormat.MEDIUM)
    }

    @Inject
    lateinit var readTextUseCase: ReadTextUseCase

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
            NotificationManager.IMPORTANCE_LOW
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

    fun startClock(value: Int, unit: ClockUnit) {
        Intent(applicationContext, TimerService::class.java).also { intent ->
            startForegroundService(intent)
        }
        serviceJob.cancelChildren()
        serviceScope.launch {
            while (true) {
                delay(1000L)
                val calendar = Calendar.getInstance()
                val now = FORMAT.format(calendar.time)
                val field = when (unit) {
                    ClockUnit.HOUR -> Calendar.HOUR_OF_DAY
                    ClockUnit.MINUTE -> Calendar.MINUTE
                    ClockUnit.SECOND -> Calendar.SECOND
                }
                if (calendar.get(field) == value) {
                    readTextUseCase.execute(now)
                }
                getSystemService<NotificationManager>()?.notify(
                    NOTIFICATION_ID, createNotification(now)
                )
            }
        }
    }

    fun startTimer(duration: Duration, timer: Int) {
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
                if (next.seconds % timer == 0L) {
                    readTextUseCase.execute(next.formattedString())
                }
                if (next.isZero) {
                    stop()
                    return@launch
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
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .build()
    }

    inner class TimerBinder : Binder() {
        fun getService(): TimerService = this@TimerService
    }
}
