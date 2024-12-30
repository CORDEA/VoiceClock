package jp.cordea.voiceclock.ui.timer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import jp.cordea.voiceclock.TtsState
import jp.cordea.voiceclock.ui.clock.ClockUnit

@Composable
fun Timer(viewModel: TimerViewModel) {
    val state by viewModel.uiState.collectAsState()
    Scaffold(
        floatingActionButton = {
            when (state.ttsState) {
                TtsState.INITIALIZED ->
                    LargeFloatingActionButton(
                        onClick = {
                            viewModel.onFabClicked()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            modifier = Modifier.size(FloatingActionButtonDefaults.LargeIconSize)
                        )
                    }

                TtsState.ERROR -> {}
                TtsState.LOADING -> LargeFloatingActionButton(onClick = {}) {
                    CircularProgressIndicator()
                }
            }
        }
    ) { innerPadding ->
        if (state.showController) {
            Controller(
                isValueExpanded = state.isValueExpanded,
                isUnitExpanded = state.isUnitExpanded,
                isHoursExpanded = state.isHoursExpanded,
                isMinutesExpanded = state.isMinutesExpanded,
                isSecondsExpanded = state.isSecondsExpanded,
                value = state.value,
                unit = state.unit,
                hours = state.hours,
                minutes = state.minutes,
                seconds = state.seconds,
                onDismiss = {
                    viewModel.onDismissController()
                },
                onValueExpandChanged = {
                    viewModel.onValueExpandChanged(it)
                },
                onValueChanged = {
                    viewModel.onValueChanged(it)
                },
                onUnitExpandChanged = {
                    viewModel.onUnitExpandChanged(it)
                },
                onUnitChanged = {
                    viewModel.onUnitChanged(it)
                },
                onHoursChanged = {
                    viewModel.onHoursChanged(it)
                },
                onHoursExpandChanged = {
                    viewModel.onHoursExpandChanged(it)
                },
                onMinutesChanged = {
                    viewModel.onMinutesChanged(it)
                },
                onMinutesExpandChanged = {
                    viewModel.onMinutesExpandChanged(it)
                },
                onSecondsChanged = {
                    viewModel.onSecondsChanged(it)
                },
                onSecondsExpandChanged = {
                    viewModel.onSecondsExpandChanged(it)
                },
                onClicked = {
                    viewModel.onPlayClicked()
                }
            )
        }
        Column(
            modifier = Modifier.padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            Progress(
                remaining = state.remaining,
                sweepAngle = state.sweepAngle
            )
        }
    }
}

@Composable
private fun Progress(
    remaining: Long,
    sweepAngle: Float
) {
    val color = MaterialTheme.colorScheme.primary
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .aspectRatio(1f)
        ) {
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = 8.dp.toPx())
            )
        }
        Text(
            text = "%d:%02d:%02d".format(
                remaining / 3600,
                (remaining % 3600) / 60,
                remaining % 60
            ),
            style = MaterialTheme.typography.headlineLarge,
            fontSize = MaterialTheme.typography.headlineLarge.fontSize,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun Controller(
    isValueExpanded: Boolean,
    isUnitExpanded: Boolean,
    isHoursExpanded: Boolean,
    isMinutesExpanded: Boolean,
    isSecondsExpanded: Boolean,
    value: Int,
    unit: ClockUnit,
    hours: Int,
    minutes: Int,
    seconds: Int,
    onDismiss: () -> Unit,
    onValueExpandChanged: (Boolean) -> Unit,
    onValueChanged: (Int) -> Unit,
    onUnitExpandChanged: (Boolean) -> Unit,
    onUnitChanged: (ClockUnit) -> Unit,
    onHoursChanged: (Int) -> Unit,
    onHoursExpandChanged: (Boolean) -> Unit,
    onMinutesChanged: (Int) -> Unit,
    onMinutesExpandChanged: (Boolean) -> Unit,
    onSecondsChanged: (Int) -> Unit,
    onSecondsExpandChanged: (Boolean) -> Unit,
    onClicked: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Dropdown(
                    value = hours.toString(),
                    expanded = isHoursExpanded,
                    onExpandedChange = onHoursExpandChanged,
                    modifier = Modifier.weight(1f)
                ) {
                    (0..23).forEach {
                        DropdownMenuItem(
                            onClick = {
                                onHoursChanged(it)
                            },
                            text = {
                                Text(it.toString())
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(":")
                Spacer(modifier = Modifier.width(4.dp))
                Dropdown(
                    value = minutes.toString(),
                    expanded = isMinutesExpanded,
                    onExpandedChange = onMinutesExpandChanged,
                    modifier = Modifier.weight(1f)
                ) {
                    (0..59).forEach {
                        DropdownMenuItem(
                            onClick = {
                                onMinutesChanged(it)
                            },
                            text = {
                                Text(it.toString())
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(":")
                Spacer(modifier = Modifier.width(4.dp))
                Dropdown(
                    value = seconds.toString(),
                    expanded = isSecondsExpanded,
                    onExpandedChange = onSecondsExpandChanged,
                    modifier = Modifier.weight(1f)
                ) {
                    (0..59).forEach {
                        DropdownMenuItem(
                            onClick = {
                                onSecondsChanged(it)
                            },
                            text = {
                                Text(it.toString())
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Every")
                Spacer(modifier = Modifier.width(16.dp))
                Dropdown(
                    value = value.toString(),
                    expanded = isValueExpanded,
                    onExpandedChange = onValueExpandChanged,
                    modifier = Modifier.weight(1f)
                ) {
                    val range = when (unit) {
                        ClockUnit.HOUR -> (0..23)
                        ClockUnit.MINUTE -> (0..59)
                        ClockUnit.SECOND -> (0..59)
                    }
                    range.forEach {
                        DropdownMenuItem(
                            onClick = {
                                onValueChanged(it)
                            },
                            text = {
                                Text(it.toString())
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Dropdown(
                    value = when (unit) {
                        ClockUnit.HOUR -> "hour"
                        ClockUnit.MINUTE -> "minute"
                        ClockUnit.SECOND -> "second"
                    },
                    expanded = isUnitExpanded,
                    onExpandedChange = onUnitExpandChanged,
                    modifier = Modifier.weight(1f)
                ) {
                    ClockUnit.entries.forEach {
                        DropdownMenuItem(
                            onClick = {
                                onUnitChanged(it)
                            },
                            text = {
                                when (it) {
                                    ClockUnit.HOUR -> Text("hour")
                                    ClockUnit.MINUTE -> Text("minute")
                                    ClockUnit.SECOND -> Text("second")
                                }
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onClicked,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Play")
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun Dropdown(
    value: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = onExpandedChange,
        modifier = modifier
    ) {
        TextField(
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable),
            value = value,
            onValueChange = {},
            singleLine = true,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded,
                    modifier = Modifier.menuAnchor(MenuAnchorType.SecondaryEditable)
                )
            }
        )
        DropdownMenu(expanded = expanded, onDismissRequest = {
            onExpandedChange(false)
        }, content = content)
    }
}
