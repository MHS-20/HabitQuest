package compose.project.demo.contexts.marketplace.infrastructure.repository

import compose.project.demo.contexts.marketplace.domain.contract.MarketplaceGateway
import compose.project.demo.contexts.marketplace.domain.model.MarketplaceBuyResult
import compose.project.demo.contexts.marketplace.domain.model.MarketplaceItemsResult
import compose.project.demo.contexts.marketplace.domain.model.MarketplaceLoadResult
import compose.project.demo.contexts.marketplace.domain.model.MarketplaceSellResult
import compose.project.demo.contexts.marketplace.infrastructure.mapper.parseItemsFromMarketplacePayload
import compose.project.demo.createHttpEngine
import compose.project.demo.edgeServiceBaseUrl
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.encodeURLPath
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class MarketplaceRepository : MarketplaceGateway {
    private val client =
        HttpClient(createHttpEngine()) {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }

    override suspend fun ensureMarketplace(
        token: String,
        avatarId: String,
    ): MarketplaceLoadResult {
        val response =
            runCatching {
                client.get("${edgeServiceBaseUrl()}/api/v1/marketplaces/by-avatar/$avatarId") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    header(HttpHeaders.Accept, ContentType.Application.Json)
                }
            }.getOrElse {
                return MarketplaceLoadResult.Error("Unable to contact marketplace-service")
            }

        return when (response.status) {
            HttpStatusCode.OK -> {
                val body = response.body<JsonObject>()
                val source = body["content"]?.jsonObject ?: body
                val marketplaceId =
                    source["id"]?.jsonPrimitive?.contentOrNull
                        ?: return MarketplaceLoadResult.Error("Marketplace response missing id")
                val items = parseItemsFromMarketplacePayload(source)
                MarketplaceLoadResult.Success(marketplaceId, items)
            }

            HttpStatusCode.NotFound -> {
                createMarketplace(token, avatarId)
            }

            HttpStatusCode.Unauthorized -> {
                MarketplaceLoadResult.Error("Session expired, please log in again")
            }

            else -> {
                MarketplaceLoadResult.Error("Marketplace error (${response.status.value})")
            }
        }
    }

    private suspend fun createMarketplace(
        token: String,
        avatarId: String,
    ): MarketplaceLoadResult {
        val response =
            runCatching {
                client.post("${edgeServiceBaseUrl()}/api/v1/marketplaces") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    header(HttpHeaders.ContentType, ContentType.Application.Json)
                    setBody(buildJsonObject { put("avatarId", JsonPrimitive(avatarId)) })
                }
            }.getOrElse {
                return MarketplaceLoadResult.Error("Unable to contact marketplace-service")
            }

        return when (response.status) {
            HttpStatusCode.Created,
            HttpStatusCode.OK,
            -> {
                val body = response.body<JsonObject>()
                val source = body["content"]?.jsonObject ?: body
                val marketplaceId =
                    source["id"]?.jsonPrimitive?.contentOrNull
                        ?: return MarketplaceLoadResult.Error("Marketplace response missing id")
                val items = parseItemsFromMarketplacePayload(source)
                MarketplaceLoadResult.Success(marketplaceId, items)
            }

            HttpStatusCode.Unauthorized -> {
                MarketplaceLoadResult.Error("Session expired, please log in again")
            }

            HttpStatusCode.NotFound -> {
                MarketplaceLoadResult.Error("Avatar not found")
            }

            else -> {
                MarketplaceLoadResult.Error("Marketplace error (${response.status.value})")
            }
        }
    }

    override suspend fun fetchAvailableItems(
        token: String,
        marketplaceId: String,
    ): MarketplaceItemsResult {
        val response =
            runCatching {
                client.get("${edgeServiceBaseUrl()}/api/v1/marketplaces/$marketplaceId/items") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    header(HttpHeaders.Accept, ContentType.Application.Json)
                }
            }.getOrElse {
                return MarketplaceItemsResult.Error("Unable to refresh marketplace")
            }

        return when (response.status) {
            HttpStatusCode.OK -> {
                val body = response.body<JsonObject>()
                MarketplaceItemsResult.Success(parseItemsFromMarketplacePayload(body))
            }

            HttpStatusCode.Unauthorized -> {
                MarketplaceItemsResult.Error("Session expired, please log in again")
            }

            HttpStatusCode.NotFound -> {
                MarketplaceItemsResult.Error("Marketplace not found")
            }

            else -> {
                MarketplaceItemsResult.Error("Marketplace read error (${response.status.value})")
            }
        }
    }

    override suspend fun buyItem(
        token: String,
        marketplaceId: String,
        itemName: String,
        currentLevel: Int,
    ): MarketplaceBuyResult {
        val encodedItemName = itemName.encodeURLPath()
        val response =
            runCatching {
                client.post(
                    "${edgeServiceBaseUrl()}/api/v1/marketplaces/$marketplaceId/items/$encodedItemName/buy",
                ) {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    parameter("currentLevel", currentLevel)
                }
            }.getOrElse {
                return MarketplaceBuyResult.Error("Unable to contact marketplace-service")
            }

        return when (response.status) {
            HttpStatusCode.NoContent,
            HttpStatusCode.OK,
            -> {
                MarketplaceBuyResult.Success
            }

            HttpStatusCode.Forbidden -> {
                MarketplaceBuyResult.Error("Level too low to buy this item")
            }

            HttpStatusCode.NotFound -> {
                MarketplaceBuyResult.Error("Item not available")
            }

            HttpStatusCode.Unauthorized -> {
                MarketplaceBuyResult.Error("Session expired, please log in again")
            }

            else -> {
                MarketplaceBuyResult.Error("Purchase failed (${response.status.value})")
            }
        }
    }

    override suspend fun sellItem(
        token: String,
        marketplaceId: String,
        itemName: String,
    ): MarketplaceSellResult {
        val encodedItemName = itemName.encodeURLPath()
        val response =
            runCatching {
                client.post(
                    "${edgeServiceBaseUrl()}/api/v1/marketplaces/$marketplaceId/sold-items/$encodedItemName/sell",
                ) {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
            }.getOrElse {
                return MarketplaceSellResult.Error("Unable to contact marketplace-service")
            }

        return when (response.status) {
            HttpStatusCode.NoContent,
            HttpStatusCode.OK,
            -> {
                MarketplaceSellResult.Success
            }

            HttpStatusCode.NotFound -> {
                MarketplaceSellResult.Error("Item not available")
            }

            HttpStatusCode.Unauthorized -> {
                MarketplaceSellResult.Error("Session expired, please log in again")
            }

            else -> {
                MarketplaceSellResult.Error("Sale failed (${response.status.value})")
            }
        }
    }
}
