package jp.cordea.voiceclock

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import jp.cordea.voiceclock.ui.VoiceClockApp
import jp.cordea.voiceclock.ui.theme.VoiceClockTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VoiceClockTheme {
                VoiceClockApp()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing) {
            listOf(
                Intent(this, TimerService::class.java),
                Intent(this, ClockService::class.java)
            ).forEach(::stopService)
        }
    }
}
