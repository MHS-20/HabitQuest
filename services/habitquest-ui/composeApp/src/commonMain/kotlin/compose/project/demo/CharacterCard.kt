package compose.project.demo

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun CharacterCard(name: String, stats: StatsState) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .border(2.dp, MaterialTheme.colorScheme.onBackground, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp)),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        BoxWithConstraints(modifier = Modifier.padding(12.dp)) {
            val maxW = this.maxWidth
            val compactThreshold: Dp = 420.dp
            val isCompact = maxW < compactThreshold

            val avatarSize = if (isCompact) 64.dp else 72.dp
            val nameStyle = if (isCompact) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium

            if (isCompact) {
                // Vertical compact layout
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Surface(
                        modifier = Modifier
                            .size(avatarSize)
                            .clip(RoundedCornerShape(8.dp)),
                        color = Color.Gray
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(name.take(2).uppercase(), color = Color.White)
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(name, style = nameStyle)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Lv ${stats.level}",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(4.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            stats.charClass,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondary,
                            modifier = Modifier.padding(4.dp)
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    // Stats stacked
                    StatRow(
                        label = "HP",
                        value = stats.hp,
                        max = stats.maxHp,
                        onDecrease = { stats.takeDamage(10) },
                        onIncrease = { stats.heal(10) },
                        decreaseLabel = "-10",
                        increaseLabel = "+10",
                        color = MaterialTheme.colorScheme.error
                    )

                    Spacer(Modifier.height(8.dp))

                    StatRow(
                        label = "Mana",
                        value = stats.mana,
                        max = stats.maxMana,
                        onDecrease = { stats.useMana(15) },
                        onIncrease = { stats.restoreMana(15) },
                        decreaseLabel = "-15",
                        increaseLabel = "+15",
                        color = MaterialTheme.colorScheme.tertiary
                    )

                    Spacer(Modifier.height(8.dp))

                    XpRow(stats = stats)
                }
            } else {
                // Wide layout: avatar left, stats right
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier
                            .size(avatarSize)
                            .clip(RoundedCornerShape(8.dp)),
                        color = Color.Gray
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(name.take(2).uppercase(), color = Color.White)
                        }
                    }

                    Spacer(Modifier.width(8.dp))

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(name, style = nameStyle)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Lv ${stats.level}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(4.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                stats.charClass,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondary,
                                modifier = Modifier.padding(4.dp)
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        StatRow(
                            label = "HP",
                            value = stats.hp,
                            max = stats.maxHp,
                            onDecrease = { stats.takeDamage(10) },
                            onIncrease = { stats.heal(10) },
                            decreaseLabel = "-10",
                            increaseLabel = "+10",
                            color = MaterialTheme.colorScheme.error
                        )

                        Spacer(Modifier.height(8.dp))

                        StatRow(
                            label = "Mana",
                            value = stats.mana,
                            max = stats.maxMana,
                            onDecrease = { stats.useMana(15) },
                            onIncrease = { stats.restoreMana(15) },
                            decreaseLabel = "-15",
                            increaseLabel = "+15",
                            color = MaterialTheme.colorScheme.tertiary
                        )

                        Spacer(Modifier.height(8.dp))

                        XpRow(stats = stats)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CharacterCardPreview() {
    val stats = StatsState()
    stats.setClass("Mage")
    CharacterCard(name = "Aria", stats = stats)
}
