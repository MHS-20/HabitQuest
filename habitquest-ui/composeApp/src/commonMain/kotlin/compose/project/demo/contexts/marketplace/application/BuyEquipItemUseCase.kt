package compose.project.demo.contexts.marketplace.application

import compose.project.demo.contexts.avatar.domain.contract.AvatarGateway
import compose.project.demo.contexts.avatar.domain.model.AvatarInventoryActionResult
import compose.project.demo.contexts.avatar.domain.model.AvatarInventoryItem
import compose.project.demo.contexts.marketplace.domain.contract.MarketplaceGateway
import compose.project.demo.contexts.marketplace.domain.model.MarketplaceBuyResult

class BuyEquipItemUseCase(
    private val marketplaceGateway: MarketplaceGateway,
    private val avatarGateway: AvatarGateway,
) {
    suspend fun buy(
        token: String,
        marketplaceId: String,
        itemName: String,
        currentLevel: Int,
    ): MarketplaceBuyResult = marketplaceGateway.buyItem(token, marketplaceId, itemName, currentLevel)

    suspend fun toggleEquip(
        token: String,
        avatarId: String,
        item: AvatarInventoryItem,
        isEquipped: Boolean,
    ): AvatarInventoryActionResult =
        if (isEquipped) {
            avatarGateway.unequipItem(token, avatarId, item)
        } else {
            avatarGateway.equipItem(token, avatarId, item)
        }
}
