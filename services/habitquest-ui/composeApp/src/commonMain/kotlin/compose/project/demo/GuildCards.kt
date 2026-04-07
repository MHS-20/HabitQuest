package compose.project.demo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun GuildCard(
    guild: GuildData,
    isLeader: Boolean = false,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(guild.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("ID: ${guild.id}", style = MaterialTheme.typography.bodySmall)
            Text(
                "Global rank: ${guild.globalRank?.toString() ?: "-"}",
                style = MaterialTheme.typography.bodySmall
            )
            Text("Members: ${guild.members.size}", style = MaterialTheme.typography.bodySmall)

            if (guild.members.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                guild.members.forEach { member ->
                    Text(
                        "- ${member.nickname} (${member.role}) [${member.avatarId}]",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            if (isLeader) {
                Spacer(Modifier.height(4.dp))
                Text("Leader", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun GuildLeaderboardRow(guild: GuildData, onInviteClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(guild.name, style = MaterialTheme.typography.titleMedium)
            Text("ID: ${guild.id}", style = MaterialTheme.typography.bodySmall)
            Text(
                "Rank: ${guild.globalRank?.toString() ?: "-"}",
                style = MaterialTheme.typography.bodySmall
            )
            Text("Members: ${guild.members.size}", style = MaterialTheme.typography.bodySmall)
            Button(
                onClick = onInviteClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Invite avatar")
            }
        }
    }
}

