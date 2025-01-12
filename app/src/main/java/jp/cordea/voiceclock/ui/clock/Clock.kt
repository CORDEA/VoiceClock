package jp.cordea.voiceclock.ui.clock

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
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
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import jp.cordea.voiceclock.R
import jp.cordea.voiceclock.TtsState

@Composable
fun Clock(viewModel: ClockViewModel) {
    val context = LocalContext.current
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
                            imageVector = if (state.isStarted) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = stringResource(if (state.isStarted) R.string.clock_stop else R.string.clock_play),
                            modifier = Modifier.size(FloatingActionButtonDefaults.LargeIconSize)
                        )
                    }

                TtsState.ERROR -> {
                    val message = stringResource(R.string.clock_error)
                    LargeFloatingActionButton(onClick = {
                        Toast.makeText(
                            context,
                            message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = stringResource(R.string.clock_error),
                            modifier = Modifier.size(FloatingActionButtonDefaults.LargeIconSize)
                        )
                    }
                }

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
                value = state.value,
                unit = state.unit,
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
                onClicked = {
                    viewModel.onPlayClicked()
                }
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(innerPadding)
        ) {
            Text(
                text = state.time,
                modifier = Modifier
                    .layout { measurable, constraints ->
                        val placeable = measurable.measure(constraints)
                        layout(placeable.width, placeable.height) {
                            placeable.place(
                                constraints.maxWidth / 2 - placeable.width / 2,
                                constraints.maxHeight / 3
                            )
                        }
                    },
                fontStyle = MaterialTheme.typography.headlineLarge.fontStyle,
                fontSize = MaterialTheme.typography.headlineLarge.fontSize,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun Controller(
    isValueExpanded: Boolean,
    isUnitExpanded: Boolean,
    value: Int,
    unit: ClockUnit,
    onDismiss: () -> Unit,
    onValueExpandChanged: (Boolean) -> Unit,
    onValueChanged: (Int) -> Unit,
    onUnitExpandChanged: (Boolean) -> Unit,
    onUnitChanged: (ClockUnit) -> Unit,
    onClicked: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                stringResource(R.string.clock_controller_title),
                style = MaterialTheme.typography.titleSmall,
                fontSize = MaterialTheme.typography.titleSmall.fontSize,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.clock_controller_prefix))
                Spacer(modifier = Modifier.width(16.dp))
                ExposedDropdownMenuBox(
                    modifier = Modifier.weight(1f),
                    expanded = isValueExpanded,
                    onExpandedChange = onValueExpandChanged
                ) {
                    TextField(
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable),
                        value = value.toString(),
                        onValueChange = {},
                        singleLine = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = isValueExpanded,
                                modifier = Modifier.menuAnchor(MenuAnchorType.SecondaryEditable)
                            )
                        }
                    )
                    DropdownMenu(expanded = isValueExpanded, onDismissRequest = {
                        onValueExpandChanged(false)
                    }) {
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
                }
                Spacer(modifier = Modifier.width(8.dp))
                ExposedDropdownMenuBox(
                    modifier = Modifier.weight(1f),
                    expanded = isUnitExpanded,
                    onExpandedChange = onUnitExpandChanged
                ) {
                    TextField(
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable),
                        value = when (unit) {
                            ClockUnit.HOUR -> stringResource(R.string.clock_controller_unit_hour)
                            ClockUnit.MINUTE -> stringResource(R.string.clock_controller_unit_minute)
                            ClockUnit.SECOND -> stringResource(R.string.clock_controller_unit_second)
                        },
                        onValueChange = {},
                        singleLine = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = isUnitExpanded,
                                modifier = Modifier.menuAnchor(MenuAnchorType.SecondaryEditable)
                            )
                        }
                    )
                    DropdownMenu(expanded = isUnitExpanded, onDismissRequest = {
                        onUnitExpandChanged(false)
                    }) {
                        ClockUnit.entries.forEach {
                            DropdownMenuItem(
                                onClick = {
                                    onUnitChanged(it)
                                },
                                text = {
                                    when (it) {
                                        ClockUnit.HOUR ->
                                            Text(stringResource(R.string.clock_controller_unit_hour))

                                        ClockUnit.MINUTE ->
                                            Text(stringResource(R.string.clock_controller_unit_minute))

                                        ClockUnit.SECOND ->
                                            Text(stringResource(R.string.clock_controller_unit_second))
                                    }
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                            )
                        }

                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(stringResource(R.string.clock_controller_suffix))
            }
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onClicked,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.clock_controller_play))
            }
        }
    }
}