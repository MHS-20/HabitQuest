package compose.project.demo.contexts.marketplace.infrastructure.mapper

import compose.project.demo.contexts.marketplace.domain.model.MarketplaceItem
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

internal fun parseItemsFromMarketplacePayload(payload: JsonObject): List<MarketplaceItem> {
    val directItems = payload["items"]?.jsonArray.orEmpty().mapNotNull(::asMarketplaceItem)
    if (directItems.isNotEmpty()) {
        return directItems
    }

    return payload["_embedded"]
        ?.jsonObject
        ?.values
        ?.flatMap { value -> value.jsonArray.mapNotNull(::asMarketplaceItem) }
        .orEmpty()
}


private fun asMarketplaceItem(element: JsonElement): MarketplaceItem? {
    val raw = element as? JsonObject ?: return null
    val source = raw["content"]?.jsonObject ?: raw
    val name = source["name"]?.jsonPrimitive?.contentOrNull ?: return null
    return MarketplaceItem(
        name = name,
        description = source["description"]?.jsonPrimitive?.contentOrNull.orEmpty(),
        type = source["type"]?.jsonPrimitive?.contentOrNull ?: "UNKNOWN",
        power = source["power"]?.jsonPrimitive?.intOrNull,
        price = source["price"]?.jsonPrimitive?.intOrNull ?: 0,
        requiredLevel = (source["requiredLevel"]?.jsonPrimitive?.intOrNull ?: 1).coerceAtLeast(1),
    )
}
