package compose.project.demo

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun StatRow(
    label: String,
    value: Int,
    max: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    decreaseLabel: String = "-",
    increaseLabel: String = "+",
    color: Color = Color.Unspecified
) {
    val progressTarget = if (max > 0) value.toFloat() / max else 0f
    val animatedProgress by animateFloatAsState(targetValue = progressTarget)

    // Use theme colors if no explicit color passed
    val barColor = if (color == Color.Unspecified) MaterialTheme.colorScheme.primary else color
    val track = barColor.copy(alpha = 0.25f)

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("$label: $value / $max")
        LinearProgressIndicator(
            progress = { animatedProgress },
            color = barColor,
            trackColor = track,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
        )
        Spacer(Modifier.height(8.dp))
        Row {
            Button(onClick = onDecrease) { Text(decreaseLabel) }
            Spacer(Modifier.width(8.dp))
            Button(onClick = onIncrease) { Text(increaseLabel) }
        }
    }
}
