package compose.project.demo

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
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

sealed interface AvatarResult {
    data class Success(val avatar: AvatarData) : AvatarResult
    data class Error(val message: String) : AvatarResult
}

sealed interface AvatarInventoryResult {
    data class Success(val items: List<AvatarInventoryItem>) : AvatarInventoryResult
    data class Error(val message: String) : AvatarInventoryResult
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
)

data class AvatarInventoryItem(
    val name: String,
    val description: String,
    val type: String,
    val power: Int?,
    val price: Int,
    val quantity: Int,
)

class AvatarRepository {
    private val client = HttpClient(createHttpEngine()) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    suspend fun fetchAvatar(avatarId: String, token: String): AvatarResult {
        if (avatarId.isBlank()) {
            return AvatarResult.Error("Utente non valido")
        }

        val response = runCatching {
            client.get("${edgeServiceBaseUrl()}/api/v1/avatars/$avatarId") {
                header(HttpHeaders.Authorization, "Bearer $token")
                header(HttpHeaders.Accept, ContentType.Application.Json)
            }
        }.getOrElse {
            return AvatarResult.Error("Impossibile contattare avatar-service")
        }

        return when (response.status) {
            HttpStatusCode.OK -> mapAvatarResponse(response.body<JsonObject>())
            HttpStatusCode.NotFound -> AvatarResult.Error("Avatar non trovato")
            HttpStatusCode.Unauthorized -> AvatarResult.Error("Sessione scaduta, rifai il login")
            else -> AvatarResult.Error("Errore avatar (${response.status.value})")
        }
    }

    suspend fun fetchInventory(avatarId: String, token: String): AvatarInventoryResult {
        if (avatarId.isBlank()) {
            return AvatarInventoryResult.Error("Utente non valido")
        }

        val response = runCatching {
            client.get("${edgeServiceBaseUrl()}/api/v1/avatars/$avatarId/inventory") {
                header(HttpHeaders.Authorization, "Bearer $token")
                header(HttpHeaders.Accept, ContentType.Application.Json)
            }
        }.getOrElse {
            return AvatarInventoryResult.Error("Impossibile contattare avatar-service")
        }

        return when (response.status) {
            HttpStatusCode.OK -> {
                val body = response.body<JsonElement>()
                AvatarInventoryResult.Success(parseInventoryFromPayload(body))
            }

            HttpStatusCode.Unauthorized -> AvatarInventoryResult.Error("Sessione scaduta, rifai il login")
            HttpStatusCode.NotFound -> AvatarInventoryResult.Error("Inventario non trovato")
            else -> AvatarInventoryResult.Error("Errore inventario (${response.status.value})")
        }
    }

    private fun mapAvatarResponse(body: JsonObject): AvatarResult {
        val source = body["content"]?.jsonObject ?: body
        val level = source["level"]?.jsonObject ?: buildJsonObject {}
        val health = source["health"]?.jsonObject ?: buildJsonObject {}
        val mana = source["mana"]?.jsonObject ?: buildJsonObject {}
        val stats = source["stats"]?.jsonObject ?: buildJsonObject {}

        val id = source.stringValue("id") ?: return AvatarResult.Error("Risposta avatar senza id")
        val name = source.stringValue("name") ?: return AvatarResult.Error("Risposta avatar senza nome")

        val data = AvatarData(
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
        )
        return AvatarResult.Success(data)
    }

    private fun parseInventoryFromPayload(payload: JsonElement): List<AvatarInventoryItem> {
        val directItems = when (payload) {
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
            }
            .orEmpty()
    }

    private fun asInventoryItem(element: JsonElement): AvatarInventoryItem? {
        val raw = element as? JsonObject ?: return null
        val source = raw["content"]?.jsonObject ?: raw
        val name = source["name"]?.jsonPrimitive?.contentOrNull
            ?: source["itemName"]?.jsonPrimitive?.contentOrNull
            ?: return null

        return AvatarInventoryItem(
            name = name,
            description = source["description"]?.jsonPrimitive?.contentOrNull.orEmpty(),
            type = source["type"]?.jsonPrimitive?.contentOrNull
                ?: source["category"]?.jsonPrimitive?.contentOrNull
                ?: "UNKNOWN",
            power = source["power"]?.jsonPrimitive?.intOrNull,
            price = source["price"]?.jsonPrimitive?.intOrNull ?: 0,
            quantity = source["quantity"]?.jsonPrimitive?.intOrNull
                ?: source["amount"]?.jsonPrimitive?.intOrNull
                ?: 1,
        )
    }
}

private fun JsonObject.stringValue(key: String): String? {
    return (this[key] as? JsonPrimitive)?.contentOrNull
}

private fun JsonObject.intValue(key: String): Int {
    return (this[key] as? JsonPrimitive)?.intOrNull ?: 0
}

