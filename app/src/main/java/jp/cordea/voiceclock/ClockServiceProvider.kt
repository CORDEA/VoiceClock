package jp.cordea.voiceclock

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClockServiceProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun get() = callbackFlow {
        val connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                val binder = service as ClockService.ClockBinder
                trySend(binder.getService())
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                cancel()
            }
        }
        Intent(context, ClockService::class.java).also { intent ->
            context.bindService(
                intent,
                connection,
                Context.BIND_AUTO_CREATE
            )
        }
        awaitClose { context.unbindService(connection) }
    }
}
