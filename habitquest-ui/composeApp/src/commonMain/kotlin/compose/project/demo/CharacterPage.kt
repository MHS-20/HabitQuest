package compose.project.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
  var equippedItems by remember { mutableStateOf<List<AvatarInventoryItem>>(emptyList()) }
  var equippedItemNames by remember { mutableStateOf<Set<String>>(emptySet()) }
  var marketplaceId by remember { mutableStateOf<String?>(null) }
  var sellingItemName by remember { mutableStateOf<String?>(null) }
  var inventoryActionItemName by remember { mutableStateOf<String?>(null) }
  var sellActionMessage by remember { mutableStateOf<String?>(null) }
  var inventoryActionMessage by remember { mutableStateOf<String?>(null) }

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

  suspend fun loadEquippedItems(avatarId: String) {
    when (val result = avatarRepository.fetchEquippedItems(avatarId = avatarId, token = token)) {
      is AvatarEquippedItemsResult.Success -> {
        equippedItems = result.items
        equippedItemNames = result.items.mapTo(mutableSetOf()) { it.name }
      }
      is AvatarEquippedItemsResult.Error -> inventoryError = result.message
    }
  }

  suspend fun reloadInventoryState(avatarId: String) {
    loadInventory(avatarId)
    loadEquippedItems(avatarId)
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

  Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
    when (val state = avatarState) {
      AvatarUiState.Loading ->
        Text(
          text = "Loading character...",
          style = MaterialTheme.typography.bodyLarge,
          modifier = Modifier.align(Alignment.Center),
        )

      is AvatarUiState.Error ->
        Text(
          text = state.message,
          color = MaterialTheme.colorScheme.error,
          style = MaterialTheme.typography.bodyLarge,
          modifier = Modifier.align(Alignment.Center),
        )

      is AvatarUiState.Ready -> {
        LaunchedEffect(state.avatar.id, token) {
          marketplaceId = null
          reloadInventoryState(state.avatar.id)
        }

        Column(
          modifier = Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Text("👤 ${state.avatar.name}", style = MaterialTheme.typography.headlineSmall)
          Text("ID: ${state.avatar.id}")
          Text("Money: ${state.avatar.money}")

          sellActionMessage?.let { message ->
            Text(
              text = message,
              color =
                if (message.startsWith("Sale completed")) {
                  MaterialTheme.colorScheme.primary
                } else {
                  MaterialTheme.colorScheme.error
                },
            )
          }

          inventoryActionMessage?.let { message ->
            Text(
              text = message,
              color =
                if (
                  message.startsWith("Equip") ||
                    message.startsWith("Unequip") ||
                    message.startsWith("Use potion")
                ) {
                  MaterialTheme.colorScheme.primary
                } else {
                  MaterialTheme.colorScheme.error
                },
            )
          }

          Spacer(Modifier.height(12.dp))
          Text("Inventory", style = MaterialTheme.typography.titleMedium)

          when {
            inventoryLoading -> Text("Loading inventory...")
            inventoryError != null ->
              Text(text = inventoryError.orEmpty(), color = MaterialTheme.colorScheme.error)

            inventoryItems.isEmpty() -> Text("Inventory is empty")
            else ->
              inventoryItems.forEach { item ->
                val isEquippable =
                  item.type.equals("WEAPON", true) || item.type.equals("ARMOR", true)
                val isPotion =
                  item.type.equals("HEALTH_POTION", true) || item.type.equals("MANA_POTION", true)
                val isEquipped = equippedItemNames.contains(item.name)
                Card(
                  modifier = Modifier.fillMaxWidth(),
                  colors =
                    CardDefaults.cardColors(
                      containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                ) {
                  Column(modifier = Modifier.padding(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                      Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                      )
                      if (isEquipped) {
                        Surface(
                          color = MaterialTheme.colorScheme.primaryContainer,
                          contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                          shape = MaterialTheme.shapes.small,
                        ) {
                          Text(
                            text = "Equipped",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                          )
                        }
                      }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(item.description, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(4.dp))
                    Text("Type: ${item.type}", style = MaterialTheme.typography.bodySmall)
                    Text("Power: ${item.power ?: "-"}", style = MaterialTheme.typography.bodySmall)
                    Text("Quantity: ${item.quantity}", style = MaterialTheme.typography.bodySmall)
                    Text("Price: ${item.price}", style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(10.dp))
                    if (isEquippable) {
                      Button(
                        onClick = {
                          scope.launch {
                            inventoryActionItemName = item.name
                            inventoryActionMessage = null
                            val result =
                              if (isEquipped) {
                                avatarRepository.unequipItem(token, state.avatar.id, item)
                              } else {
                                avatarRepository.equipItem(token, state.avatar.id, item)
                              }
                            when (result) {
                              AvatarInventoryActionResult.Success -> {
                                inventoryActionMessage =
                                  if (isEquipped) {
                                    "Unequip completed: ${item.name}"
                                  } else {
                                    "Equip completed: ${item.name}"
                                  }
                                onAvatarRefresh()
                                reloadInventoryState(state.avatar.id)
                              }

                              is AvatarInventoryActionResult.Error -> {
                                inventoryActionMessage = result.message
                              }
                            }
                            inventoryActionItemName = null
                          }
                        },
                        enabled =
                          inventoryActionItemName == null || inventoryActionItemName == item.name,
                        modifier = Modifier.fillMaxWidth(),
                      ) {
                        if (inventoryActionItemName == item.name) {
                          CircularProgressIndicator(strokeWidth = 2.dp)
                          Spacer(Modifier.width(8.dp))
                          Text(if (isEquipped) "Unequip..." else "Equip...")
                        } else {
                          Text(if (isEquipped) "Unequip" else "Equip")
                        }
                      }
                      Spacer(Modifier.height(8.dp))
                    }
                    if (isPotion) {
                      Button(
                        onClick = {
                          scope.launch {
                            inventoryActionItemName = item.name
                            inventoryActionMessage = null
                            when (
                              val result = avatarRepository.usePotion(token, state.avatar.id, item)
                            ) {
                              AvatarInventoryActionResult.Success -> {
                                inventoryActionMessage = "Use potion completed: ${item.name}"
                                onAvatarRefresh()
                                reloadInventoryState(state.avatar.id)
                              }

                              is AvatarInventoryActionResult.Error -> {
                                inventoryActionMessage = result.message
                              }
                            }
                            inventoryActionItemName = null
                          }
                        },
                        enabled =
                          inventoryActionItemName == null || inventoryActionItemName == item.name,
                        modifier = Modifier.fillMaxWidth(),
                      ) {
                        if (inventoryActionItemName == item.name) {
                          CircularProgressIndicator(strokeWidth = 2.dp)
                          Spacer(Modifier.width(8.dp))
                          Text("Using potion...")
                        } else {
                          Text("Use potion")
                        }
                      }
                      Spacer(Modifier.height(8.dp))
                    }
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
                          when (
                            val result =
                              marketplaceRepository.sellItem(token, marketplace, item.name)
                          ) {
                            MarketplaceSellResult.Success -> {
                              sellActionMessage = "Sale completed: ${item.name}"
                              onMoneyDelta(item.price)
                              onAvatarRefresh()
                              reloadInventoryState(state.avatar.id)
                            }

                            is MarketplaceSellResult.Error -> {
                              sellActionMessage = result.message
                            }
                          }
                          sellingItemName = null
                        }
                      },
                      enabled = sellingItemName == null || sellingItemName == item.name,
                      modifier = Modifier.fillMaxWidth(),
                    ) {
                      if (sellingItemName == item.name) {
                        CircularProgressIndicator(strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("Selling...")
                      } else {
                        Text("Sell")
                      }
                    }
                  }
                }
                Spacer(Modifier.height(8.dp))
              }
          }

          Spacer(Modifier.height(12.dp))
          Text("Equipped", style = MaterialTheme.typography.titleMedium)
          if (equippedItems.isEmpty()) {
            Text("No equipped items")
          } else {
            equippedItems.forEach { item ->
              Card(
                modifier = Modifier.fillMaxWidth(),
                colors =
                  CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
              ) {
                Column(modifier = Modifier.padding(12.dp)) {
                  Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                      text = item.name,
                      style = MaterialTheme.typography.titleMedium,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis,
                      modifier = Modifier.weight(1f),
                    )
                    Surface(
                      color = MaterialTheme.colorScheme.primaryContainer,
                      contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                      shape = MaterialTheme.shapes.small,
                    ) {
                      Text(
                        text = "Equipped",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                      )
                    }
                  }
                  Spacer(Modifier.height(4.dp))
                  Text(item.description, style = MaterialTheme.typography.bodyMedium)
                  Spacer(Modifier.height(4.dp))
                  Text("Type: ${item.type}", style = MaterialTheme.typography.bodySmall)
                  Text("Power: ${item.power ?: "-"}", style = MaterialTheme.typography.bodySmall)
                  Spacer(Modifier.height(10.dp))
                  Button(
                    onClick = {
                      scope.launch {
                        inventoryActionItemName = item.name
                        inventoryActionMessage = null
                        when (
                          val result = avatarRepository.unequipItem(token, state.avatar.id, item)
                        ) {
                          AvatarInventoryActionResult.Success -> {
                            inventoryActionMessage = "Unequip completed: ${item.name}"
                            onAvatarRefresh()
                            reloadInventoryState(state.avatar.id)
                          }

                          is AvatarInventoryActionResult.Error -> {
                            inventoryActionMessage = result.message
                          }
                        }
                        inventoryActionItemName = null
                      }
                    },
                    enabled =
                      inventoryActionItemName == null || inventoryActionItemName == item.name,
                    modifier = Modifier.fillMaxWidth(),
                  ) {
                    if (inventoryActionItemName == item.name) {
                      CircularProgressIndicator(strokeWidth = 2.dp)
                      Spacer(Modifier.width(8.dp))
                      Text("Unequip...")
                    } else {
                      Text("Unequip")
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
