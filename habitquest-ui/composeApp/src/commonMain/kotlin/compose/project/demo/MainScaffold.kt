package compose.project.demo

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import compose.project.demo.contexts.avatar.domain.model.AvatarData
import compose.project.demo.contexts.avatar.domain.model.AvatarResult
import compose.project.demo.contexts.avatar.infrastructure.repository.AvatarRepository
import compose.project.demo.contexts.avatar.presentation.screen.CharacterScreen
import compose.project.demo.contexts.dashboard.presentation.screen.DashboardScreen
import compose.project.demo.contexts.guild.presentation.screen.GuildScreen
import compose.project.demo.contexts.habits.domain.model.HabitCalendarEntry
import compose.project.demo.contexts.habits.presentation.screen.HabitsScreen
import compose.project.demo.contexts.marketplace.presentation.screen.MarketplaceScreen
import compose.project.demo.contexts.quest.presentation.screen.QuestScreen

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun MainScaffold(
    onLogout: () -> Unit,
    token: String,
    userId: String,
    habitCalendarLauncher: (HabitCalendarEntry) -> Unit,
) {
    var selectedPage by remember { mutableStateOf(AppPage.Dashboard) }
    val avatarRepository = remember { AvatarRepository() }
    var avatarState by remember { mutableStateOf<AvatarUiState>(AvatarUiState.Loading) }
    var avatarRefreshTick by remember { mutableIntStateOf(0) }
    var questProgressRefreshTick by remember { mutableIntStateOf(0) }

    fun requestAvatarRefresh() {
        avatarRefreshTick += 1
    }

    fun requestQuestProgressRefresh() {
        questProgressRefreshTick += 1
    }

    fun applyMoneyDelta(delta: Int) {
        if (delta == 0) return
        val readyState = avatarState as? AvatarUiState.Ready ?: return
        avatarState =
            readyState.copy(
                avatar = readyState.avatar.copy(money = (readyState.avatar.money + delta).coerceAtLeast(0)),
            )
    }

    LaunchedEffect(token, userId, avatarRefreshTick) {
        avatarState = AvatarUiState.Loading
        avatarState =
            when {
                token.isBlank() -> {
                    AvatarUiState.Error("Invalid session")
                }

                userId.isBlank() -> {
                    AvatarUiState.Error("User not available in session")
                }

                else -> {
                    when (val result = avatarRepository.fetchAvatar(avatarId = userId, token = token)) {
                        is AvatarResult.Success -> AvatarUiState.Ready(result.avatar)
                        is AvatarResult.Error -> AvatarUiState.Error(result.message)
                    }
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("HabitQuest") },
                actions = { TextButton(onClick = onLogout) { Text("Logout") } },
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 4.dp,
            ) {
                AppPage.entries.forEach { page ->
                    NavigationBarItem(
                        selected = selectedPage == page,
                        onClick = { selectedPage = page },
                        icon = { Text(page.emoji) },
                        label = { Text(page.label) },
                        colors =
                            NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            ),
                    )
                }
            }
        },
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (selectedPage) {
                AppPage.Dashboard -> {
                    DashboardScreen(
                        token = token,
                        avatarState = avatarState,
                        onRefreshStats = ::requestAvatarRefresh,
                    )
                }

                AppPage.Habits -> {
                    HabitsScreen(
                        token = token,
                        avatarState = avatarState,
                        onHabitAttended = {
                            requestAvatarRefresh()
                            requestQuestProgressRefresh()
                        },
                        habitCalendarLauncher = habitCalendarLauncher,
                    )
                }

                AppPage.Quest -> {
                    QuestScreen(
                        token = token,
                        avatarState = avatarState,
                        progressRefreshTick = questProgressRefreshTick,
                    )
                }

                AppPage.Guild -> {
                    GuildScreen(token = token, avatarState = avatarState)
                }

                AppPage.Marketplace -> {
                    MarketplaceScreen(
                        token = token,
                        avatarState = avatarState,
                        onItemBought = { spentAmount ->
                            applyMoneyDelta(-spentAmount)
                            requestAvatarRefresh()
                        },
                    )
                }

                AppPage.Character -> {
                    CharacterScreen(
                        token = token,
                        avatarState = avatarState,
                        onMoneyDelta = ::applyMoneyDelta,
                        onAvatarRefresh = ::requestAvatarRefresh,
                    )
                }
            }
        }
    }
}

sealed interface AvatarUiState {
    data object Loading : AvatarUiState

    data class Ready(
        val avatar: AvatarData,
    ) : AvatarUiState

    data class Error(
        val message: String,
    ) : AvatarUiState
}
