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
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
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
    var showInviteDialog by remember { mutableStateOf(false) }
    var selectedGuildForInvite by remember { mutableStateOf<GuildData?>(null) }
    var inviteSearchText by remember { mutableStateOf("") }
    var inviteSearchResult by remember { mutableStateOf<SearchAvatarData?>(null) }
    var inviteLoading by remember { mutableStateOf(false) }
    var inviteError by remember { mutableStateOf<String?>(null) }
    var showPendingInvitesDialog by remember { mutableStateOf(false) }
    var pendingInvites by remember { mutableStateOf<List<PendingInviteData>>(emptyList()) }
    var pendingInvitesLoading by remember { mutableStateOf(false) }
    var pendingInvitesError by remember { mutableStateOf<String?>(null) }
    var pendingInvitesMessage by remember { mutableStateOf<String?>(null) }
    var acceptingInviteId by remember { mutableStateOf<String?>(null) }
    var selectedTabIndex by remember { mutableStateOf(0) }

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

    val avatar = (avatarState as? AvatarUiState.Ready)?.avatar
    val avatarId = avatar?.id

    val normalizedSearch = searchText.trim().lowercase()
    val filteredGuilds = if (normalizedSearch.isBlank()) {
        leaderboard
    } else {
        leaderboard.filter { guild ->
            guild.name.lowercase().contains(normalizedSearch) || guild.id.lowercase().contains(normalizedSearch)
        }
    }
    val joinedGuilds = if (avatarId.isNullOrBlank()) {
        emptyList()
    } else {
        leaderboard.filter { guild ->
            guild.members.any { member -> member.avatarId == avatarId }
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

        Button(
            onClick = {
                val currentAvatar = (avatarState as? AvatarUiState.Ready)?.avatar
                if (currentAvatar == null) {
                    statusMessage = "Avatar not available"
                    return@Button
                }

                showPendingInvitesDialog = true
                scope.launch {
                    pendingInvitesLoading = true
                    pendingInvitesError = null
                    pendingInvitesMessage = null
                    when (val result = repository.fetchPendingInvites(token, currentAvatar.id)) {
                        is PendingInvitesResult.Success -> pendingInvites = result.invites
                        is PendingInvitesResult.Error -> {
                            pendingInvites = emptyList()
                            pendingInvitesError = result.message
                        }
                    }
                    pendingInvitesLoading = false
                }
            },
            enabled = avatar != null && !loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Pending invites")
        }

        PrimaryTabRow(selectedTabIndex = selectedTabIndex) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                text = { Text("All guilds") }
            )
            Tab(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
                text = { Text("Joined guild") }
            )
        }

        if (selectedTabIndex == 0) {
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                label = { Text("Search guild by name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

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
        if (selectedTabIndex == 0) {
            Text("All guilds", style = MaterialTheme.typography.titleMedium)

            if (filteredGuilds.isEmpty()) {
                Text("No guilds available")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filteredGuilds, key = { it.id }) { guild ->
                        GuildLeaderboardRow(
                            guild = guild,
                            onInviteClick = {
                                selectedGuildForInvite = guild
                                showInviteDialog = true
                                inviteSearchText = ""
                                inviteSearchResult = null
                                inviteError = null
                            }
                        )
                    }
                }
            }
        } else {
            Text("Joined guild", style = MaterialTheme.typography.titleMedium)

            if (avatarId.isNullOrBlank()) {
                Text("Avatar not available")
            } else if (joinedGuilds.isEmpty()) {
                Text("You have not joined any guild yet")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(joinedGuilds, key = { it.id }) { guild ->
                        GuildCard(guild = guild)
                    }
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

        if (showInviteDialog && selectedGuildForInvite != null) {
            AlertDialog(
                onDismissRequest = {
                    showInviteDialog = false
                    inviteSearchText = ""
                    inviteSearchResult = null
                    inviteError = null
                },
                title = { Text("Invite avatar to ${selectedGuildForInvite!!.name}") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = inviteSearchText,
                            onValueChange = {
                                inviteSearchText = it
                                inviteError = null
                            },
                            label = { Text("Search avatar by name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                scope.launch {
                                    inviteLoading = true
                                    inviteError = null
                                    when (val result = repository.searchAvatar(token, inviteSearchText)) {
                                        is SearchAvatarResult.Success -> {
                                            inviteSearchResult = result.avatar
                                        }

                                        is SearchAvatarResult.Error -> {
                                            inviteError = result.message
                                            inviteSearchResult = null
                                        }
                                    }
                                    inviteLoading = false
                                }
                            },
                            enabled = inviteSearchText.isNotBlank() && !inviteLoading,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Search")
                        }

                        inviteError?.let { error ->
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        inviteSearchResult?.let { avatar ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(avatar.name, style = MaterialTheme.typography.titleSmall)
                                    Text("ID: ${avatar.id}", style = MaterialTheme.typography.bodySmall)
                                    Button(
                                        onClick = {
                                            scope.launch {
                                                val currentAvatarId = (avatarState as? AvatarUiState.Ready)?.avatar?.id
                                                if (currentAvatarId.isNullOrBlank()) {
                                                    inviteError = "Current avatar not available"
                                                    return@launch
                                                }

                                                inviteLoading = true
                                                inviteError = null
                                                when (
                                                    val result = repository.inviteAvatarToGuild(
                                                        token = token,
                                                        guildId = selectedGuildForInvite!!.id,
                                                        requestorId = currentAvatarId,
                                                        avatarId = avatar.id
                                                    )
                                                ) {
                                                    is InviteAvatarResult.Success -> {
                                                        inviteError = "Invite sent successfully!"
                                                        inviteSearchText = ""
                                                        inviteSearchResult = null
                                                        loadLeaderboard(showLoading = false)
                                                    }

                                                    is InviteAvatarResult.Error -> {
                                                        inviteError = result.message
                                                    }
                                                }
                                                inviteLoading = false
                                            }
                                        },
                                        enabled = !inviteLoading,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Send Invite")
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showInviteDialog = false
                            inviteSearchText = ""
                            inviteSearchResult = null
                            inviteError = null
                        }
                    ) {
                        Text("Close")
                    }
                }
            )
        }

        if (showPendingInvitesDialog) {
            AlertDialog(
                onDismissRequest = {
                    showPendingInvitesDialog = false
                    pendingInvites = emptyList()
                    pendingInvitesError = null
                    pendingInvitesMessage = null
                    acceptingInviteId = null
                },
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
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        pendingInvitesMessage?.let { message ->
                            Text(
                                text = message,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        if (!pendingInvitesLoading && pendingInvitesError == null) {
                            if (pendingInvites.isEmpty()) {
                                Text("No pending invites")
                            } else {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(220.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(pendingInvites, key = { it.inviteId }) { invite ->
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(8.dp),
                                                verticalArrangement = Arrangement.spacedBy(2.dp)
                                            ) {
                                                Text(invite.guildName, style = MaterialTheme.typography.titleSmall)
                                                Text("Guild ID: ${invite.guildId}", style = MaterialTheme.typography.bodySmall)
                                                Text("Invite ID: ${invite.inviteId}", style = MaterialTheme.typography.bodySmall)
                                                Text(
                                                    "Expires at: ${invite.expiresAt ?: "-"}",
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                                Button(
                                                    onClick = {
                                                        if (avatarId.isNullOrBlank()) {
                                                            pendingInvitesError = "Avatar not available"
                                                            return@Button
                                                        }
                                                        val avatarNickname = avatar?.name?.takeIf { it.isNotBlank() }
                                                        if (avatarNickname == null) {
                                                            pendingInvitesError = "Avatar nickname not available"
                                                            return@Button
                                                        }

                                                        scope.launch {
                                                            acceptingInviteId = invite.inviteId
                                                            pendingInvitesError = null
                                                            pendingInvitesMessage = null
                                                            when (
                                                                val result = repository.acceptInvite(
                                                                    token = token,
                                                                    avatarId = avatarId,
                                                                    inviteId = invite.inviteId,
                                                                    guildId = invite.guildId,
                                                                    nickname = avatarNickname
                                                                )
                                                            ) {
                                                                is AcceptInviteResult.Success -> {
                                                                    pendingInvites = pendingInvites.filterNot { it.inviteId == invite.inviteId }
                                                                    pendingInvitesMessage = "Invite accepted"
                                                                    statusMessage = null
                                                                    loadLeaderboard(showLoading = false)
                                                                }

                                                                is AcceptInviteResult.Error -> {
                                                                    pendingInvitesError = result.message
                                                                }
                                                            }
                                                            acceptingInviteId = null
                                                        }
                                                    },
                                                    enabled = acceptingInviteId != invite.inviteId,
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Text(
                                                        if (acceptingInviteId == invite.inviteId) "Accepting..." else "Accept invite"
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
                confirmButton = {
                    TextButton(onClick = { showPendingInvitesDialog = false }) {
                        Text("Close")
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
private fun GuildLeaderboardRow(guild: GuildData, onInviteClick: () -> Unit) {
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
