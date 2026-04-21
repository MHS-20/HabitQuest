package compose.project.demo.contexts.avatar.domain.model

sealed interface AvatarResult {
    data class Success(
        val avatar: AvatarData,
    ) : AvatarResult

    data class Error(
        val message: String,
    ) : AvatarResult
}

sealed interface AvatarInventoryResult {
    data class Success(
        val items: List<AvatarInventoryItem>,
    ) : AvatarInventoryResult

    data class Error(
        val message: String,
    ) : AvatarInventoryResult
}

sealed interface AvatarEquippedItemsResult {
    data class Success(
        val items: List<AvatarInventoryItem>,
    ) : AvatarEquippedItemsResult

    data class Error(
        val message: String,
    ) : AvatarEquippedItemsResult
}

sealed interface AvatarInventoryActionResult {
    data object Success : AvatarInventoryActionResult

    data class Error(
        val message: String,
    ) : AvatarInventoryActionResult
}

sealed interface AvatarStatIncrementResult {
    data object Success : AvatarStatIncrementResult

    data class Error(
        val message: String,
    ) : AvatarStatIncrementResult
}

enum class AvatarStatType {
    STRENGTH,
    DEFENSE,
    INTELLIGENCE,
}

data class AvatarData(
    val id: String,
    val name: String,
    val money: Int,
    val level: Int,
    val currentXp: Int,
    val nextLevelXp: Int,
    val hp: Int,
    val maxHp: Int,
    val mana: Int,
    val maxMana: Int,
    val strength: Int,
    val defense: Int,
    val intelligence: Int,
    val inventoryItems: List<AvatarInventoryItem>? = null,
    val equippedItems: List<AvatarInventoryItem>? = null,
)

data class AvatarInventoryItem(
    val name: String,
    val description: String,
    val type: String,
    val power: Int?,
    val price: Int,
    val quantity: Int,
)
