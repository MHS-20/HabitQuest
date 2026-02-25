package compose.project.demo

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun XpRow(stats: StatsState) {
    val xpColor = MaterialTheme.colorScheme.secondary

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("XP: ${stats.xp} / ${stats.nextLevelXp}")
        LinearProgressIndicator(
            progress = { stats.xp.toFloat() / stats.nextLevelXp },
            color = xpColor,
            trackColor = xpColor.copy(alpha = 0.25f),
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
        )
        Spacer(Modifier.height(8.dp))
        Row {
            Button(onClick = { stats.gainXp(120) }) { Text("Gain 120 XP") }
            Spacer(Modifier.width(8.dp))
            Button(onClick = { stats.resetXp() }) { Text("Reset XP") }
        }
    }
}
