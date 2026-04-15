package compose.project.demo.contexts.avatar.domain.contract

import compose.project.demo.contexts.avatar.domain.model.AvatarEquippedItemsResult
import compose.project.demo.contexts.avatar.domain.model.AvatarInventoryActionResult
import compose.project.demo.contexts.avatar.domain.model.AvatarInventoryItem
import compose.project.demo.contexts.avatar.domain.model.AvatarInventoryResult
import compose.project.demo.contexts.avatar.domain.model.AvatarResult
import compose.project.demo.contexts.avatar.domain.model.AvatarStatIncrementResult

interface AvatarGateway {
    suspend fun fetchAvatar(
        avatarId: String,
        token: String,
    ): AvatarResult

    suspend fun fetchInventory(
        avatarId: String,
        token: String,
    ): AvatarInventoryResult

    suspend fun fetchEquippedItems(
        avatarId: String,
        token: String,
    ): AvatarEquippedItemsResult

    suspend fun equipItem(
        token: String,
        avatarId: String,
        item: AvatarInventoryItem,
    ): AvatarInventoryActionResult

    suspend fun unequipItem(
        token: String,
        avatarId: String,
        item: AvatarInventoryItem,
    ): AvatarInventoryActionResult

    suspend fun usePotion(
        token: String,
        avatarId: String,
        item: AvatarInventoryItem,
    ): AvatarInventoryActionResult

    suspend fun increaseStrength(
        token: String,
        avatarId: String,
    ): AvatarStatIncrementResult

    suspend fun increaseDefense(
        token: String,
        avatarId: String,
    ): AvatarStatIncrementResult

    suspend fun increaseIntelligence(
        token: String,
        avatarId: String,
    ): AvatarStatIncrementResult
}
