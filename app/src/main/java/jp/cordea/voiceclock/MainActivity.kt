package jp.cordea.voiceclock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import jp.cordea.voiceclock.ui.VoiceClockApp
import jp.cordea.voiceclock.ui.theme.VoiceClockTheme

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
}
