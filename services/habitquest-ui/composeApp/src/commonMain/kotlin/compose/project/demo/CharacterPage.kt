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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.coroutines.launch

@Composable
fun CharacterScreen(
    token: String,
    avatarState: AvatarUiState,
    onMoneyDelta: (Int) -> Unit = {},
    onAvatarRefresh: () -> Unit = {},
) {
    val avatarRepository = remember { AvatarRepository() }
    val marketplaceRepository = remember { MarketplaceRepository() }
    val scope = rememberCoroutineScope()
    var inventoryLoading by remember { mutableStateOf(false) }
    var inventoryError by remember { mutableStateOf<String?>(null) }
    var inventoryItems by remember { mutableStateOf<List<AvatarInventoryItem>>(emptyList()) }
    var marketplaceId by remember { mutableStateOf<String?>(null) }
    var sellingItemName by remember { mutableStateOf<String?>(null) }
    var sellActionMessage by remember { mutableStateOf<String?>(null) }

    suspend fun loadInventory(avatarId: String) {
        inventoryLoading = true
        inventoryError = null
        inventoryItems = emptyList()
        when (val result = avatarRepository.fetchInventory(avatarId = avatarId, token = token)) {
            is AvatarInventoryResult.Success -> inventoryItems = result.items
            is AvatarInventoryResult.Error -> inventoryError = result.message
        }
        inventoryLoading = false
    }

    suspend fun ensureMarketplaceId(avatarId: String): String? {
        val current = marketplaceId
        if (current != null) return current
        return when (val result = marketplaceRepository.ensureMarketplace(token, avatarId)) {
            is MarketplaceLoadResult.Success -> {
                marketplaceId = result.marketplaceId
                result.marketplaceId
            }

            is MarketplaceLoadResult.Error -> {
                inventoryError = result.message
                null
            }
        }
    }

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
                    marketplaceId = null
                    loadInventory(state.avatar.id)
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

                    sellActionMessage?.let { message ->
                        Text(
                            text = message,
                            color = if (message.startsWith("Vendita completata")) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.error
                            }
                        )
                    }

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
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = item.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(item.description, style = MaterialTheme.typography.bodyMedium)
                                    Spacer(Modifier.height(4.dp))
                                    Text("Tipo: ${item.type}", style = MaterialTheme.typography.bodySmall)
                                    Text("Potenza: ${item.power ?: "-"}", style = MaterialTheme.typography.bodySmall)
                                    Text("Quantità: ${item.quantity}", style = MaterialTheme.typography.bodySmall)
                                    Text("Prezzo: ${item.price}", style = MaterialTheme.typography.bodySmall)
                                    Spacer(Modifier.height(10.dp))
                                    Button(
                                        onClick = {
                                            scope.launch {
                                                sellingItemName = item.name
                                                sellActionMessage = null
                                                val marketplace = ensureMarketplaceId(state.avatar.id)
                                                if (marketplace == null) {
                                                    sellingItemName = null
                                                    return@launch
                                                }
                                                when (val result = marketplaceRepository.sellItem(token, marketplace, item.name)) {
                                                    MarketplaceSellResult.Success -> {
                                                        sellActionMessage = "Vendita completata: ${item.name}"
                                                        onMoneyDelta(item.price)
                                                        onAvatarRefresh()
                                                        loadInventory(state.avatar.id)
                                                    }

                                                    is MarketplaceSellResult.Error -> {
                                                        sellActionMessage = result.message
                                                    }
                                                }
                                                sellingItemName = null
                                            }
                                        },
                                        enabled = sellingItemName == null || sellingItemName == item.name,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        if (sellingItemName == item.name) {
                                            CircularProgressIndicator(strokeWidth = 2.dp)
                                            Spacer(Modifier.width(8.dp))
                                            Text("Vendo...")
                                        } else {
                                            Text("Vendi")
                                        }
                                    }
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

