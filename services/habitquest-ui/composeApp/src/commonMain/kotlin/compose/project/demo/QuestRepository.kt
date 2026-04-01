package compose.project.demo

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

data class QuestData(
    val id: String,
    val name: String,
    val duration: String,
    val reward: Int,
    val habitIds: List<String>,
)

sealed interface CreateQuestResult {
    data class Success(val questId: String) : CreateQuestResult
    data class Error(val message: String) : CreateQuestResult
}

sealed interface QuestResult {
    data class Success(val quest: QuestData) : QuestResult
    data class Error(val message: String) : QuestResult
}

sealed interface QuestListResult {
    data class Success(val quests: List<QuestData>) : QuestListResult
    data class Error(val message: String) : QuestListResult
}

class QuestRepository {
    private val client = HttpClient(createHttpEngine()) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    suspend fun createQuest(token: String, name: String): CreateQuestResult {
        val response = runCatching {
            client.post("${edgeServiceBaseUrl()}/api/v1/quests") {
                header(HttpHeaders.Authorization, "Bearer $token")
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                setBody(
                    buildJsonObject {
                        put("name", JsonPrimitive(name))
                    }
                )
            }
        }.getOrElse {
            return CreateQuestResult.Error("Impossibile contattare quest-service")
        }

        return when (response.status) {
            HttpStatusCode.Created, HttpStatusCode.OK -> {
                val body = response.body<JsonObject>()
                val source = body["content"]?.jsonObject ?: body
                val id = source["id"]?.jsonPrimitive?.contentOrNull
                    ?: return CreateQuestResult.Error("Risposta create quest senza id")
                CreateQuestResult.Success(id)
            }

            HttpStatusCode.BadRequest -> CreateQuestResult.Error("Dati quest non validi")
            HttpStatusCode.Unauthorized -> CreateQuestResult.Error("Sessione scaduta, rifai il login")
            else -> CreateQuestResult.Error("Errore create quest (${response.status.value})")
        }
    }

    suspend fun fetchQuestById(token: String, questId: String): QuestResult {
        val response = runCatching {
            client.get("${edgeServiceBaseUrl()}/api/v1/quests/$questId") {
                header(HttpHeaders.Authorization, "Bearer $token")
                header(HttpHeaders.Accept, ContentType.Application.Json)
            }
        }.getOrElse {
            return QuestResult.Error("Impossibile contattare quest-service")
        }

        return when (response.status) {
            HttpStatusCode.OK -> {
                val body = response.body<JsonObject>()
                val source = body["content"]?.jsonObject ?: body
                val quest = parseQuest(source) ?: return QuestResult.Error("Risposta quest non valida")
                QuestResult.Success(quest)
            }

            HttpStatusCode.NotFound -> QuestResult.Error("Quest non trovata")
            HttpStatusCode.Unauthorized -> QuestResult.Error("Sessione scaduta, rifai il login")
            else -> QuestResult.Error("Errore lettura quest (${response.status.value})")
        }
    }

    suspend fun fetchAllQuests(token: String): QuestListResult {
        val response = runCatching {
            client.get("${edgeServiceBaseUrl()}/api/v1/quests") {
                header(HttpHeaders.Authorization, "Bearer $token")
                header(HttpHeaders.Accept, ContentType.Application.Json)
            }
        }.getOrElse {
            return QuestListResult.Error("Impossibile contattare quest-service")
        }

        return when (response.status) {
            HttpStatusCode.OK -> {
                val body = response.body<JsonObject>()
                val embedded = body["_embedded"]?.jsonObject
                val quests = embedded
                    ?.values
                    ?.flatMap { value ->
                        value.jsonArray.mapNotNull { element ->
                            val item = element as? JsonObject ?: return@mapNotNull null
                            val source = item["content"]?.jsonObject ?: item
                            parseQuest(source)
                        }
                    }
                    .orEmpty()
                QuestListResult.Success(quests)
            }

            HttpStatusCode.Unauthorized -> QuestListResult.Error("Sessione scaduta, rifai il login")
            else -> QuestListResult.Error("Errore lettura quests (${response.status.value})")
        }
    }

    private fun parseQuest(source: JsonObject): QuestData? {
        val id = source["id"]?.jsonPrimitive?.contentOrNull ?: return null
        return QuestData(
            id = id,
            name = source["name"]?.jsonPrimitive?.contentOrNull.orEmpty(),
            duration = source["duration"]?.jsonPrimitive?.contentOrNull ?: "PT0S",
            reward = source["reward"]?.jsonPrimitive?.intOrNull ?: 0,
            habitIds = source["habitIds"]?.jsonArray?.mapNotNull { it.jsonPrimitive.contentOrNull }
                ?: emptyList(),
        )
    }

    suspend fun createQuestWithDetails(
        token: String,
        name: String,
        duration: String,
        habits: List<HabitListItem>,
    ): CreateQuestResult {
        val created = createQuest(token, name)
        if (created is CreateQuestResult.Error) {
            return created
        }

        val questId = (created as CreateQuestResult.Success).questId

        if (duration.isNotBlank()) {
            val durationUpdated = updateQuestDuration(token, questId, duration)
            if (!durationUpdated) {
                return CreateQuestResult.Error("Quest creata ma durata non aggiornata")
            }
        }

        for (habit in habits) {
            val habitAdded = addHabitToQuest(token, questId, habit)
            if (!habitAdded) {
                return CreateQuestResult.Error("Quest creata ma non tutte le habits sono state aggiunte")
            }
        }

        return CreateQuestResult.Success(questId)
    }

    private suspend fun updateQuestDuration(token: String, questId: String, duration: String): Boolean {
        val response = runCatching {
            client.patch("${edgeServiceBaseUrl()}/api/v1/quests/$questId/duration") {
                header(HttpHeaders.Authorization, "Bearer $token")
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                setBody(
                    buildJsonObject {
                        put("duration", JsonPrimitive(duration))
                    }
                )
            }
        }.getOrNull() ?: return false

        return response.status == HttpStatusCode.NoContent || response.status == HttpStatusCode.OK
    }

    private suspend fun addHabitToQuest(token: String, questId: String, habit: HabitListItem): Boolean {
        val response = runCatching {
            client.post("${edgeServiceBaseUrl()}/api/v1/quests/$questId/habits") {
                header(HttpHeaders.Authorization, "Bearer $token")
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                setBody(
                    buildJsonObject {
                        put("habitId", JsonPrimitive(habit.id))
                        put("title", JsonPrimitive(habit.title))
                        put("description", JsonPrimitive(habit.description))
                        put(
                            "tags",
                            buildJsonArray {
                                habit.tags.forEach { tag -> add(JsonPrimitive(tag)) }
                            }
                        )
                        put("recurrence", JsonNull)
                    }
                )
            }
        }.getOrNull() ?: return false

        return response.status == HttpStatusCode.NoContent || response.status == HttpStatusCode.OK
    }
}
