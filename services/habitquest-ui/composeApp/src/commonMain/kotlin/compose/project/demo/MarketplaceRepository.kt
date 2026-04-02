package compose.project.demo

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
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.JsonElement

data class MarketplaceItem(
    val name: String,
    val description: String,
    val type: String,
    val power: Int?,
    val price: Int,
)

sealed interface MarketplaceLoadResult {
    data class Success(val marketplaceId: String, val items: List<MarketplaceItem>) : MarketplaceLoadResult
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

class MarketplaceRepository {
    private val client = HttpClient(createHttpEngine()) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    suspend fun ensureMarketplace(token: String, avatarId: String): MarketplaceLoadResult {
        val response = runCatching {
            client.post("${edgeServiceBaseUrl()}/api/v1/marketplaces") {
                header(HttpHeaders.Authorization, "Bearer $token")
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                setBody(
                    buildJsonObject {
                        put("avatarId", JsonPrimitive(avatarId))
                    }
                )
            }
        }.getOrElse {
            return MarketplaceLoadResult.Error("Impossibile contattare marketplace-service")
        }

        return when (response.status) {
            HttpStatusCode.Created, HttpStatusCode.OK -> {
                val body = response.body<JsonObject>()
                val source = body["content"]?.jsonObject ?: body
                val marketplaceId = source["id"]?.jsonPrimitive?.contentOrNull
                    ?: return MarketplaceLoadResult.Error("Risposta marketplace senza id")
                val items = parseItemsFromMarketplacePayload(source)
                MarketplaceLoadResult.Success(marketplaceId, items)
            }

            HttpStatusCode.Unauthorized -> MarketplaceLoadResult.Error("Sessione scaduta, rifai il login")
            HttpStatusCode.NotFound -> MarketplaceLoadResult.Error("Avatar non trovato")
            else -> MarketplaceLoadResult.Error("Errore marketplace (${response.status.value})")
        }
    }

    suspend fun fetchAvailableItems(token: String, marketplaceId: String): MarketplaceItemsResult {
        val response = runCatching {
            client.get("${edgeServiceBaseUrl()}/api/v1/marketplaces/$marketplaceId/items") {
                header(HttpHeaders.Authorization, "Bearer $token")
                header(HttpHeaders.Accept, ContentType.Application.Json)
            }
        }.getOrElse {
            return MarketplaceItemsResult.Error("Impossibile aggiornare il marketplace")
        }

        return when (response.status) {
            HttpStatusCode.OK -> {
                val body = response.body<JsonObject>()
                MarketplaceItemsResult.Success(parseItemsFromMarketplacePayload(body))
            }

            HttpStatusCode.Unauthorized -> MarketplaceItemsResult.Error("Sessione scaduta, rifai il login")
            HttpStatusCode.NotFound -> MarketplaceItemsResult.Error("Marketplace non trovato")
            else -> MarketplaceItemsResult.Error("Errore lettura marketplace (${response.status.value})")
        }
    }

    suspend fun buyItem(
        token: String,
        marketplaceId: String,
        itemName: String,
        currentLevel: Int,
    ): MarketplaceBuyResult {
        val encodedItemName = itemName.encodeURLPath()
        val response = runCatching {
            client.post("${edgeServiceBaseUrl()}/api/v1/marketplaces/$marketplaceId/items/$encodedItemName/buy") {
                header(HttpHeaders.Authorization, "Bearer $token")
                parameter("currentLevel", currentLevel)
            }
        }.getOrElse {
            return MarketplaceBuyResult.Error("Impossibile contattare marketplace-service")
        }

        return when (response.status) {
            HttpStatusCode.NoContent, HttpStatusCode.OK -> MarketplaceBuyResult.Success
            HttpStatusCode.Forbidden -> MarketplaceBuyResult.Error("Livello insufficiente per acquistare questo oggetto")
            HttpStatusCode.NotFound -> MarketplaceBuyResult.Error("Oggetto non disponibile")
            HttpStatusCode.Unauthorized -> MarketplaceBuyResult.Error("Sessione scaduta, rifai il login")
            else -> MarketplaceBuyResult.Error("Acquisto fallito (${response.status.value})")
        }
    }

    private fun parseItemsFromMarketplacePayload(payload: JsonObject): List<MarketplaceItem> {
        val directItems = payload["items"]?.jsonArray.orEmpty().mapNotNull { asMarketplaceItem(it) }
        if (directItems.isNotEmpty()) {
            return directItems
        }

        val embeddedItems = payload["_embedded"]
            ?.jsonObject
            ?.values
            ?.flatMap { value -> value.jsonArray.mapNotNull { asMarketplaceItem(it) } }
            .orEmpty()

        return embeddedItems
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
        )
    }
}

