package jp.cordea.voiceclock

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.shareIn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimerServiceProvider @Inject constructor(
    @ApplicationContext private val context: Context
) : CoroutineScope by MainScope() {
    private lateinit var connection: ServiceConnection

    private val service = callbackFlow {
        connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                val binder = service as TimerService.TimerBinder
                trySend(binder.getService())
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                cancel()
            }
        }
        Intent(context, TimerService::class.java).also { intent ->
            context.bindService(
                intent,
                connection,
                Context.BIND_AUTO_CREATE
            )
        }
        awaitClose { context.unbindService(connection) }
    }.shareIn(this, SharingStarted.Lazily, replay = 1)

    fun get(): Flow<TimerService> = service
}
