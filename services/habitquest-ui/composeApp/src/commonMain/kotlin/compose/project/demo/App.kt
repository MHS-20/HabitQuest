package compose.project.demo

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

// Pagine disponibili nella nav bar
enum class AppPage(val label: String, val emoji: String) {
    Dashboard("Dashboard", "🏠"),
    Habits("Abitudini", "📝"),
    Quest("Quest", "🎯"),
    Marketplace("Marketplace", "🛒"),
    Character("Personaggio", "👤"),
    HabitHistory("Storico", "🕒"),
}

private enum class AuthRoute {
    Login,
    Register,
    Main,
}

@Composable
@Preview
fun App() {
    MaterialTheme {
        val authRepository = remember { AuthRepository() }
        val scope = rememberCoroutineScope()

        var authRoute by remember { mutableStateOf(AuthRoute.Login) }
        var authLoading by remember { mutableStateOf(false) }
        var authError by remember { mutableStateOf<String?>(null) }
        var authToken by remember { mutableStateOf<String?>(null) }
        var authUserId by remember { mutableStateOf<String?>(null) }

        when (authRoute) {
            AuthRoute.Login -> LoginScreen(
                isLoading = authLoading,
                errorMessage = authError,
                onLogin = { email, password ->
                    scope.launch {
                        authLoading = true
                        authError = null
                        when (val result = authRepository.login(email, password)) {
                            is AuthResult.Success -> {
                                authToken = result.token
                                authUserId = result.userId
                                authRoute = AuthRoute.Main
                            }

                            is AuthResult.Error -> {
                                authError = result.message
                            }
                        }
                        authLoading = false
                    }
                },
                onNavigateRegister = {
                    authError = null
                    authRoute = AuthRoute.Register
                }
            )

            AuthRoute.Register -> RegisterScreen(
                isLoading = authLoading,
                errorMessage = authError,
                onRegister = { name, email, password ->
                    scope.launch {
                        authLoading = true
                        authError = null
                        when (val result = authRepository.register(name, email, password)) {
                            is AuthResult.Success -> {
                                authToken = result.token
                                authUserId = result.userId
                                authRoute = AuthRoute.Main
                            }

                            is AuthResult.Error -> {
                                authError = result.message
                            }
                        }
                        authLoading = false
                    }
                },
                onNavigateLogin = {
                    authError = null
                    authRoute = AuthRoute.Login
                }
            )

            AuthRoute.Main -> MainScaffold(
                onLogout = {
                    authToken = null
                    authUserId = null
                    authError = null
                    authRoute = AuthRoute.Login
                },
                token = authToken.orEmpty(),
                userId = authUserId.orEmpty()
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun MainScaffold(onLogout: () -> Unit, token: String, userId: String) {
    var selectedPage by remember { mutableStateOf(AppPage.Dashboard) }
    val avatarRepository = remember { AvatarRepository() }
    var avatarState by remember { mutableStateOf<AvatarUiState>(AvatarUiState.Loading) }
    var avatarRefreshTick by remember { mutableIntStateOf(0) }

    fun requestAvatarRefresh() {
        avatarRefreshTick += 1
    }

    LaunchedEffect(token, userId, avatarRefreshTick) {
        avatarState = AvatarUiState.Loading
        avatarState = when {
            token.isBlank() -> AvatarUiState.Error("Sessione non valida")
            userId.isBlank() -> AvatarUiState.Error("Utente non disponibile nella sessione")
            else -> when (val result = avatarRepository.fetchAvatar(avatarId = userId, token = token)) {
                is AvatarResult.Success -> AvatarUiState.Ready(result.avatar)
                is AvatarResult.Error -> AvatarUiState.Error(result.message)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("HabitQuest") },
                actions = {
                    TextButton(onClick = onLogout) {
                        Text("Logout")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 4.dp
            ) {
                AppPage.entries.forEach { page ->
                    NavigationBarItem(
                        selected = selectedPage == page,
                        onClick = { selectedPage = page },
                        icon = { Text(page.emoji) },
                        label = { Text(page.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedPage) {
                AppPage.Dashboard -> DashboardScreen(token = token, avatarState = avatarState)
                AppPage.Habits -> HabitsScreen(
                    token = token,
                    avatarState = avatarState,
                    onHabitAttended = ::requestAvatarRefresh,
                )
                AppPage.Quest -> QuestScreen(token = token, avatarState = avatarState)
                AppPage.Marketplace -> MarketplaceScreen(
                    token = token,
                    avatarState = avatarState,
                    onItemBought = ::requestAvatarRefresh,
                )
                AppPage.Character -> CharacterScreen(avatarState = avatarState)
                AppPage.HabitHistory -> HabitHistoryScreen(token = token, avatarState = avatarState)
            }
        }
    }
}

sealed interface AvatarUiState {
    data object Loading : AvatarUiState
    data class Ready(val avatar: AvatarData) : AvatarUiState
    data class Error(val message: String) : AvatarUiState
}
