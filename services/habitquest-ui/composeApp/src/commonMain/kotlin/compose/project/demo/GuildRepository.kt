package compose.project.demo

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

data class GuildMemberData(
    val avatarId: String,
    val nickname: String,
    val role: String,
)

data class GuildData(
    val id: String,
    val name: String,
    val globalRank: Int?,
    val members: List<GuildMemberData>,
)

sealed interface GuildCreateResult {
    data class Success(val guildId: String) : GuildCreateResult
    data class Error(val message: String) : GuildCreateResult
}

sealed interface GuildResult {
    data class Success(val guild: GuildData) : GuildResult
    data class Error(val message: String) : GuildResult
}

sealed interface GuildLeaderboardResult {
    data class Success(val guilds: List<GuildData>) : GuildLeaderboardResult
    data class Error(val message: String) : GuildLeaderboardResult
}

class GuildRepository {
    private val client = HttpClient(createHttpEngine()) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    suspend fun createGuild(
        token: String,
        name: String,
        creatorAvatarId: String,
        creatorNickname: String,
    ): GuildCreateResult {
        if (name.isBlank()) return GuildCreateResult.Error("Guild name cannot be blank")
        if (creatorAvatarId.isBlank()) return GuildCreateResult.Error("Avatar not available")

        val response = runCatching {
            client.post("${edgeServiceBaseUrl()}/api/v1/guilds") {
                header(HttpHeaders.Authorization, "Bearer $token")
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                setBody(
                    buildJsonObject {
                        put("name", JsonPrimitive(name))
                        put("creatorAvatarId", JsonPrimitive(creatorAvatarId))
                        put("creatorNickname", JsonPrimitive(creatorNickname))
                    }
                )
            }
        }.getOrElse {
            return GuildCreateResult.Error("Unable to contact guild-service")
        }

        return when (response.status) {
            HttpStatusCode.Created, HttpStatusCode.OK -> {
                val body = response.body<JsonObject>()
                val source = body["content"]?.jsonObject ?: body
                val id = source["id"]?.jsonPrimitive?.contentOrNull
                    ?: return GuildCreateResult.Error("Create guild response missing id")
                GuildCreateResult.Success(id)
            }

            HttpStatusCode.BadRequest -> GuildCreateResult.Error("Invalid guild data")
            HttpStatusCode.Unauthorized -> GuildCreateResult.Error("Session expired, please log in again")
            else -> GuildCreateResult.Error("Create guild error (${response.status.value})")
        }
    }

    suspend fun fetchGuildById(token: String, guildId: String): GuildResult {
        if (guildId.isBlank()) return GuildResult.Error("Guild id cannot be blank")

        val response = runCatching {
            client.get("${edgeServiceBaseUrl()}/api/v1/guilds/$guildId") {
                header(HttpHeaders.Authorization, "Bearer $token")
                header(HttpHeaders.Accept, ContentType.Application.Json)
            }
        }.getOrElse {
            return GuildResult.Error("Unable to contact guild-service")
        }

        return when (response.status) {
            HttpStatusCode.OK -> {
                val body = response.body<JsonObject>()
                val source = body["content"]?.jsonObject ?: body
                val guild = parseGuild(source) ?: return GuildResult.Error("Invalid guild response")
                GuildResult.Success(guild)
            }

            HttpStatusCode.NotFound -> GuildResult.Error("Guild not found")
            HttpStatusCode.Unauthorized -> GuildResult.Error("Session expired, please log in again")
            else -> GuildResult.Error("Guild read error (${response.status.value})")
        }
    }

    suspend fun fetchLeaderboard(token: String): GuildLeaderboardResult {
        val response = runCatching {
            client.get("${edgeServiceBaseUrl()}/api/v1/guilds/leaderboard") {
                header(HttpHeaders.Authorization, "Bearer $token")
                header(HttpHeaders.Accept, ContentType.Application.Json)
            }
        }.getOrElse {
            return GuildLeaderboardResult.Error("Unable to contact guild-service")
        }

        return when (response.status) {
            HttpStatusCode.OK -> {
                val body = response.body<JsonObject>()
                val guilds = parseGuildCollection(body)
                GuildLeaderboardResult.Success(guilds)
            }

            HttpStatusCode.Unauthorized -> GuildLeaderboardResult.Error("Session expired, please log in again")
            else -> GuildLeaderboardResult.Error("Leaderboard read error (${response.status.value})")
        }
    }

    private fun parseGuild(source: JsonObject): GuildData? {
        val id = source["id"]?.jsonPrimitive?.contentOrNull ?: return null
        val name = source["name"]?.jsonPrimitive?.contentOrNull.orEmpty()
        val rank = source["globalRank"]?.jsonPrimitive?.intOrNull
        val members = source["members"]?.jsonArray?.mapNotNull { element ->
            val obj = element as? JsonObject ?: return@mapNotNull null
            GuildMemberData(
                avatarId = obj["avatarId"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null,
                nickname = obj["nickname"]?.jsonPrimitive?.contentOrNull.orEmpty(),
                role = obj["role"]?.jsonPrimitive?.contentOrNull.orEmpty(),
            )
        }.orEmpty()

        return GuildData(id = id, name = name, globalRank = rank, members = members)
    }

    private fun parseGuildCollection(body: JsonObject): List<GuildData> {
        val directArray = body["guilds"] as? JsonArray
        if (directArray != null) {
            return parseGuildArray(directArray)
        }

        val embedded = body["_embedded"]?.jsonObject
        val embeddedItems = embedded?.values?.flatMap { parseGuildElements(it) }.orEmpty()
        if (embeddedItems.isNotEmpty()) {
            return embeddedItems
        }

        val contentArray = body["content"] as? JsonArray
        if (contentArray != null) {
            return parseGuildArray(contentArray)
        }

        return parseGuild(body)?.let(::listOf).orEmpty()
    }

    private fun parseGuildElements(element: JsonElement): List<GuildData> {
        val objectValue = element as? JsonObject
        if (objectValue != null) {
            val maybeContentArray = objectValue["content"] as? JsonArray
            if (maybeContentArray != null) {
                return parseGuildArray(maybeContentArray)
            }
        }

        val arrayValue = element as? JsonArray
        return if (arrayValue != null) parseGuildArray(arrayValue) else emptyList()
    }

    private fun parseGuildArray(array: JsonArray): List<GuildData> {
        return array.mapNotNull { element ->
            val item = element as? JsonObject ?: return@mapNotNull null
            val source = item["content"]?.jsonObject ?: item
            parseGuild(source)
        }
    }
}
