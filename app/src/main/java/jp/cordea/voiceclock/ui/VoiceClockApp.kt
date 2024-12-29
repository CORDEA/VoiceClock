package jp.cordea.voiceclock.ui

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import jp.cordea.voiceclock.ui.home.Home

@Composable
fun VoiceClockApp() {
    AppNavHost(navHostController = rememberNavController())
}

@Composable
private fun AppNavHost(navHostController: NavHostController) {
    NavHost(navHostController, startDestination = "home") {
        composable("home") { Home(hiltViewModel()) }
    }
}
