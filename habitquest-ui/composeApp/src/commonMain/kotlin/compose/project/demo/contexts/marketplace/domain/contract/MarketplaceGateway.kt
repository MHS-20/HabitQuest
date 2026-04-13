package compose.project.demo.contexts.marketplace.domain.contract

import compose.project.demo.contexts.marketplace.domain.model.MarketplaceBuyResult
import compose.project.demo.contexts.marketplace.domain.model.MarketplaceItemsResult
import compose.project.demo.contexts.marketplace.domain.model.MarketplaceLoadResult
import compose.project.demo.contexts.marketplace.domain.model.MarketplaceSellResult

interface MarketplaceGateway {
  suspend fun ensureMarketplace(token: String, avatarId: String): MarketplaceLoadResult

  suspend fun fetchAvailableItems(token: String, marketplaceId: String): MarketplaceItemsResult

  suspend fun buyItem(
    token: String,
    marketplaceId: String,
    itemName: String,
    currentLevel: Int,
  ): MarketplaceBuyResult

  suspend fun sellItem(
    token: String,
    marketplaceId: String,
    itemName: String,
  ): MarketplaceSellResult
}
