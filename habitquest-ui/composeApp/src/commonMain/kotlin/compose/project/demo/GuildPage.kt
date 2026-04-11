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
import androidx.compose.material3.Button
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
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun GuildScreen(token: String, avatarState: AvatarUiState) {
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

  var showBattleDialog by remember { mutableStateOf(false) }
  var bosses by remember { mutableStateOf<List<BossData>>(emptyList()) }
  var selectedBoss by remember { mutableStateOf<BossData?>(null) }
  var bossesLoading by remember { mutableStateOf(false) }
  var bossesError by remember { mutableStateOf<String?>(null) }
  var battleStarting by remember { mutableStateOf(false) }
  var battleError by remember { mutableStateOf<String?>(null) }
  var activeBattleGuildId by remember { mutableStateOf<String?>(null) }
  var showBattleInfoDialog by remember { mutableStateOf(false) }
  var battleStats by remember { mutableStateOf<BattleStatsData?>(null) }
  var battleStatsLoading by remember { mutableStateOf(false) }
  var battleStatsError by remember { mutableStateOf<String?>(null) }
  var battleAttackLoading by remember { mutableStateOf(false) }
  var battleAttackError by remember { mutableStateOf<String?>(null) }

  suspend fun loadLeaderboard(showLoading: Boolean) {
    if (showLoading) loading = true
    when (val result = repository.fetchLeaderboard(token)) {
      is GuildLeaderboardResult.Success -> leaderboard = result.guilds
      is GuildLeaderboardResult.Error -> statusMessage = result.message
    }
    if (showLoading) loading = false
  }

  suspend fun loadBattleStats(guildId: String) {
    battleStatsLoading = true
    battleStatsError = null
    battleAttackError = null
    when (val result = repository.fetchBattleStatsByGuild(token, guildId)) {
      is BattleStatsResult.Success -> battleStats = result.stats
      is BattleStatsResult.Error -> {
        battleStats = null
        battleStatsError = result.message
      }
    }
    battleStatsLoading = false
  }

  LaunchedEffect(token) {
    if (token.isBlank()) return@LaunchedEffect
    loadLeaderboard(showLoading = true)
  }

  val avatar = (avatarState as? AvatarUiState.Ready)?.avatar
  val avatarId = avatar?.id

  val normalizedSearch = searchText.trim().lowercase()
  val filteredGuilds =
    if (normalizedSearch.isBlank()) {
      leaderboard
    } else {
      leaderboard.filter { guild ->
        guild.name.lowercase().contains(normalizedSearch) ||
          guild.id.lowercase().contains(normalizedSearch)
      }
    }
  val joinedGuilds =
    if (avatarId.isNullOrBlank()) {
      emptyList()
    } else {
      leaderboard.filter { guild -> guild.members.any { member -> member.avatarId == avatarId } }
    }

  val activeBattleGuild =
    activeBattleGuildId?.let { guildId -> joinedGuilds.firstOrNull { it.id == guildId } }
      ?: joinedGuilds.firstOrNull()
  val battleIsActive = battleStats != null && battleStats!!.bossRemainingHealth > 0
  val currentTurnMemberId =
    activeBattleGuild?.members?.getOrNull((battleStats?.currentTurn ?: -1))?.avatarId
  val canAttackNow = battleIsActive && avatarId != null && currentTurnMemberId == avatarId
  val turnInfo =
    when {
      battleStats == null -> null
      battleIsActive && currentTurnMemberId == avatarId -> "It's your turn"
      battleIsActive -> "Waiting for avatar: ${currentTurnMemberId ?: "unknown"}"
      else -> "Battle is ${battleStats?.status?.uppercase() ?: "UNKNOWN"}"
    }

  Column(
    modifier =
      Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    Text("Guild", style = MaterialTheme.typography.headlineSmall)

    Text(
      text =
        if (avatar != null) {
          "Current avatar: ${avatar.name} (${avatar.id})"
        } else {
          "Avatar not available"
        },
      style = MaterialTheme.typography.bodyMedium,
    )

    statusMessage?.let { message ->
      Text(
        text = message,
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodyMedium,
      )
    }

    Button(
      onClick = { showCreateDialog = true },
      enabled = avatar != null && !loading,
      modifier = Modifier.fillMaxWidth(),
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
      modifier = Modifier.fillMaxWidth(),
    ) {
      Text("Pending invites")
    }

    PrimaryTabRow(selectedTabIndex = selectedTabIndex) {
      Tab(
        selected = selectedTabIndex == 0,
        onClick = { selectedTabIndex = 0 },
        text = { Text("All guilds") },
      )
      Tab(
        selected = selectedTabIndex == 1,
        onClick = { selectedTabIndex = 1 },
        text = { Text("Joined guild") },
      )
    }

    if (selectedTabIndex == 0) {
      OutlinedTextField(
        value = searchText,
        onValueChange = { searchText = it },
        label = { Text("Search guild by name") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
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
        modifier = Modifier.weight(1f),
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
        modifier = Modifier.weight(1f),
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
              },
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
        val joinedGuild = joinedGuilds.first()
        val isLeader =
          joinedGuild.members.any { member ->
            member.avatarId == avatarId && member.role.uppercase() == "LEADER"
          }

        Button(
          onClick = {
            scope.launch {
              when (val result = repository.fetchBattleStatsByGuild(token, joinedGuild.id)) {
                is BattleStatsResult.Success -> {
                  activeBattleGuildId = joinedGuild.id
                  battleStats = result.stats
                  battleStatsError = null
                  showBattleInfoDialog = true
                }

                is BattleStatsResult.Error -> {
                  if (isLeader) {
                    showBattleDialog = true
                    bossesLoading = true
                    bossesError = null
                    selectedBoss = null
                    when (val bossesResult = repository.fetchBosses(token)) {
                      is BossesResult.Success -> bosses = bossesResult.bosses
                      is BossesResult.Error -> {
                        bossesError = bossesResult.message
                        bosses = emptyList()
                      }
                    }
                    bossesLoading = false
                  } else {
                    statusMessage = result.message
                  }
                }
              }
            }
          },
          enabled = !loading,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Text(if (isLeader) "Start battle / Battle info" else "Battle info")
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          items(joinedGuilds, key = { it.id }) { guild ->
            GuildCard(
              guild = guild,
              isLeader =
                guild.members.any { member ->
                  member.avatarId == avatarId && member.role.uppercase() == "LEADER"
                },
            )
          }
        }
      }
    }

    if (showCreateDialog) {
      CreateGuildDialog(
        guildName = guildName,
        onGuildNameChange = {
          guildName = it
          statusMessage = null
        },
        loading = loading,
        onCreate = {
          val currentAvatar = (avatarState as? AvatarUiState.Ready)?.avatar
          if (currentAvatar == null) {
            statusMessage = "Avatar not available"
            return@CreateGuildDialog
          }
          if (guildName.isBlank()) {
            statusMessage = "Enter a guild name"
            return@CreateGuildDialog
          }
          scope.launch {
            loading = true
            statusMessage = null
            when (
              val result =
                repository.createGuild(
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
        onDismiss = { showCreateDialog = false },
      )
    }

    if (showInviteDialog && selectedGuildForInvite != null) {
      InviteAvatarDialog(
        guildName = selectedGuildForInvite!!.name,
        inviteSearchText = inviteSearchText,
        onInviteSearchTextChange = {
          inviteSearchText = it
          inviteError = null
        },
        inviteLoading = inviteLoading,
        inviteError = inviteError,
        inviteSearchResult = inviteSearchResult,
        onSearch = {
          scope.launch {
            inviteLoading = true
            inviteError = null
            when (val result = repository.searchAvatar(token, inviteSearchText)) {
              is SearchAvatarResult.Success -> inviteSearchResult = result.avatar
              is SearchAvatarResult.Error -> {
                inviteError = result.message
                inviteSearchResult = null
              }
            }
            inviteLoading = false
          }
        },
        onSendInvite = { avatar ->
          scope.launch {
            val currentAvatarId = (avatarState as? AvatarUiState.Ready)?.avatar?.id
            if (currentAvatarId.isNullOrBlank()) {
              inviteError = "Current avatar not available"
              return@launch
            }

            inviteLoading = true
            inviteError = null
            when (
              val result =
                repository.inviteAvatarToGuild(
                  token = token,
                  guildId = selectedGuildForInvite!!.id,
                  requestorId = currentAvatarId,
                  avatarId = avatar.id,
                )
            ) {
              is InviteAvatarResult.Success -> {
                inviteError = "Invite sent successfully!"
                inviteSearchText = ""
                inviteSearchResult = null
                loadLeaderboard(showLoading = false)
              }

              is InviteAvatarResult.Error -> inviteError = result.message
            }
            inviteLoading = false
          }
        },
        onDismiss = {
          showInviteDialog = false
          inviteSearchText = ""
          inviteSearchResult = null
          inviteError = null
        },
      )
    }

    if (showPendingInvitesDialog) {
      PendingInvitesDialog(
        pendingInvitesLoading = pendingInvitesLoading,
        pendingInvitesError = pendingInvitesError,
        pendingInvitesMessage = pendingInvitesMessage,
        pendingInvites = pendingInvites,
        acceptingInviteId = acceptingInviteId,
        onAcceptInvite = { invite ->
          if (avatarId.isNullOrBlank()) {
            pendingInvitesError = "Avatar not available"
            return@PendingInvitesDialog
          }
          val avatarNickname = avatar?.name?.takeIf { it.isNotBlank() }
          if (avatarNickname == null) {
            pendingInvitesError = "Avatar nickname not available"
            return@PendingInvitesDialog
          }

          scope.launch {
            acceptingInviteId = invite.inviteId
            pendingInvitesError = null
            pendingInvitesMessage = null
            when (
              val result =
                repository.acceptInvite(
                  token = token,
                  avatarId = avatarId,
                  inviteId = invite.inviteId,
                  guildId = invite.guildId,
                  nickname = avatarNickname,
                )
            ) {
              is AcceptInviteResult.Success -> {
                pendingInvites = pendingInvites.filterNot { it.inviteId == invite.inviteId }
                pendingInvitesMessage = "Invite accepted"
                statusMessage = null
                loadLeaderboard(showLoading = false)
              }

              is AcceptInviteResult.Error -> pendingInvitesError = result.message
            }
            acceptingInviteId = null
          }
        },
        onDismiss = {
          showPendingInvitesDialog = false
          pendingInvites = emptyList()
          pendingInvitesError = null
          pendingInvitesMessage = null
          acceptingInviteId = null
        },
      )
    }

    if (showBattleDialog) {
      BattleSelectionDialog(
        bossesLoading = bossesLoading,
        bossesError = bossesError,
        battleError = battleError,
        bosses = bosses,
        selectedBoss = selectedBoss,
        battleStarting = battleStarting,
        onSelectBoss = { selectedBoss = it },
        onStartBattle = {
          if (selectedBoss != null) {
            val currentJoinedGuild = joinedGuilds.firstOrNull()
            if (currentJoinedGuild != null && avatarId != null) {
              scope.launch {
                battleStarting = true
                battleError = null
                when (
                  val result =
                    repository.initiateBattle(
                      token = token,
                      guildId = currentJoinedGuild.id,
                      requesterId = avatarId,
                      bossType = selectedBoss!!.type,
                    )
                ) {
                  is BattleStartResult.Success -> {
                    statusMessage = "Battle started! Battle ID: ${result.battleId}"
                    activeBattleGuildId = currentJoinedGuild.id
                    selectedBoss = null
                    bosses = emptyList()
                    showBattleDialog = false
                    loadBattleStats(currentJoinedGuild.id)
                    showBattleInfoDialog = true
                    loadLeaderboard(showLoading = false)
                  }
                  is BattleStartResult.Error -> {
                    battleError = result.message
                  }
                }
                battleStarting = false
              }
            }
          }
        },
        onDismiss = {
          showBattleDialog = false
          bosses = emptyList()
          selectedBoss = null
          bossesError = null
          battleError = null
        },
      )
    }

    if (showBattleInfoDialog) {
      val guildIdForStats = activeBattleGuildId ?: joinedGuilds.firstOrNull()?.id
      val normalizedBattleStats =
        battleStats?.let { stats ->
          if (stats.bossRemainingHealth > 0) stats.copy(status = "ONGOING") else stats
        }
      BattleInfoDialog(
        battleStatsLoading = battleStatsLoading,
        battleStatsError = battleStatsError,
        battleStats = normalizedBattleStats,
        canRefresh = guildIdForStats != null,
        canAttack = canAttackNow,
        attackLoading = battleAttackLoading,
        attackError = battleAttackError,
        turnInfo = turnInfo,
        onRefresh = {
          if (guildIdForStats != null) {
            scope.launch { loadBattleStats(guildIdForStats) }
          }
        },
        onAttack = {
          val stats = battleStats
          if (stats == null || avatarId == null) {
            battleAttackError = "Battle or avatar not available"
            return@BattleInfoDialog
          }
          if (!canAttackNow) {
            battleAttackError = "It's not your turn"
            return@BattleInfoDialog
          }
          val damage = (avatar?.strength ?: 10).coerceAtLeast(1)
          scope.launch {
            battleAttackLoading = true
            battleAttackError = null
            when (
              val result =
                repository.attackBattle(
                  token = token,
                  battleId = stats.battleId,
                  attackerAvatarId = avatarId,
                  damage = damage,
                )
            ) {
              is BattleAttackResult.Success -> {
                if (guildIdForStats != null) {
                  loadBattleStats(guildIdForStats)
                }
              }

              is BattleAttackResult.Error -> {
                battleAttackError = result.message
              }
            }
            battleAttackLoading = false
          }
        },
        onClose = { showBattleInfoDialog = false },
      )
    }
  }
}
