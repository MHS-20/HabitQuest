package compose.project.demo

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import compose.project.demo.contexts.auth.application.LoginUseCase
import compose.project.demo.contexts.auth.application.RegisterUseCase
import compose.project.demo.contexts.auth.domain.model.AuthResult
import compose.project.demo.contexts.auth.infrastructure.repository.AuthRepository
import compose.project.demo.contexts.auth.presentation.screen.LoginScreen
import compose.project.demo.contexts.habits.infrastructure.calendar.HabitCalendarLauncher
import compose.project.demo.contexts.habits.infrastructure.calendar.defaultHabitCalendarLauncher
import compose.project.demo.contexts.auth.presentation.screen.RegisterScreen
import kotlinx.coroutines.launch

enum class AppPage(
    val label: String,
    val emoji: String,
) {
    Dashboard("Dashboard", "🏠"),
    Habits("Habits", "📝"),
    Quest("Quest", "🎯"),
    Guild("Guild", "🛡️"),
    Marketplace("Marketplace", "🛒"),
    Character("Character", "👤"),
}

private enum class AuthRoute {
    Login,
    Register,
    Main,
}

@Composable
@Preview
fun App(habitCalendarLauncher: HabitCalendarLauncher = defaultHabitCalendarLauncher()) {
    MaterialTheme {
        val authRepository = remember { AuthRepository() }
        val loginUseCase = remember { LoginUseCase(authRepository) }
        val registerUseCase = remember { RegisterUseCase(authRepository) }
        val scope = rememberCoroutineScope()

        var authRoute by remember { mutableStateOf(AuthRoute.Login) }
        var authLoading by remember { mutableStateOf(false) }
        var authError by remember { mutableStateOf<String?>(null) }
        var authToken by remember { mutableStateOf<String?>(null) }
        var authUserId by remember { mutableStateOf<String?>(null) }

        when (authRoute) {
            AuthRoute.Login ->
                LoginScreen(
                    isLoading = authLoading,
                    errorMessage = authError,
                    onLogin = { email, password ->
                        scope.launch {
                            authLoading = true
                            authError = null
                            when (val result = loginUseCase(email, password)) {
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
                    },
                )

            AuthRoute.Register ->
                RegisterScreen(
                    isLoading = authLoading,
                    errorMessage = authError,
                    onRegister = { name, email, password ->
                        scope.launch {
                            authLoading = true
                            authError = null
                            when (val result = registerUseCase(name, email, password)) {
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
                    },
                )

            AuthRoute.Main ->
                MainScaffold(
                    onLogout = {
                        authToken = null
                        authUserId = null
                        authError = null
                        authRoute = AuthRoute.Login
                    },
                    token = authToken.orEmpty(),
                    userId = authUserId.orEmpty(),
                    habitCalendarLauncher = habitCalendarLauncher,
                )

        }
    }
}