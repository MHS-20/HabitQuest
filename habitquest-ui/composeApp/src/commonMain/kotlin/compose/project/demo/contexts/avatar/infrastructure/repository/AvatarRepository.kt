package compose.project.demo.contexts.avatar.infrastructure.repository

import compose.project.demo.contexts.avatar.domain.contract.AvatarGateway
import compose.project.demo.contexts.avatar.domain.model.AvatarEquippedItemsResult
import compose.project.demo.contexts.avatar.domain.model.AvatarInventoryActionResult
import compose.project.demo.contexts.avatar.domain.model.AvatarInventoryItem
import compose.project.demo.contexts.avatar.domain.model.AvatarInventoryResult
import compose.project.demo.contexts.avatar.domain.model.AvatarResult
import compose.project.demo.contexts.avatar.domain.model.AvatarStatIncrementResult
import compose.project.demo.contexts.avatar.infrastructure.mapper.buildInventoryActionPayload
import compose.project.demo.contexts.avatar.infrastructure.mapper.buildPotionActionPayload
import compose.project.demo.contexts.avatar.infrastructure.mapper.mapAvatarResponse
import compose.project.demo.contexts.avatar.infrastructure.mapper.parseInventoryFromPayload
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
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

class AvatarRepository : AvatarGateway {
    private val client =
        HttpClient(createHttpEngine()) {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }

    override suspend fun fetchAvatar(
        avatarId: String,
        token: String,
    ): AvatarResult {
        if (avatarId.isBlank()) {
            return AvatarResult.Error("Invalid user")
        }

        val response =
            runCatching {
                client.get("${edgeServiceBaseUrl()}/api/v1/avatars/$avatarId") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    header(HttpHeaders.Accept, ContentType.Application.Json)
                }
            }.getOrElse {
                return AvatarResult.Error("Unable to contact avatar-service")
            }

        return when (response.status) {
            HttpStatusCode.OK -> mapAvatarResponse(response.body<JsonObject>())
            HttpStatusCode.NotFound -> AvatarResult.Error("Avatar not found")
            HttpStatusCode.Unauthorized -> AvatarResult.Error("Session expired, please log in again")
            else -> AvatarResult.Error("Avatar error (${response.status.value})")
        }
    }

    override suspend fun fetchInventory(
        avatarId: String,
        token: String,
    ): AvatarInventoryResult {
        if (avatarId.isBlank()) {
            return AvatarInventoryResult.Error("Invalid user")
        }

        val response =
            runCatching {
                client.get("${edgeServiceBaseUrl()}/api/v1/avatars/$avatarId/inventory") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    header(HttpHeaders.Accept, ContentType.Application.Json)
                }
            }.getOrElse {
                return AvatarInventoryResult.Error("Unable to contact avatar-service")
            }

        return when (response.status) {
            HttpStatusCode.OK -> {
                val body = response.body<JsonElement>()
                AvatarInventoryResult.Success(parseInventoryFromPayload(body))
            }

            HttpStatusCode.Unauthorized -> {
                AvatarInventoryResult.Error("Session expired, please log in again")
            }

            HttpStatusCode.NotFound -> {
                AvatarInventoryResult.Error("Inventory not found")
            }

            else -> {
                AvatarInventoryResult.Error("Inventory error (${response.status.value})")
            }
        }
    }

    override suspend fun fetchEquippedItems(
        avatarId: String,
        token: String,
    ): AvatarEquippedItemsResult {
        if (avatarId.isBlank()) {
            return AvatarEquippedItemsResult.Error("Invalid user")
        }

        val response =
            runCatching {
                client.get("${edgeServiceBaseUrl()}/api/v1/avatars/$avatarId/equipped-items") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    header(HttpHeaders.Accept, ContentType.Application.Json)
                }
            }.getOrElse {
                return AvatarEquippedItemsResult.Error("Unable to contact avatar-service")
            }

        return when (response.status) {
            HttpStatusCode.OK -> {
                val body = response.body<JsonElement>()
                AvatarEquippedItemsResult.Success(parseInventoryFromPayload(body))
            }

            HttpStatusCode.Unauthorized -> {
                AvatarEquippedItemsResult.Error("Session expired, please log in again")
            }

            HttpStatusCode.NotFound -> {
                AvatarEquippedItemsResult.Error("Equipped inventory not found")
            }

            else -> {
                AvatarEquippedItemsResult.Error("Equipped items error (${response.status.value})")
            }
        }
    }

    override suspend fun equipItem(
        token: String,
        avatarId: String,
        item: AvatarInventoryItem,
    ): AvatarInventoryActionResult =
        sendInventoryAction(
            token = token,
            avatarId = avatarId,
            path = "equip",
            item = item,
            errorPrefix = "Equip",
        )

    override suspend fun unequipItem(
        token: String,
        avatarId: String,
        item: AvatarInventoryItem,
    ): AvatarInventoryActionResult =
        sendInventoryAction(
            token = token,
            avatarId = avatarId,
            path = "unequip",
            item = item,
            errorPrefix = "Unequip",
        )

    override suspend fun usePotion(
        token: String,
        avatarId: String,
        item: AvatarInventoryItem,
    ): AvatarInventoryActionResult {
        val potionPath =
            resolvePotionPath(item)
                ?: return AvatarInventoryActionResult.Error("Unsupported potion type: ${item.type}")

        return sendPotionAction(
            token = token,
            avatarId = avatarId,
            path = potionPath,
            item = item,
            errorPrefix = "Use potion",
        )
    }

    private fun resolvePotionPath(item: AvatarInventoryItem): String? {
        val type = item.type.trim().uppercase()
        val name = item.name.trim().uppercase()

        return when {
            type in setOf("HEALTH_POTION", "POTION_HEALTH", "HEALTH", "HP_POTION") -> "health/potion"
            type in setOf("MANA_POTION", "POTION_MANA", "MANA", "MP_POTION") -> "mana/potion"
            type in setOf("POTION", "CONSUMABLE") && "MANA" in name -> "mana/potion"
            type in setOf("POTION", "CONSUMABLE") && "HEALTH" in name -> "health/potion"
            "MANA" in name -> "mana/potion"
            "HEALTH" in name || "HP" in name -> "health/potion"
            else -> null
        }
    }

    override suspend fun increaseStrength(
        token: String,
        avatarId: String,
    ): AvatarStatIncrementResult = sendStatIncrement(token, avatarId, "strength", "Increase Strength")

    override suspend fun increaseDefense(
        token: String,
        avatarId: String,
    ): AvatarStatIncrementResult = sendStatIncrement(token, avatarId, "defense", "Increase Defense")

    override suspend fun increaseIntelligence(
        token: String,
        avatarId: String,
    ): AvatarStatIncrementResult = sendStatIncrement(token, avatarId, "intelligence", "Increase Intelligence")

    private suspend fun sendStatIncrement(
        token: String,
        avatarId: String,
        statName: String,
        errorPrefix: String,
    ): AvatarStatIncrementResult {
        if (avatarId.isBlank()) {
            return AvatarStatIncrementResult.Error("Invalid user")
        }

        val response =
            runCatching {
                client.post("${edgeServiceBaseUrl()}/api/v1/avatars/$avatarId/stats/$statName") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    header(HttpHeaders.ContentType, ContentType.Application.Json)
                }
            }.getOrElse {
                return AvatarStatIncrementResult.Error("Unable to contact avatar-service")
            }

        return when (response.status) {
            HttpStatusCode.NoContent,
            HttpStatusCode.OK,
            -> {
                AvatarStatIncrementResult.Success
            }

            HttpStatusCode.Unauthorized -> {
                AvatarStatIncrementResult.Error("Session expired, please log in again")
            }

            HttpStatusCode.NotFound -> {
                AvatarStatIncrementResult.Error("Avatar not found")
            }

            HttpStatusCode.BadRequest -> {
                val body = runCatching { response.body<JsonObject>() }.getOrNull()
                val message = body?.get("message")?.jsonPrimitive?.contentOrNull
                AvatarStatIncrementResult.Error(message ?: "$errorPrefix failed")
            }

            else -> {
                AvatarStatIncrementResult.Error("$errorPrefix failed (${response.status.value})")
            }
        }
    }

    private suspend fun sendInventoryAction(
        token: String,
        avatarId: String,
        path: String,
        item: AvatarInventoryItem,
        errorPrefix: String,
    ): AvatarInventoryActionResult {
        if (avatarId.isBlank()) {
            return AvatarInventoryActionResult.Error("Invalid user")
        }

        val response =
            runCatching {
                client.post("${edgeServiceBaseUrl()}/api/v1/avatars/$avatarId/inventory/items/$path") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    header(HttpHeaders.ContentType, ContentType.Application.Json)
                    setBody(buildInventoryActionPayload(item))
                }
            }.getOrElse {
                return AvatarInventoryActionResult.Error("Unable to contact avatar-service")
            }

        return when (response.status) {
            HttpStatusCode.NoContent,
            HttpStatusCode.OK,
            -> {
                AvatarInventoryActionResult.Success
            }

            HttpStatusCode.Unauthorized -> {
                AvatarInventoryActionResult.Error("Session expired, please log in again")
            }

            HttpStatusCode.NotFound -> {
                AvatarInventoryActionResult.Error("Item not found")
            }

            HttpStatusCode.BadRequest -> {
                val body = runCatching { response.body<JsonObject>() }.getOrNull()
                val message = body?.get("message")?.jsonPrimitive?.contentOrNull
                AvatarInventoryActionResult.Error(message ?: "$errorPrefix failed")
            }

            else -> {
                AvatarInventoryActionResult.Error("$errorPrefix failed (${response.status.value})")
            }
        }
    }

    private suspend fun sendPotionAction(
        token: String,
        avatarId: String,
        path: String,
        item: AvatarInventoryItem,
        errorPrefix: String,
    ): AvatarInventoryActionResult {
        if (avatarId.isBlank()) {
            return AvatarInventoryActionResult.Error("Invalid user")
        }

        val response =
            runCatching {
                client.post("${edgeServiceBaseUrl()}/api/v1/avatars/$avatarId/$path") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    header(HttpHeaders.ContentType, ContentType.Application.Json)
                    setBody(buildPotionActionPayload(item))
                }
            }.getOrElse {
                return AvatarInventoryActionResult.Error("Unable to contact avatar-service")
            }

        return when (response.status) {
            HttpStatusCode.NoContent,
            HttpStatusCode.OK,
            -> {
                AvatarInventoryActionResult.Success
            }

            HttpStatusCode.Unauthorized -> {
                AvatarInventoryActionResult.Error("Session expired, please log in again")
            }

            HttpStatusCode.NotFound -> {
                AvatarInventoryActionResult.Error("Potion not found")
            }

            HttpStatusCode.BadRequest -> {
                val body = runCatching { response.body<JsonObject>() }.getOrNull()
                val message = body?.get("message")?.jsonPrimitive?.contentOrNull
                AvatarInventoryActionResult.Error(message ?: "$errorPrefix failed")
            }

            else -> {
                AvatarInventoryActionResult.Error("$errorPrefix failed (${response.status.value})")
            }
        }
    }
}
