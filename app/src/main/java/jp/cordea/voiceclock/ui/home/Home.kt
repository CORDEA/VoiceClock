package jp.cordea.voiceclock.ui.home

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import jp.cordea.voiceclock.ui.clock.Clock
import jp.cordea.voiceclock.ui.settings.Settings
import jp.cordea.voiceclock.ui.timer.Timer

@Composable
fun Home() {
    val navHostController = rememberNavController()
    Scaffold(
        bottomBar = {
            NavigationBar {
                val currentBackStackEntry by navHostController.currentBackStackEntryAsState()
                Route.entries.forEach {
                    NavigationBarItem(
                        icon = { Icon(it.icon, contentDescription = null) },
                        label = { Text(it.label) },
                        selected = currentBackStackEntry?.destination?.route == it.route,
                        onClick = {
                            navHostController.navigate(it.route) {
                                popUpTo(navHostController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) {
        NavHost(
            navHostController,
            startDestination = Route.Clock.route,
            modifier = Modifier.padding(it)
        ) {
            composable(Route.Clock.route) { Clock(hiltViewModel()) }
            composable(Route.Timer.route) { Timer() }
            composable(Route.Settings.route) { Settings() }
        }
    }
}

private enum class Route {
    Clock,
    Timer,
    Settings;

    val route: String
        get() = name.lowercase()

    val icon: ImageVector
        get() = when (this) {
            Clock -> Icons.Default.Schedule
            Timer -> Icons.Default.Alarm
            Settings -> Icons.Default.Settings
        }

    val label: String
        get() = name
}
