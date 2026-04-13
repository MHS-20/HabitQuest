package compose.project.demo.contexts.guild.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import compose.project.demo.contexts.guild.infrastructure.repository.BattleStatsData
import compose.project.demo.contexts.guild.infrastructure.repository.BossData
import compose.project.demo.contexts.guild.infrastructure.repository.PendingInviteData
import compose.project.demo.contexts.guild.infrastructure.repository.SearchAvatarData

@Composable
fun CreateGuildDialog(
  guildName: String,
  onGuildNameChange: (String) -> Unit,
  loading: Boolean,
  onCreate: () -> Unit,
  onDismiss: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Create new guild") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
          value = guildName,
          onValueChange = onGuildNameChange,
          label = { Text("Guild name") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
        )
        Text(
          text = "The current avatar will be used as the creator.",
          style = MaterialTheme.typography.bodySmall,
        )
      }
    },
    confirmButton = { TextButton(onClick = onCreate, enabled = !loading) { Text("Create") } },
    dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
  )
}

@Composable
fun InviteAvatarDialog(
  guildName: String,
  inviteSearchText: String,
  onInviteSearchTextChange: (String) -> Unit,
  inviteLoading: Boolean,
  inviteError: String?,
  inviteSearchResult: SearchAvatarData?,
  onSearch: () -> Unit,
  onSendInvite: (SearchAvatarData) -> Unit,
  onDismiss: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Invite avatar to $guildName") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
          value = inviteSearchText,
          onValueChange = onInviteSearchTextChange,
          label = { Text("Search avatar by name") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
        )

        Button(
          onClick = onSearch,
          enabled = inviteSearchText.isNotBlank() && !inviteLoading,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Text("Search")
        }

        inviteError?.let { error ->
          Text(
            text = error,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
          )
        }

        inviteSearchResult?.let { avatar ->
          Card(
            modifier = Modifier.fillMaxWidth(),
            colors =
              CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
          ) {
            Column(
              modifier = Modifier.padding(8.dp),
              verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
              Text(avatar.name, style = MaterialTheme.typography.titleSmall)
              Text("ID: ${avatar.id}", style = MaterialTheme.typography.bodySmall)
              Button(
                onClick = { onSendInvite(avatar) },
                enabled = !inviteLoading,
                modifier = Modifier.fillMaxWidth(),
              ) {
                Text("Send Invite")
              }
            }
          }
        }
      }
    },
    confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
  )
}

@Composable
fun PendingInvitesDialog(
  pendingInvitesLoading: Boolean,
  pendingInvitesError: String?,
  pendingInvitesMessage: String?,
  pendingInvites: List<PendingInviteData>,
  acceptingInviteId: String?,
  onAcceptInvite: (PendingInviteData) -> Unit,
  onDismiss: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Pending guild invites") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (pendingInvitesLoading) {
          CircularProgressIndicator()
        }

        pendingInvitesError?.let { error ->
          Text(
            text = error,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
          )
        }

        pendingInvitesMessage?.let { message ->
          Text(text = message, style = MaterialTheme.typography.bodySmall)
        }

        if (!pendingInvitesLoading && pendingInvitesError == null) {
          if (pendingInvites.isEmpty()) {
            Text("No pending invites")
          } else {
            LazyColumn(
              modifier = Modifier.fillMaxWidth().height(220.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              items(pendingInvites, key = { it.inviteId }) { invite ->
                Card(
                  modifier = Modifier.fillMaxWidth(),
                  colors =
                    CardDefaults.cardColors(
                      containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                ) {
                  Column(
                    modifier = Modifier.padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                  ) {
                    Text(invite.guildName, style = MaterialTheme.typography.titleSmall)
                    Text("Guild ID: ${invite.guildId}", style = MaterialTheme.typography.bodySmall)
                    Text(
                      "Invite ID: ${invite.inviteId}",
                      style = MaterialTheme.typography.bodySmall,
                    )
                    Text(
                      "Expires at: ${invite.expiresAt ?: "-"}",
                      style = MaterialTheme.typography.bodySmall,
                    )
                    Button(
                      onClick = { onAcceptInvite(invite) },
                      enabled = acceptingInviteId != invite.inviteId,
                      modifier = Modifier.fillMaxWidth(),
                    ) {
                      Text(
                        if (acceptingInviteId == invite.inviteId) "Accepting..."
                        else "Accept invite"
                      )
                    }
                  }
                }
              }
            }
          }
        }
      }
    },
    confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
  )
}

@Composable
fun BattleSelectionDialog(
  bossesLoading: Boolean,
  bossesError: String?,
  battleError: String?,
  bosses: List<BossData>,
  selectedBoss: BossData?,
  battleStarting: Boolean,
  onSelectBoss: (BossData) -> Unit,
  onStartBattle: () -> Unit,
  onDismiss: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Select a boss to fight") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (bossesLoading) {
          CircularProgressIndicator()
        }

        bossesError?.let { error ->
          Text(
            text = error,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
          )
        }

        battleError?.let { error ->
          Text(
            text = error,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
          )
        }

        if (!bossesLoading && bossesError == null) {
          if (bosses.isEmpty()) {
            Text("No bosses available")
          } else {
            LazyColumn(
              modifier = Modifier.fillMaxWidth().height(300.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              items(bosses, key = { it.type }) { boss ->
                Card(
                  modifier = Modifier.fillMaxWidth(),
                  colors =
                    CardDefaults.cardColors(
                      containerColor =
                        if (selectedBoss?.type == boss.type) {
                          MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        } else {
                          MaterialTheme.colorScheme.surfaceVariant
                        }
                    ),
                ) {
                  Column(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                  ) {
                    Text(
                      boss.name,
                      style = MaterialTheme.typography.titleSmall,
                      fontWeight = FontWeight.Bold,
                    )
                    Text("Health: ${boss.health}", style = MaterialTheme.typography.bodySmall)
                    Text("Strength: ${boss.strength}", style = MaterialTheme.typography.bodySmall)
                    Text("Defense: ${boss.defense}", style = MaterialTheme.typography.bodySmall)
                    Text(
                      "XP Reward: ${boss.experienceReward}",
                      style = MaterialTheme.typography.bodySmall,
                    )
                    Text(
                      "Money Reward: ${boss.moneyReward}",
                      style = MaterialTheme.typography.bodySmall,
                    )
                    Text(
                      "Penalty on Loss: ${boss.penalty}",
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.error,
                    )
                    Button(
                      onClick = { onSelectBoss(boss) },
                      enabled = !battleStarting,
                      modifier = Modifier.fillMaxWidth(),
                    ) {
                      Text(if (selectedBoss?.type == boss.type) "Selected" else "Select")
                    }
                  }
                }
              }
            }
          }
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onStartBattle, enabled = selectedBoss != null && !battleStarting) {
        Text(if (battleStarting) "Starting..." else "Start Battle")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss, enabled = !battleStarting) { Text("Cancel") }
    },
  )
}

@Composable
fun BattleInfoDialog(
  battleStatsLoading: Boolean,
  battleStatsError: String?,
  battleStats: BattleStatsData?,
  canRefresh: Boolean,
  canAttack: Boolean,
  attackLoading: Boolean,
  attackError: String?,
  turnInfo: String?,
  onRefresh: () -> Unit,
  onAttack: () -> Unit,
  onClose: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = onClose,
    title = { Text("Battle info") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (battleStatsLoading) {
          CircularProgressIndicator()
        }

        battleStatsError?.let {
          Text(
            it,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
          )
        }

        turnInfo?.let { Text(it, style = MaterialTheme.typography.bodySmall) }

        attackError?.let {
          Text(
            it,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
          )
        }

        battleStats?.let { stats ->
          Text("Battle ID: ${stats.battleId}", style = MaterialTheme.typography.bodySmall)
          Text("Guild ID: ${stats.guildId}", style = MaterialTheme.typography.bodySmall)
          Text("Status: ${stats.status}", style = MaterialTheme.typography.bodySmall)
          Text("Boss: ${stats.bossName}", style = MaterialTheme.typography.bodySmall)
          Text(
            "Boss remaining HP: ${stats.bossRemainingHealth}",
            style = MaterialTheme.typography.bodySmall,
          )
          Text("Current turn: ${stats.currentTurn}", style = MaterialTheme.typography.bodySmall)
          Text("Total turns: ${stats.totalTurns}", style = MaterialTheme.typography.bodySmall)
        }

        if (canAttack && battleStats != null) {
          Button(onClick = onAttack, enabled = !attackLoading, modifier = Modifier.fillMaxWidth()) {
            Text(if (attackLoading) "Attacking..." else "Attack boss")
          }
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onRefresh, enabled = canRefresh && !battleStatsLoading) {
        Text("Refresh")
      }
    },
    dismissButton = { TextButton(onClick = onClose) { Text("Close") } },
  )
}
