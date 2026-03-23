package compose.project.demo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

// Pagine disponibili nella nav bar
enum class AppPage(val label: String, val emoji: String) {
    Dashboard("Dashboard", "🏠"),
    Character("Personaggio", "👤"),
    Achievements("Traguardi", "⭐"),
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
                    authError = null
                    authRoute = AuthRoute.Login
                },
                token = authToken.orEmpty()
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun MainScaffold(onLogout: () -> Unit, token: String) {
    var selectedPage by remember { mutableStateOf(AppPage.Dashboard) }

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
                AppPage.Dashboard -> DashboardScreen(token)
                AppPage.Character -> CharacterScreen()
                AppPage.Achievements -> AchievementsScreen()
            }
        }
    }
}

@Composable
fun DashboardScreen(token: String) {
    var showContent by remember { mutableStateOf(true) }
    val stats = remember { StatsState() }

    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .safeContentPadding()
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("RPG Dashboard", style = MaterialTheme.typography.headlineSmall)
        if (token.isNotBlank()) {
            Text(
                text = "Sessione attiva",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(Modifier.height(12.dp))

        Button(onClick = { showContent = !showContent }) {
            Text(if (showContent) "Nascondi" else "Mostra")
        }

        AnimatedVisibility(showContent) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(12.dp))
                CharacterCard(name = "Aria", stats = stats)
                Spacer(Modifier.height(24.dp))
                Text("Suggerimento: usa i pulsanti per cambiare le statistiche.")
            }
        }
    }
}

@Composable
fun CharacterScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "👤 Pagina Personaggio",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun AchievementsScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "⭐ Pagina Traguardi",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}
