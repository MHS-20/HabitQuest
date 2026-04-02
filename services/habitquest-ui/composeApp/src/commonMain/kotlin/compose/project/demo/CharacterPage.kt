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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CharacterScreen(token: String, avatarState: AvatarUiState) {
    val avatarRepository = remember { AvatarRepository() }
    var inventoryLoading by remember { mutableStateOf(false) }
    var inventoryError by remember { mutableStateOf<String?>(null) }
    var inventoryItems by remember { mutableStateOf<List<AvatarInventoryItem>>(emptyList()) }

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

            is AvatarUiState.Ready -> {
                LaunchedEffect(state.avatar.id, token) {
                    inventoryLoading = true
                    inventoryError = null
                    inventoryItems = emptyList()
                    when (val result = avatarRepository.fetchInventory(avatarId = state.avatar.id, token = token)) {
                        is AvatarInventoryResult.Success -> inventoryItems = result.items
                        is AvatarInventoryResult.Error -> inventoryError = result.message
                    }
                    inventoryLoading = false
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("👤 ${state.avatar.name}", style = MaterialTheme.typography.headlineSmall)
                    Text("ID: ${state.avatar.id}")
                    Text("Money: ${state.avatar.money}")

                    Spacer(Modifier.height(12.dp))
                    Text("Inventario", style = MaterialTheme.typography.titleMedium)

                    when {
                        inventoryLoading -> Text("Caricamento inventario...")
                        inventoryError != null -> Text(
                            text = inventoryError.orEmpty(),
                            color = MaterialTheme.colorScheme.error
                        )

                        inventoryItems.isEmpty() -> Text("Inventario vuoto")
                        else -> inventoryItems.forEach { item ->
                            Text("- ${item.name} x${item.quantity} (${item.type})")
                        }
                    }
                }
            }
        }
    }
}

