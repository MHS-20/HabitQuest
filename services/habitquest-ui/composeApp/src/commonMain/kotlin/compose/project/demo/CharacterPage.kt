package compose.project.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CharacterScreen(avatarState: AvatarUiState) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (val state = avatarState) {
            AvatarUiState.Loading -> Text(
                text = "Caricamento personaggio...",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.align(Alignment.Center)
            )

            is AvatarUiState.Error -> Text(
                text = state.message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.align(Alignment.Center)
            )

            is AvatarUiState.Ready -> Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("👤 ${state.avatar.name}", style = MaterialTheme.typography.headlineSmall)
                Text("ID: ${state.avatar.id}")
                Text("Livello: ${state.avatar.level}")
                Text("XP: ${state.avatar.currentXp} / ${state.avatar.nextLevelXp}")
                Text("HP: ${state.avatar.hp} / ${state.avatar.maxHp}")
                Text("Mana: ${state.avatar.mana} / ${state.avatar.maxMana}")
                Text("Money: ${state.avatar.money}")
                Spacer(Modifier.height(8.dp))
                Text("Statistiche", style = MaterialTheme.typography.titleMedium)
                Text("Strength: ${state.avatar.strength}")
                Text("Defense: ${state.avatar.defense}")
                Text("Intelligence: ${state.avatar.intelligence}")
            }
        }
    }
}

