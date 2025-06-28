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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@AndroidEntryPoint
class ClockService : Service() {
    companion object {
        private const val NOTIFICATION_ID = 2
        private val FORMAT = SimpleDateFormat.getTimeInstance(SimpleDateFormat.MEDIUM)
    }

    @Inject
    lateinit var readTextUseCase: ReadTextUseCase

    @Inject
    lateinit var observeCurrentTimeUseCase: ObserveCurrentTimeUseCase

    private val binder = ClockBinder()
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    override fun onCreate() {
        super.onCreate()
        val channel = NotificationChannel(
            getString(R.string.clock_service_channel_id),
            getString(R.string.clock_service_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        )
        getSystemService<NotificationManager>()?.createNotificationChannel(channel)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(
            NOTIFICATION_ID,
            createNotification(getString(R.string.clock_service_notification_title_initial))
        )
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onDestroy() {
        super.onDestroy()
        stop()
    }

    fun start(value: Int, unit: ClockUnit) {
        Intent(applicationContext, ClockService::class.java).also { intent ->
            startForegroundService(intent)
        }
        serviceJob.cancelChildren()
        observeCurrentTimeUseCase.execute()
            .onEach { calendar ->
                val now = FORMAT.format(calendar.time)
                when (unit) {
                    ClockUnit.HOUR ->
                        if (calendar.get(Calendar.HOUR_OF_DAY) == value) {
                            if (calendar.get(Calendar.MINUTE) == 0 && calendar.get(Calendar.SECOND) == 0) {
                                readTextUseCase.execute(now)
                            }
                        }

                    ClockUnit.MINUTE ->
                        if (calendar.get(Calendar.MINUTE) == value && calendar.get(Calendar.SECOND) == 0) {
                            readTextUseCase.execute(now)
                        }

                    ClockUnit.SECOND -> if (calendar.get(Calendar.SECOND) == value) {
                        readTextUseCase.execute(now)
                    }
                }
                getSystemService<NotificationManager>()?.notify(
                    NOTIFICATION_ID, createNotification(now)
                )
            }
            .launchIn(serviceScope)
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
        return NotificationCompat.Builder(this, getString(R.string.clock_service_channel_id))
            .setContentTitle(contentText)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setOnlyAlertOnce(true)
            .build()
    }

    inner class ClockBinder : Binder() {
        fun getService(): ClockService = this@ClockService
    }
}
