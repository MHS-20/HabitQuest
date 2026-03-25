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
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

sealed interface AvatarResult {
    data class Success(val avatar: AvatarData) : AvatarResult
    data class Error(val message: String) : AvatarResult
}

data class AvatarData(
    val id: String,
    val name: String,
    val level: Int,
    val currentXp: Int,
    val nextLevelXp: Int,
    val hp: Int,
    val maxHp: Int,
    val mana: Int,
    val maxMana: Int,
    val charClass: String,
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
            level = level.intValue("levelNumber"),
            currentXp = level.intValue("currentExperience"),
            nextLevelXp = level.intValue("experienceRequired").coerceAtLeast(1),
            hp = health.intValue("current"),
            maxHp = health.intValue("max").coerceAtLeast(1),
            mana = mana.intValue("amount"),
            maxMana = mana.intValue("max").coerceAtLeast(1),
            charClass = classFromStats(stats)
        )
        return AvatarResult.Success(data)
    }

    private fun classFromStats(stats: JsonObject): String {
        val strength = stats.intValue("strength")
        val defense = stats.intValue("defense")
        val intelligence = stats.intValue("intelligence")
        val max = maxOf(strength, defense, intelligence)

        return when (max) {
            intelligence -> "Mage"
            defense -> "Guardian"
            strength -> "Warrior"
            else -> "Adventurer"
        }
    }
}

private fun JsonObject.stringValue(key: String): String? {
    return (this[key] as? JsonPrimitive)?.contentOrNull
}

private fun JsonObject.intValue(key: String): Int {
    return (this[key] as? JsonPrimitive)?.intOrNull ?: 0
}

