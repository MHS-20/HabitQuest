package compose.project.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun GuildScreen(
    token: String,
    avatarState: AvatarUiState,
) {
    val repository = remember { GuildRepository() }
    val scope = rememberCoroutineScope()

    var guildName by remember { mutableStateOf("") }
    var searchText by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    var leaderboard by remember { mutableStateOf<List<GuildData>>(emptyList()) }
    var showCreateDialog by remember { mutableStateOf(false) }

    suspend fun loadLeaderboard(showLoading: Boolean) {
        if (showLoading) loading = true
        when (val result = repository.fetchLeaderboard(token)) {
            is GuildLeaderboardResult.Success -> leaderboard = result.guilds
            is GuildLeaderboardResult.Error -> statusMessage = result.message
        }
        if (showLoading) loading = false
    }

    LaunchedEffect(token) {
        if (token.isBlank()) return@LaunchedEffect
        loadLeaderboard(showLoading = true)
    }

    val normalizedSearch = searchText.trim().lowercase()
    val filteredGuilds = if (normalizedSearch.isBlank()) {
        leaderboard
    } else {
        leaderboard.filter { guild ->
            guild.name.lowercase().contains(normalizedSearch) || guild.id.lowercase().contains(normalizedSearch)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Guild", style = MaterialTheme.typography.headlineSmall)

        val avatar = (avatarState as? AvatarUiState.Ready)?.avatar
        Text(
            text = if (avatar != null) {
                "Current avatar: ${avatar.name} (${avatar.id})"
            } else {
                "Avatar not available"
            },
            style = MaterialTheme.typography.bodyMedium
        )

        statusMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Button(
            onClick = { showCreateDialog = true },
            enabled = avatar != null && !loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Create guild")
        }

        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            label = { Text("Search guild by name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = {
                    scope.launch {
                        statusMessage = null
                        loadLeaderboard(showLoading = true)
                    }
                },
                enabled = !loading,
                modifier = Modifier.weight(1f)
            ) {
                Text("Refresh")
            }
            TextButton(
                onClick = {
                    scope.launch {
                        statusMessage = null
                        loadLeaderboard(showLoading = true)
                    }
                },
                enabled = !loading,
                modifier = Modifier.weight(1f)
            ) {
                Text("Refresh leaderboard")
            }
        }

        if (loading) {
            CircularProgressIndicator()
        }

        Spacer(Modifier.height(4.dp))
        Text("All guilds", style = MaterialTheme.typography.titleMedium)

        if (filteredGuilds.isEmpty()) {
            Text("No guilds available")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filteredGuilds, key = { it.id }) { guild ->
                    GuildLeaderboardRow(guild = guild)
                }
            }
        }

        if (showCreateDialog) {
            AlertDialog(
                onDismissRequest = { showCreateDialog = false },
                title = { Text("Create new guild") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = guildName,
                            onValueChange = {
                                guildName = it
                                statusMessage = null
                            },
                            label = { Text("Guild name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "The current avatar will be used as the creator.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val currentAvatar = (avatarState as? AvatarUiState.Ready)?.avatar
                            if (currentAvatar == null) {
                                statusMessage = "Avatar not available"
                                return@TextButton
                            }
                            if (guildName.isBlank()) {
                                statusMessage = "Enter a guild name"
                                return@TextButton
                            }
                            scope.launch {
                                loading = true
                                statusMessage = null
                                when (
                                    val result = repository.createGuild(
                                        token = token,
                                        name = guildName.trim(),
                                        creatorAvatarId = currentAvatar.id,
                                        creatorNickname = currentAvatar.name,
                                    )
                                ) {
                                    is GuildCreateResult.Success -> {
                                        statusMessage = "Guild created: ${result.guildId}"
                                        guildName = ""
                                        showCreateDialog = false
                                        loadLeaderboard(showLoading = false)
                                    }

                                    is GuildCreateResult.Error -> statusMessage = result.message
                                }
                                loading = false
                            }
                        },
                        enabled = !loading,
                    ) {
                        Text("Create")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun GuildCard(guild: GuildData) {
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
        }
    }
}

@Composable
private fun GuildLeaderboardRow(guild: GuildData) {
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
        }
    }
}
