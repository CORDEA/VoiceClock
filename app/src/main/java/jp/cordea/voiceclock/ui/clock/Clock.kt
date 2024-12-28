package jp.cordea.voiceclock.ui.clock

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout

@Composable
fun Clock(viewModel: ClockViewModel) {
    val state by viewModel.uiState.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
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
            fontSize = MaterialTheme.typography.headlineLarge.fontSize
        )
    }
}
