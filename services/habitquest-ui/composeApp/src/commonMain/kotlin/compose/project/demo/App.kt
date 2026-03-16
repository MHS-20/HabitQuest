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

// Pagine disponibili nella nav bar
enum class AppPage(val label: String, val emoji: String) {
    Dashboard("Dashboard", "🏠"),
    Character("Personaggio", "👤"),
    Achievements("Traguardi", "⭐"),
}

@Composable
@Preview
fun App() {
    MaterialTheme {
        var isLoggedIn by remember { mutableStateOf(false) }

        if (!isLoggedIn) {
            LoginScreen(onLoginSuccess = { isLoggedIn = true })
        } else {
            MainScaffold()
        }
    }
}

@Composable
fun MainScaffold() {
    var selectedPage by remember { mutableStateOf(AppPage.Dashboard) }

    Scaffold(
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
                AppPage.Dashboard -> DashboardScreen()
                AppPage.Character -> CharacterScreen()
                AppPage.Achievements -> AchievementsScreen()
            }
        }
    }
}

@Composable
fun DashboardScreen() {
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
