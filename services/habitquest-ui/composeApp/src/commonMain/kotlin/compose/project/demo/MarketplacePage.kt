package compose.project.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun MarketplaceScreen(token: String, avatarState: AvatarUiState) {
    val repository = remember { MarketplaceRepository() }
    val scope = rememberCoroutineScope()
    var uiState by remember { mutableStateOf<MarketplaceUiState>(MarketplaceUiState.Loading) }
    var marketplaceId by remember { mutableStateOf<String?>(null) }
    var actionMessage by remember { mutableStateOf<String?>(null) }
    var buyingItemName by remember { mutableStateOf<String?>(null) }

    suspend fun loadMarketplace(showLoading: Boolean) {
        val avatar = (avatarState as? AvatarUiState.Ready)?.avatar
        if (showLoading) {
            uiState = MarketplaceUiState.Loading
        }

        uiState = when {
            token.isBlank() -> MarketplaceUiState.Error("Sessione non valida")
            avatar == null -> MarketplaceUiState.Error("Avatar non disponibile")
            marketplaceId != null -> {
                when (val refreshed = repository.fetchAvailableItems(token, marketplaceId.orEmpty())) {
                    is MarketplaceItemsResult.Success -> MarketplaceUiState.Ready(
                        marketplaceId = marketplaceId.orEmpty(),
                        items = refreshed.items
                    )

                    is MarketplaceItemsResult.Error -> MarketplaceUiState.Error(refreshed.message)
                }
            }

            else -> when (val created = repository.ensureMarketplace(token, avatar.id)) {
                is MarketplaceLoadResult.Error -> MarketplaceUiState.Error(created.message)
                is MarketplaceLoadResult.Success -> {
                    marketplaceId = created.marketplaceId
                    when (val refreshed = repository.fetchAvailableItems(token, created.marketplaceId)) {
                        is MarketplaceItemsResult.Success -> MarketplaceUiState.Ready(
                            marketplaceId = created.marketplaceId,
                            items = refreshed.items
                        )

                        is MarketplaceItemsResult.Error -> MarketplaceUiState.Ready(
                            marketplaceId = created.marketplaceId,
                            items = created.items,
                            warning = refreshed.message
                        )
                    }
                }
            }
        }
    }

    LaunchedEffect(token, avatarState) {
        marketplaceId = null
        loadMarketplace(showLoading = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Marketplace", style = MaterialTheme.typography.headlineSmall)

        actionMessage?.let { message ->
            Text(
                text = message,
                color = if (message.startsWith("Acquisto completato")) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
            )
        }

        when (val state = uiState) {
            MarketplaceUiState.Loading -> Text("Caricamento marketplace...")
            is MarketplaceUiState.Error -> Text(state.message, color = MaterialTheme.colorScheme.error)
            is MarketplaceUiState.Ready -> {
                if (state.warning != null) {
                    Text(state.warning, color = MaterialTheme.colorScheme.error)
                }

                if (state.items.isEmpty()) {
                    Text("Nessun oggetto disponibile")
                } else {
                    val avatarLevel = (avatarState as? AvatarUiState.Ready)?.avatar?.level ?: 0
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(state.items, key = { it.name }) { item ->
                            MarketplaceItemRow(
                                item = item,
                                isBuying = buyingItemName == item.name,
                                onBuy = {
                                    scope.launch {
                                        buyingItemName = item.name
                                        actionMessage = null
                                        when (
                                            val result = repository.buyItem(
                                                token = token,
                                                marketplaceId = state.marketplaceId,
                                                itemName = item.name,
                                                currentLevel = avatarLevel
                                            )
                                        ) {
                                            MarketplaceBuyResult.Success -> {
                                                actionMessage = "Acquisto completato: ${item.name}"
                                                loadMarketplace(showLoading = false)
                                            }

                                            is MarketplaceBuyResult.Error -> {
                                                actionMessage = result.message
                                            }
                                        }
                                        buyingItemName = null
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MarketplaceItemRow(
    item: MarketplaceItem,
    isBuying: Boolean,
    onBuy: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(item.name, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(item.description, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(6.dp))
            Text("Tipo: ${item.type}", style = MaterialTheme.typography.bodySmall)
            Text("Potenza: ${item.power ?: "-"}", style = MaterialTheme.typography.bodySmall)
            Text("Prezzo: ${item.price}", style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = onBuy,
                enabled = !isBuying,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isBuying) {
                    CircularProgressIndicator(strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("Acquisto...")
                } else {
                    Text("Compra")
                }
            }
        }
    }
}

private sealed interface MarketplaceUiState {
    data object Loading : MarketplaceUiState
    data class Ready(
        val marketplaceId: String,
        val items: List<MarketplaceItem>,
        val warning: String? = null,
    ) : MarketplaceUiState

    data class Error(val message: String) : MarketplaceUiState
}
