package compose.project.demo.contexts.avatar.infrastructure.mapper

import compose.project.demo.contexts.avatar.domain.model.AvatarData
import compose.project.demo.contexts.avatar.domain.model.AvatarInventoryItem
import compose.project.demo.contexts.avatar.domain.model.AvatarResult
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

internal fun mapAvatarResponse(body: JsonObject): AvatarResult {
    val source = body["content"]?.jsonObject ?: body
    val level = source["level"]?.jsonObject ?: buildJsonObject {}
    val health = source["health"]?.jsonObject ?: buildJsonObject {}
    val mana = source["mana"]?.jsonObject ?: buildJsonObject {}
    val stats = source["stats"]?.jsonObject ?: buildJsonObject {}
    val inventoryItems = source.inventoryItemsOrNull("inventory")
    val equippedItems = source.inventoryItemsOrNull("equippedItems")

    val id = source.stringValue("id") ?: return AvatarResult.Error("Avatar response missing id")
    val name = source.stringValue("name") ?: return AvatarResult.Error("Avatar response missing name")

    val data =
        AvatarData(
            id = id,
            name = name,
            money = source["money"]?.jsonObject?.intValue("amount") ?: 0,
            level = level.intValue("levelNumber"),
            currentXp = level.intValue("currentExperience"),
            nextLevelXp = level.intValue("experienceRequired").coerceAtLeast(1),
            hp = health.intValue("current"),
            maxHp = health.intValue("max").coerceAtLeast(1),
            mana = mana.intValue("amount"),
            maxMana = mana.intValue("max").coerceAtLeast(1),
            strength = stats.intValue("strength"),
            defense = stats.intValue("defense"),
            intelligence = stats.intValue("intelligence"),
            inventoryItems = inventoryItems,
            equippedItems = equippedItems,
        )
    return AvatarResult.Success(data)
}

internal fun parseInventoryFromPayload(payload: JsonElement): List<AvatarInventoryItem> {
    val directItems =
        when (payload) {
            is JsonArray -> payload.mapNotNull(::asInventoryItem)
            is JsonObject -> payload["items"]?.jsonArray.orEmpty().mapNotNull(::asInventoryItem)
            else -> emptyList()
        }
    if (directItems.isNotEmpty()) {
        return directItems
    }

    val payloadObject = payload as? JsonObject ?: return emptyList()
    val content = payloadObject["content"]

    if (content is JsonArray) {
        return content.mapNotNull(::asInventoryItem)
    }

    if (content is JsonObject) {
        val contentItems = content["items"]?.jsonArray.orEmpty().mapNotNull(::asInventoryItem)
        if (contentItems.isNotEmpty()) {
            return contentItems
        }
    }

    return payloadObject["_embedded"]
        ?.jsonObject
        ?.values
        ?.flatMap { value ->
            val array = value as? JsonArray ?: return@flatMap emptyList()
            array.mapNotNull(::asInventoryItem)
        }.orEmpty()
}

internal fun buildInventoryActionPayload(item: AvatarInventoryItem): JsonObject =
    buildJsonObject {
        put("type", JsonPrimitive(item.type))
        put("name", JsonPrimitive(item.name))
        put("description", JsonPrimitive(item.description))
        put("power", JsonPrimitive(item.power ?: 0))
    }

internal fun buildPotionActionPayload(item: AvatarInventoryItem): JsonObject =
    buildJsonObject {
        put("name", JsonPrimitive(item.name))
        put("description", JsonPrimitive(item.description))
        put("power", JsonPrimitive(item.power ?: 0))
    }

private fun asInventoryItem(element: JsonElement): AvatarInventoryItem? {
    val raw = element as? JsonObject ?: return null
    val source = raw["content"]?.jsonObject ?: raw
    val name =
        source["name"]?.jsonPrimitive?.contentOrNull
            ?: source["itemName"]?.jsonPrimitive?.contentOrNull
            ?: return null

    return AvatarInventoryItem(
        name = name,
        description = source["description"]?.jsonPrimitive?.contentOrNull.orEmpty(),
        type =
            source["type"]?.jsonPrimitive?.contentOrNull
                ?: source["category"]?.jsonPrimitive?.contentOrNull
                ?: "UNKNOWN",
        power = source["power"]?.jsonPrimitive?.intOrNull,
        price = source["price"]?.jsonPrimitive?.intOrNull ?: 0,
        quantity =
            source["quantity"]?.jsonPrimitive?.intOrNull
                ?: source["amount"]?.jsonPrimitive?.intOrNull
                ?: 1,
        requiredLevel =
            source["requiredLevel"]?.jsonPrimitive?.intOrNull
                ?: source["levelRequired"]?.jsonPrimitive?.intOrNull
                ?: source["minLevel"]?.jsonPrimitive?.intOrNull
                ?: 1,
    )
}

private fun JsonObject.stringValue(key: String): String? = (this[key] as? JsonPrimitive)?.contentOrNull

private fun JsonObject.intValue(key: String): Int = (this[key] as? JsonPrimitive)?.intOrNull ?: 0

private fun JsonObject.inventoryItemsOrNull(key: String): List<AvatarInventoryItem>? {
    val element = this[key] ?: return null
    return parseInventoryFromPayload(element)
}
