package compose.project.demo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DashboardScreen(token: String, avatarState: AvatarUiState) {
    var showContent by remember { mutableStateOf(true) }

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
                when (val state = avatarState) {
                    AvatarUiState.Loading -> Text("Caricamento avatar in corso...")
                    is AvatarUiState.Error -> Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error
                    )

                    is AvatarUiState.Ready -> AvatarCard(avatar = state.avatar)
                }
                Spacer(Modifier.height(24.dp))
                Text("Dati avatar aggiornati da avatar-service")
            }
        }
    }
}

