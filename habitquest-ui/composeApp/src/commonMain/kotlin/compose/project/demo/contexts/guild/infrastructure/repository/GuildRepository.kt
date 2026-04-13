@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package compose.project.demo.contexts.guild.infrastructure.repository

import compose.project.demo.createHttpEngine
import compose.project.demo.edgeServiceBaseUrl
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
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

data class GuildMemberData(val avatarId: String, val nickname: String, val role: String)

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

sealed interface SearchAvatarResult {
  data class Success(val avatar: SearchAvatarData) : SearchAvatarResult

  data class Error(val message: String) : SearchAvatarResult
}

sealed interface InviteAvatarResult {
  data object Success : InviteAvatarResult

  data class Error(val message: String) : InviteAvatarResult
}

data class SearchAvatarData(val id: String, val name: String)

data class PendingInviteData(
  val inviteId: String,
  val guildId: String,
  val guildName: String,
  val expiresAt: String?,
)

sealed interface PendingInvitesResult {
  data class Success(val invites: List<PendingInviteData>) : PendingInvitesResult

  data class Error(val message: String) : PendingInvitesResult
}

sealed interface AcceptInviteResult {
  data object Success : AcceptInviteResult

  data class Error(val message: String) : AcceptInviteResult
}

data class BossData(
  val name: String,
  val type: String,
  val health: Int,
  val strength: Int,
  val defense: Int,
  val experienceReward: Int,
  val moneyReward: Int,
  val penalty: Int,
)

sealed interface BossesResult {
  data class Success(val bosses: List<BossData>) : BossesResult

  data class Error(val message: String) : BossesResult
}

sealed interface BattleStartResult {
  data class Success(val battleId: String) : BattleStartResult

  data class Error(val message: String) : BattleStartResult
}

data class BattleStatsData(
  val battleId: String,
  val guildId: String,
  val status: String,
  val currentTurn: Int,
  val totalTurns: Int,
  val bossName: String,
  val bossRemainingHealth: Int,
)

sealed interface BattleStatsResult {
  data class Success(val stats: BattleStatsData) : BattleStatsResult

  data class Error(val message: String) : BattleStatsResult
}

sealed interface BattleAttackResult {
  data object Success : BattleAttackResult

  data class Error(val message: String) : BattleAttackResult
}

class GuildRepository {
  private val client =
    HttpClient(createHttpEngine()) {
      install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    }

  suspend fun createGuild(
    token: String,
    name: String,
    creatorAvatarId: String,
    creatorNickname: String,
  ): GuildCreateResult {
    if (name.isBlank()) return GuildCreateResult.Error("Guild name cannot be blank")
    if (creatorAvatarId.isBlank()) return GuildCreateResult.Error("Avatar not available")

    val response =
      runCatching {
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
        }
        .getOrElse {
          return GuildCreateResult.Error("Unable to contact guild-service")
        }

    return when (response.status) {
      HttpStatusCode.Created,
      HttpStatusCode.OK -> {
        val body = response.body<JsonObject>()
        val source = body["content"]?.jsonObject ?: body
        val id =
          source["id"]?.jsonPrimitive?.contentOrNull
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

    val response =
      runCatching {
          client.get("${edgeServiceBaseUrl()}/api/v1/guilds/$guildId") {
            header(HttpHeaders.Authorization, "Bearer $token")
            header(HttpHeaders.Accept, ContentType.Application.Json)
          }
        }
        .getOrElse {
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
    val response =
      runCatching {
          client.get("${edgeServiceBaseUrl()}/api/v1/guilds/leaderboard") {
            header(HttpHeaders.Authorization, "Bearer $token")
            header(HttpHeaders.Accept, ContentType.Application.Json)
          }
        }
        .getOrElse {
          return GuildLeaderboardResult.Error("Unable to contact guild-service")
        }

    return when (response.status) {
      HttpStatusCode.OK -> {
        val body = response.body<JsonObject>()
        val guilds = parseGuildCollection(body)
        GuildLeaderboardResult.Success(guilds)
      }

      HttpStatusCode.Unauthorized ->
        GuildLeaderboardResult.Error("Session expired, please log in again")
      else -> GuildLeaderboardResult.Error("Leaderboard read error (${response.status.value})")
    }
  }

  suspend fun searchAvatar(token: String, search: String): SearchAvatarResult {
    if (search.isBlank()) return SearchAvatarResult.Error("Search cannot be empty")

    val response =
      runCatching {
          client.post("${edgeServiceBaseUrl()}/api/v1/avatars/search") {
            header(HttpHeaders.Authorization, "Bearer $token")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            header(HttpHeaders.Accept, ContentType.Application.Json)
            setBody(
              buildJsonObject {
                put("name", JsonPrimitive(search.trim()))
                put("minLevel", null)
                put("maxLevel", null)
              }
            )
          }
        }
        .getOrElse {
          return SearchAvatarResult.Error("Unable to contact avatar-service")
        }

    return when (response.status) {
      HttpStatusCode.OK -> {
        val body = response.body<JsonObject>()

        // Parse the response which may contain _embedded.avatarResponseList
        val embedded = body["_embedded"]?.jsonObject
        val avatarList = embedded?.get("avatarResponseList")?.jsonArray

        if (avatarList != null && avatarList.isNotEmpty()) {
          val firstAvatar =
            avatarList[0] as? JsonObject
              ?: return SearchAvatarResult.Error("Invalid avatar response")
          val id = firstAvatar["id"]?.jsonPrimitive?.contentOrNull
          val name = firstAvatar["name"]?.jsonPrimitive?.contentOrNull

          if (id != null && name != null) {
            SearchAvatarResult.Success(SearchAvatarData(id, name))
          } else {
            SearchAvatarResult.Error("Invalid avatar response")
          }
        } else {
          SearchAvatarResult.Error("Avatar not found")
        }
      }

      HttpStatusCode.NotFound -> SearchAvatarResult.Error("Avatar not found")
      HttpStatusCode.Unauthorized ->
        SearchAvatarResult.Error("Session expired, please log in again")
      else -> SearchAvatarResult.Error("Search error (${response.status.value})")
    }
  }

  suspend fun inviteAvatarToGuild(
    token: String,
    guildId: String,
    requestorId: String,
    avatarId: String,
  ): InviteAvatarResult {
    if (guildId.isBlank()) return InviteAvatarResult.Error("Guild id cannot be blank")
    if (requestorId.isBlank()) return InviteAvatarResult.Error("Requestor id cannot be blank")
    if (avatarId.isBlank()) return InviteAvatarResult.Error("Avatar id cannot be blank")

    val response =
      runCatching {
          client.post("${edgeServiceBaseUrl()}/api/v1/guilds/$guildId/invites") {
            header(HttpHeaders.Authorization, "Bearer $token")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(
              buildJsonObject {
                put("requestorId", JsonPrimitive(requestorId))
                put("targetAvatarId", JsonPrimitive(avatarId))
              }
            )
          }
        }
        .getOrElse {
          return InviteAvatarResult.Error("Unable to contact guild-service")
        }

    return when (response.status) {
      HttpStatusCode.OK,
      HttpStatusCode.Created,
      HttpStatusCode.NoContent -> InviteAvatarResult.Success
      HttpStatusCode.BadRequest -> InviteAvatarResult.Error("Invalid guild or avatar data")
      HttpStatusCode.Unauthorized ->
        InviteAvatarResult.Error("Session expired, please log in again")
      HttpStatusCode.NotFound -> InviteAvatarResult.Error("Guild or avatar not found")
      else -> InviteAvatarResult.Error("Invite error (${response.status.value})")
    }
  }

  suspend fun fetchPendingInvites(token: String, avatarId: String): PendingInvitesResult {
    if (avatarId.isBlank()) return PendingInvitesResult.Error("Avatar id cannot be blank")

    val response =
      runCatching {
          client.get("${edgeServiceBaseUrl()}/api/v1/avatars/$avatarId/invites") {
            header(HttpHeaders.Authorization, "Bearer $token")
            header(HttpHeaders.Accept, ContentType.Application.Json)
          }
        }
        .getOrElse {
          return PendingInvitesResult.Error("Unable to contact avatar-service")
        }

    return when (response.status) {
      HttpStatusCode.OK -> {
        val body = response.body<JsonObject>()
        PendingInvitesResult.Success(parsePendingInviteCollection(body))
      }

      HttpStatusCode.NotFound -> PendingInvitesResult.Error("Avatar not found")
      HttpStatusCode.Unauthorized ->
        PendingInvitesResult.Error("Session expired, please log in again")
      else -> PendingInvitesResult.Error("Pending invites error (${response.status.value})")
    }
  }

  suspend fun acceptInvite(
    token: String,
    avatarId: String,
    inviteId: String,
    guildId: String,
    nickname: String,
  ): AcceptInviteResult {
    if (avatarId.isBlank()) return AcceptInviteResult.Error("Avatar id cannot be blank")
    if (inviteId.isBlank()) return AcceptInviteResult.Error("Invite id cannot be blank")
    if (guildId.isBlank()) return AcceptInviteResult.Error("Guild id cannot be blank")
    if (nickname.isBlank()) return AcceptInviteResult.Error("Nickname cannot be blank")

    val avatarResponse =
      runCatching {
          client.post("${edgeServiceBaseUrl()}/api/v1/avatars/$avatarId/invites/$inviteId/accept") {
            header(HttpHeaders.Authorization, "Bearer $token")
            header(HttpHeaders.Accept, ContentType.Application.Json)
          }
        }
        .getOrElse {
          return AcceptInviteResult.Error("Unable to contact avatar-service")
        }

    when (avatarResponse.status) {
      HttpStatusCode.NoContent,
      HttpStatusCode.OK -> Unit
      HttpStatusCode.NotFound -> AcceptInviteResult.Error("Invite or avatar not found")
      HttpStatusCode.Unauthorized ->
        AcceptInviteResult.Error("Session expired, please log in again")
      HttpStatusCode.BadRequest -> AcceptInviteResult.Error("Invalid invite request")
      else ->
        return AcceptInviteResult.Error("Accept invite error (${avatarResponse.status.value})")
    }

    val guildResponse =
      runCatching {
          client.post("${edgeServiceBaseUrl()}/api/v1/guilds/$guildId/invites/$inviteId/accept") {
            header(HttpHeaders.Authorization, "Bearer $token")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            header(HttpHeaders.Accept, ContentType.Application.Json)
            setBody(
              buildJsonObject {
                put("avatarId", JsonPrimitive(avatarId))
                put("nickname", JsonPrimitive(nickname))
              }
            )
          }
        }
        .getOrElse {
          return AcceptInviteResult.Error(
            "Invite accepted on avatar-service, but failed to contact guild-service"
          )
        }

    return when (guildResponse.status) {
      HttpStatusCode.NoContent,
      HttpStatusCode.OK -> AcceptInviteResult.Success
      HttpStatusCode.NotFound -> AcceptInviteResult.Error("Guild or invite not found")
      HttpStatusCode.Unauthorized ->
        AcceptInviteResult.Error("Session expired, please log in again")
      HttpStatusCode.Forbidden -> AcceptInviteResult.Error("Not allowed to accept this invite")
      HttpStatusCode.BadRequest ->
        AcceptInviteResult.Error("Invalid guild invite acceptance request")
      else -> AcceptInviteResult.Error("Guild accept invite error (${guildResponse.status.value})")
    }
  }

  private fun parseGuild(source: JsonObject): GuildData? {
    val id = source["id"]?.jsonPrimitive?.contentOrNull ?: return null
    val name = source["name"]?.jsonPrimitive?.contentOrNull.orEmpty()
    val rank = source["globalRank"]?.jsonPrimitive?.intOrNull
    val members =
      source["members"]
        ?.jsonArray
        ?.mapNotNull { element ->
          val obj = element as? JsonObject ?: return@mapNotNull null
          GuildMemberData(
            avatarId = obj["avatarId"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null,
            nickname = obj["nickname"]?.jsonPrimitive?.contentOrNull.orEmpty(),
            role = obj["role"]?.jsonPrimitive?.contentOrNull.orEmpty(),
          )
        }
        .orEmpty()

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

  private fun parsePendingInviteCollection(body: JsonObject): List<PendingInviteData> {
    val directArray = body["invites"] as? JsonArray
    if (directArray != null) {
      return parsePendingInviteArray(directArray)
    }

    val embedded = body["_embedded"]?.jsonObject
    val embeddedItems = embedded?.values?.flatMap { parsePendingInviteElements(it) }.orEmpty()
    if (embeddedItems.isNotEmpty()) {
      return embeddedItems
    }

    val contentArray = body["content"] as? JsonArray
    if (contentArray != null) {
      return parsePendingInviteArray(contentArray)
    }

    return parsePendingInvite(body)?.let(::listOf).orEmpty()
  }

  private fun parsePendingInviteElements(element: JsonElement): List<PendingInviteData> {
    val objectValue = element as? JsonObject
    if (objectValue != null) {
      val maybeContentArray = objectValue["content"] as? JsonArray
      if (maybeContentArray != null) {
        return parsePendingInviteArray(maybeContentArray)
      }
    }

    val arrayValue = element as? JsonArray
    return if (arrayValue != null) parsePendingInviteArray(arrayValue) else emptyList()
  }

  private fun parsePendingInviteArray(array: JsonArray): List<PendingInviteData> {
    return array.mapNotNull { element ->
      val item = element as? JsonObject ?: return@mapNotNull null
      val source = item["content"]?.jsonObject ?: item
      parsePendingInvite(source)
    }
  }

  private fun parsePendingInvite(source: JsonObject): PendingInviteData? {
    val inviteId = source["inviteId"]?.jsonPrimitive?.contentOrNull ?: return null
    val guildId = source["guildId"]?.jsonPrimitive?.contentOrNull ?: return null
    val guildName = source["guildName"]?.jsonPrimitive?.contentOrNull.orEmpty()
    val expiresAt = source["expiresAt"]?.jsonPrimitive?.contentOrNull
    return PendingInviteData(
      inviteId = inviteId,
      guildId = guildId,
      guildName = guildName,
      expiresAt = expiresAt,
    )
  }

  suspend fun fetchBosses(token: String): BossesResult {
    val response =
      runCatching {
          client.get("${edgeServiceBaseUrl()}/api/v1/battles/boss") {
            header(HttpHeaders.Authorization, "Bearer $token")
            header(HttpHeaders.Accept, ContentType.Application.Json)
          }
        }
        .getOrElse {
          return BossesResult.Error("Unable to contact battle-service")
        }

    return when (response.status) {
      HttpStatusCode.OK -> {
        val body = response.body<JsonObject>()
        val bosses = parseBossCollection(body)
        BossesResult.Success(bosses)
      }

      HttpStatusCode.Unauthorized -> BossesResult.Error("Session expired, please log in again")
      else -> BossesResult.Error("Bosses fetch error (${response.status.value})")
    }
  }

  suspend fun initiateBattle(
    token: String,
    guildId: String,
    requesterId: String,
    bossType: String,
  ): BattleStartResult {
    if (guildId.isBlank()) return BattleStartResult.Error("Guild id cannot be blank")
    if (requesterId.isBlank()) return BattleStartResult.Error("Requestor id cannot be blank")
    if (bossType.isBlank()) return BattleStartResult.Error("Boss type cannot be blank")

    val response =
      runCatching {
          client.post("${edgeServiceBaseUrl()}/api/v1/battles") {
            header(HttpHeaders.Authorization, "Bearer $token")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(
              buildJsonObject {
                put("guildId", JsonPrimitive(guildId))
                put("requesterId", JsonPrimitive(requesterId))
                put("bossType", JsonPrimitive(bossType))
              }
            )
          }
        }
        .getOrElse {
          return BattleStartResult.Error("Unable to contact battle-service")
        }

    return when (response.status) {
      HttpStatusCode.Created,
      HttpStatusCode.OK -> {
        val body = response.body<JsonObject>()
        val source = body["content"]?.jsonObject ?: body
        val battleId =
          source["id"]?.jsonPrimitive?.contentOrNull
            ?: return BattleStartResult.Error("Battle response missing id")
        BattleStartResult.Success(battleId)
      }

      HttpStatusCode.BadRequest -> BattleStartResult.Error("Invalid battle data")
      HttpStatusCode.Unauthorized -> BattleStartResult.Error("Session expired, please log in again")
      HttpStatusCode.Forbidden -> BattleStartResult.Error("Only guild leaders can start battles")
      HttpStatusCode.NotFound -> BattleStartResult.Error("Guild not found")
      else -> BattleStartResult.Error("Battle start error (${response.status.value})")
    }
  }

  suspend fun fetchBattleStatsByGuild(token: String, guildId: String): BattleStatsResult {
    if (guildId.isBlank()) return BattleStatsResult.Error("Guild id cannot be blank")

    val response =
      runCatching {
          client.get("${edgeServiceBaseUrl()}/api/v1/battles/guild/$guildId") {
            header(HttpHeaders.Authorization, "Bearer $token")
            header(HttpHeaders.Accept, ContentType.Application.Json)
          }
        }
        .getOrElse {
          return BattleStatsResult.Error("Unable to contact battle-service")
        }

    return when (response.status) {
      HttpStatusCode.OK -> {
        val body = response.body<JsonObject>()
        val source = body["content"]?.jsonObject ?: body
        val stats =
          parseBattleStats(source)
            ?: return BattleStatsResult.Error("Invalid battle stats response")
        BattleStatsResult.Success(stats)
      }

      HttpStatusCode.NotFound -> BattleStatsResult.Error("No active battle for this guild")
      HttpStatusCode.Unauthorized -> BattleStatsResult.Error("Session expired, please log in again")
      else -> BattleStatsResult.Error("Battle stats error (${response.status.value})")
    }
  }

  suspend fun attackBattle(
    token: String,
    battleId: String,
    attackerAvatarId: String,
    damage: Int,
  ): BattleAttackResult {
    if (battleId.isBlank()) return BattleAttackResult.Error("Battle id cannot be blank")
    if (attackerAvatarId.isBlank())
      return BattleAttackResult.Error("Attacker avatar id cannot be blank")
    if (damage <= 0) return BattleAttackResult.Error("Damage must be greater than zero")

    val response =
      runCatching {
          client.post("${edgeServiceBaseUrl()}/api/v1/battles/$battleId/damage") {
            header(HttpHeaders.Authorization, "Bearer $token")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            header(HttpHeaders.Accept, ContentType.Application.Json)
            setBody(
              buildJsonObject {
                put("damage", JsonPrimitive(damage))
                put("attackerAvatarId", JsonPrimitive(attackerAvatarId))
              }
            )
          }
        }
        .getOrElse {
          return BattleAttackResult.Error("Unable to contact battle-service")
        }

    return when (response.status) {
      HttpStatusCode.NoContent,
      HttpStatusCode.OK -> BattleAttackResult.Success
      HttpStatusCode.Forbidden -> BattleAttackResult.Error("It's not your turn")
      HttpStatusCode.BadRequest -> BattleAttackResult.Error("Invalid attack request")
      HttpStatusCode.NotFound -> BattleAttackResult.Error("Battle not found")
      HttpStatusCode.Unauthorized ->
        BattleAttackResult.Error("Session expired, please log in again")
      else -> BattleAttackResult.Error("Attack error (${response.status.value})")
    }
  }

  private fun parseBossCollection(body: JsonObject): List<BossData> {
    val directArray = body["bosses"] as? JsonArray
    if (directArray != null) {
      return parseBossArray(directArray)
    }

    val embedded = body["_embedded"]?.jsonObject
    val embeddedItems = embedded?.values?.flatMap { parseBossElements(it) }.orEmpty()
    if (embeddedItems.isNotEmpty()) {
      return embeddedItems
    }

    val contentArray = body["content"] as? JsonArray
    if (contentArray != null) {
      return parseBossArray(contentArray)
    }

    return parseBoss(body)?.let(::listOf).orEmpty()
  }

  private fun parseBossElements(element: JsonElement): List<BossData> {
    val objectValue = element as? JsonObject
    if (objectValue != null) {
      val maybeContentArray = objectValue["content"] as? JsonArray
      if (maybeContentArray != null) {
        return parseBossArray(maybeContentArray)
      }
    }

    val arrayValue = element as? JsonArray
    return if (arrayValue != null) parseBossArray(arrayValue) else emptyList()
  }

  private fun parseBossArray(array: JsonArray): List<BossData> {
    return array.mapNotNull { element ->
      val item = element as? JsonObject ?: return@mapNotNull null
      val source = item["content"]?.jsonObject ?: item
      parseBoss(source)
    }
  }

  private fun parseBoss(source: JsonObject): BossData? {
    val name = source["name"]?.jsonPrimitive?.contentOrNull ?: return null
    val type =
      source["type"]?.jsonPrimitive?.contentOrNull?.trim()?.takeIf { it.isNotBlank() }
        ?: name.trim().uppercase().replace(' ', '_')
    val health = source["health"]?.jsonPrimitive?.intOrNull ?: 0
    val strength = source["strength"]?.jsonPrimitive?.intOrNull ?: 0
    val defense = source["defense"]?.jsonPrimitive?.intOrNull ?: 0
    val experienceReward = source["experienceReward"]?.jsonPrimitive?.intOrNull ?: 0
    val moneyReward = source["moneyReward"]?.jsonPrimitive?.intOrNull ?: 0
    val penalty = source["penalty"]?.jsonPrimitive?.intOrNull ?: 0

    return BossData(
      name = name,
      type = type,
      health = health,
      strength = strength,
      defense = defense,
      experienceReward = experienceReward,
      moneyReward = moneyReward,
      penalty = penalty,
    )
  }

  private fun parseBattleStats(source: JsonObject): BattleStatsData? {
    val battleId = source["id"]?.jsonPrimitive?.contentOrNull ?: return null
    val guildId = source["guildId"]?.jsonPrimitive?.contentOrNull ?: return null
    val currentTurn = source["currentTurn"]?.jsonPrimitive?.intOrNull ?: 0
    val totalTurns = source["numOfTurns"]?.jsonPrimitive?.intOrNull ?: 0
    val bossRemainingHealth = source["bossRemainingHealth"]?.jsonPrimitive?.intOrNull ?: 0
    val bossName =
      source["boss"]?.jsonObject?.get("name")?.jsonPrimitive?.contentOrNull ?: "Unknown"

    val statusElement = source["status"]
    val status =
      when (statusElement) {
        null -> "UNKNOWN"
        is JsonPrimitive -> statusElement.contentOrNull?.uppercase() ?: "UNKNOWN"
        is JsonObject -> {
          when {
            statusElement.isEmpty() -> "ONGOING"
            statusElement.containsKey("experienceReward") &&
              statusElement.containsKey("moneyReward") -> "WON"
            statusElement.containsKey("penalty") -> "LOST"
            else -> statusElement.keys.firstOrNull()?.uppercase() ?: "UNKNOWN"
          }
        }
        else -> "UNKNOWN"
      }

    return BattleStatsData(
      battleId = battleId,
      guildId = guildId,
      status = status,
      currentTurn = currentTurn,
      totalTurns = totalTurns,
      bossName = bossName,
      bossRemainingHealth = bossRemainingHealth,
    )
  }
}
