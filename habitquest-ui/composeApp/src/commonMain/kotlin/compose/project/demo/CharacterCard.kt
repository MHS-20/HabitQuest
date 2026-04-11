package compose.project.demo

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun AvatarCard(avatar: AvatarData, onRefreshStats: (() -> Unit)? = null) {
  val avatarClass =
    when {
      avatar.intelligence >= avatar.strength && avatar.intelligence >= avatar.defense -> "Mage"
      avatar.defense >= avatar.strength && avatar.defense >= avatar.intelligence -> "Guardian"
      else -> "Warrior"
    }

  Surface(
    modifier =
      Modifier.fillMaxWidth()
        .padding(8.dp)
        .border(2.dp, MaterialTheme.colorScheme.onBackground, RoundedCornerShape(12.dp))
        .clip(RoundedCornerShape(12.dp)),
    color = MaterialTheme.colorScheme.surfaceVariant,
  ) {
    BoxWithConstraints(modifier = Modifier.padding(12.dp)) {
      val maxW = this.maxWidth
      val compactThreshold: Dp = 420.dp
      val isCompact = maxW < compactThreshold

      val avatarSize = if (isCompact) 64.dp else 72.dp
      val nameStyle =
        if (isCompact) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium

      if (isCompact) {
        // Vertical compact layout
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Surface(
            modifier = Modifier.size(avatarSize).clip(RoundedCornerShape(8.dp)),
            color = Color.Gray,
          ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
              Text(avatar.name.take(2).uppercase(), color = Color.White)
            }
          }

          Spacer(Modifier.height(8.dp))

          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(avatar.name, style = nameStyle)
            Spacer(Modifier.width(8.dp))
            Text(
              "Lv ${avatar.level}",
              style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onPrimary,
              modifier = Modifier.padding(4.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text(
              avatarClass,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSecondary,
              modifier = Modifier.padding(4.dp),
            )
            if (onRefreshStats != null) {
              Spacer(Modifier.width(6.dp))
              TextButton(onClick = onRefreshStats) {
                Text("Refresh", style = MaterialTheme.typography.bodySmall)
              }
            }
          }

          Text(
            "Gold: ${avatar.money}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
          )

          Spacer(Modifier.height(8.dp))

          MeterRow(
            label = "HP",
            value = avatar.hp,
            max = avatar.maxHp,
            color = MaterialTheme.colorScheme.error,
          )

          Spacer(Modifier.height(8.dp))

          MeterRow(
            label = "Mana",
            value = avatar.mana,
            max = avatar.maxMana,
            color = MaterialTheme.colorScheme.tertiary,
          )

          Spacer(Modifier.height(8.dp))

          MeterRow(
            label = "XP",
            value = avatar.currentXp,
            max = avatar.nextLevelXp,
            color = MaterialTheme.colorScheme.secondary,
          )

          Spacer(Modifier.height(12.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
          ) {
            Text("💪 Str: ${avatar.strength}", style = MaterialTheme.typography.bodySmall)
            Text("🛡️ Def: ${avatar.defense}", style = MaterialTheme.typography.bodySmall)
            Text("🧠 Int: ${avatar.intelligence}", style = MaterialTheme.typography.bodySmall)
          }
        }
      } else {
        // Wide layout: avatar left, stats right
        Row(verticalAlignment = Alignment.CenterVertically) {
          Surface(
            modifier = Modifier.size(avatarSize).clip(RoundedCornerShape(8.dp)),
            color = Color.Gray,
          ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
              Text(avatar.name.take(2).uppercase(), color = Color.White)
            }
          }

          Spacer(Modifier.width(8.dp))

          Column(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(avatar.name, style = nameStyle)
              Spacer(Modifier.width(8.dp))
              Text(
                "Lv ${avatar.level}",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(4.dp),
              )
              Spacer(Modifier.width(4.dp))
              Text(
                avatarClass,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondary,
                modifier = Modifier.padding(4.dp),
              )
              if (onRefreshStats != null) {
                Spacer(Modifier.width(6.dp))
                TextButton(onClick = onRefreshStats) {
                  Text("Refresh", style = MaterialTheme.typography.bodySmall)
                }
              }
            }

            Text(
              "Gold: ${avatar.money}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.primary,
            )

            Spacer(Modifier.height(8.dp))

            MeterRow(
              label = "HP",
              value = avatar.hp,
              max = avatar.maxHp,
              color = MaterialTheme.colorScheme.error,
            )

            Spacer(Modifier.height(8.dp))

            MeterRow(
              label = "Mana",
              value = avatar.mana,
              max = avatar.maxMana,
              color = MaterialTheme.colorScheme.tertiary,
            )

            Spacer(Modifier.height(8.dp))

            MeterRow(
              label = "XP",
              value = avatar.currentXp,
              max = avatar.nextLevelXp,
              color = MaterialTheme.colorScheme.secondary,
            )

            Spacer(Modifier.height(12.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
            ) {
              Text("💪 Str: ${avatar.strength}", style = MaterialTheme.typography.bodySmall)
              Text("🛡️ Def: ${avatar.defense}", style = MaterialTheme.typography.bodySmall)
              Text("🧠 Int: ${avatar.intelligence}", style = MaterialTheme.typography.bodySmall)
            }
          }
        }
      }
    }
  }
}

@Composable
private fun MeterRow(label: String, value: Int, max: Int, color: Color) {
  val normalizedMax = max.coerceAtLeast(1)
  val progressTarget = (value.coerceAtLeast(0).toFloat() / normalizedMax).coerceIn(0f, 1f)
  val animatedProgress by animateFloatAsState(targetValue = progressTarget)

  Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
    Text("$label: $value / $normalizedMax")
    LinearProgressIndicator(
      progress = { animatedProgress },
      color = color,
      trackColor = color.copy(alpha = 0.25f),
      modifier = Modifier.fillMaxWidth().height(8.dp),
    )
  }
}

@Preview(showBackground = true)
@Composable
fun AvatarCardPreview() {
  AvatarCard(
    avatar =
      AvatarData(
        id = "user-1",
        name = "Aria",
        money = 150,
        level = 5,
        currentXp = 120,
        nextLevelXp = 300,
        hp = 82,
        maxHp = 100,
        mana = 44,
        maxMana = 60,
        strength = 14,
        defense = 11,
        intelligence = 19,
      )
  )
}
