package compose.project.demo.contexts.marketplace.domain.model

data class MarketplaceItem(
  val name: String,
  val description: String,
  val type: String,
  val power: Int?,
  val price: Int,
)

sealed interface MarketplaceLoadResult {
  data class Success(val marketplaceId: String, val items: List<MarketplaceItem>) :
    MarketplaceLoadResult

  data class Error(val message: String) : MarketplaceLoadResult
}

sealed interface MarketplaceItemsResult {
  data class Success(val items: List<MarketplaceItem>) : MarketplaceItemsResult

  data class Error(val message: String) : MarketplaceItemsResult
}

sealed interface MarketplaceBuyResult {
  data object Success : MarketplaceBuyResult

  data class Error(val message: String) : MarketplaceBuyResult
}

sealed interface MarketplaceSellResult {
  data object Success : MarketplaceSellResult

  data class Error(val message: String) : MarketplaceSellResult
}
