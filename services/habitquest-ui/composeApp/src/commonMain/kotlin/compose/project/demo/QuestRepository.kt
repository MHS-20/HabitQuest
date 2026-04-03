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
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.put

data class QuestData(
    val id: String,
    val name: String,
    val durationDays: Int,
    val reward: Int,
    val habitIds: List<String>,
)

data class QuestHabitProgressData(
    val habitId: String,
    val title: String,
    val requiredOccurrences: Int,
    val attendedOccurrences: Int,
    val remainingOccurrences: Int,
)

data class QuestProgressData(
    val questId: String,
    val questName: String,
    val status: String,
    val completionPercentage: Int,
    val habits: List<QuestHabitProgressData>,
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

sealed interface QuestProgressListResult {
    data class Success(val progress: List<QuestProgressData>) : QuestProgressListResult
    data class Error(val message: String) : QuestProgressListResult
}

sealed interface JoinQuestResult {
    data object Success : JoinQuestResult
    data class Error(val message: String) : JoinQuestResult
}

class QuestRepository {
    private val client = HttpClient(createHttpEngine()) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    suspend fun createQuest(token: String, name: String, durationDays: Int): CreateQuestResult {
        val response = runCatching {
            client.post("${edgeServiceBaseUrl()}/api/v1/quests") {
                header(HttpHeaders.Authorization, "Bearer $token")
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                setBody(
                    buildJsonObject {
                        put("name", JsonPrimitive(name))
                        put("durationDays", JsonPrimitive(durationDays))
                    }
                )
            }
        }.getOrElse {
            return CreateQuestResult.Error("Unable to contact quest-service")
        }

        return when (response.status) {
            HttpStatusCode.Created, HttpStatusCode.OK -> {
                val body = response.body<JsonObject>()
                val source = body["content"]?.jsonObject ?: body
                val id = source["id"]?.jsonPrimitive?.contentOrNull
                    ?: return CreateQuestResult.Error("Create quest response missing id")
                CreateQuestResult.Success(id)
            }

            HttpStatusCode.BadRequest -> CreateQuestResult.Error("Invalid quest data")
            HttpStatusCode.Unauthorized -> CreateQuestResult.Error("Session expired, please log in again")
            else -> CreateQuestResult.Error("Create quest error (${response.status.value})")
        }
    }

    suspend fun fetchQuestById(token: String, questId: String): QuestResult {
        val response = runCatching {
            client.get("${edgeServiceBaseUrl()}/api/v1/quests/$questId") {
                header(HttpHeaders.Authorization, "Bearer $token")
                header(HttpHeaders.Accept, ContentType.Application.Json)
            }
        }.getOrElse {
            return QuestResult.Error("Unable to contact quest-service")
        }

        return when (response.status) {
            HttpStatusCode.OK -> {
                val body = response.body<JsonObject>()
                val source = body["content"]?.jsonObject ?: body
                val quest = parseQuest(source) ?: return QuestResult.Error("Invalid quest response")
                QuestResult.Success(quest)
            }

            HttpStatusCode.NotFound -> QuestResult.Error("Quest not found")
            HttpStatusCode.Unauthorized -> QuestResult.Error("Session expired, please log in again")
            else -> QuestResult.Error("Quest read error (${response.status.value})")
        }
    }

    suspend fun fetchAllQuests(token: String): QuestListResult {
        val response = runCatching {
            client.get("${edgeServiceBaseUrl()}/api/v1/quests") {
                header(HttpHeaders.Authorization, "Bearer $token")
                header(HttpHeaders.Accept, ContentType.Application.Json)
            }
        }.getOrElse {
            return QuestListResult.Error("Unable to contact quest-service")
        }

        return when (response.status) {
            HttpStatusCode.OK -> {
                val body = response.body<JsonObject>()
                val quests = parseQuestCollection(body)
                QuestListResult.Success(quests)
            }

            HttpStatusCode.Unauthorized -> QuestListResult.Error("Session expired, please log in again")
            else -> QuestListResult.Error("Quest list read error (${response.status.value})")
        }
    }

    suspend fun fetchActiveProgressByAvatar(token: String, avatarId: String): QuestProgressListResult {
        val response = runCatching {
            client.get("${edgeServiceBaseUrl()}/api/v1/quests/progress/$avatarId") {
                header(HttpHeaders.Authorization, "Bearer $token")
                header(HttpHeaders.Accept, ContentType.Application.Json)
            }
        }.getOrElse {
            return QuestProgressListResult.Error("Unable to contact quest-service")
        }

        return when (response.status) {
            HttpStatusCode.OK -> {
                val body = response.body<JsonObject>()
                val source = body["content"]?.jsonObject ?: body
                val quests = source["quests"]?.jsonArray?.mapNotNull { parseProgress(it as? JsonObject) }.orEmpty()
                QuestProgressListResult.Success(quests)
            }

            HttpStatusCode.Unauthorized -> QuestProgressListResult.Error("Session expired, please log in again")
            else -> QuestProgressListResult.Error("Quest progress read error (${response.status.value})")
        }
    }

    suspend fun joinQuest(token: String, questId: String, avatarId: String): JoinQuestResult {
        val response = runCatching {
            client.post("${edgeServiceBaseUrl()}/api/v1/quests/$questId/join") {
                header(HttpHeaders.Authorization, "Bearer $token")
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                setBody(
                    buildJsonObject {
                        put("avatarId", JsonPrimitive(avatarId))
                    }
                )
            }
        }.getOrElse {
            return JoinQuestResult.Error("Unable to contact quest-service")
        }

        return when (response.status) {
            HttpStatusCode.NoContent, HttpStatusCode.OK -> JoinQuestResult.Success
            HttpStatusCode.NotFound -> JoinQuestResult.Error("Quest not found")
            HttpStatusCode.Unauthorized -> JoinQuestResult.Error("Session expired, please log in again")
            HttpStatusCode.BadRequest -> JoinQuestResult.Error("Invalid join request")
            else -> JoinQuestResult.Error("Join quest error (${response.status.value})")
        }
    }

    private fun parseQuest(source: JsonObject): QuestData? {
        val id = source["id"]?.jsonPrimitive?.contentOrNull ?: return null
        return QuestData(
            id = id,
            name = source["name"]?.jsonPrimitive?.contentOrNull
                ?: source["title"]?.jsonPrimitive?.contentOrNull
                ?: "",
            durationDays = parseDurationDays(source),
            reward = source["reward"]?.jsonPrimitive?.intOrNull ?: 0,
            habitIds = source["habitIds"]?.jsonArray?.mapNotNull { it.jsonPrimitive.contentOrNull }
                ?: emptyList(),
        )
    }

    private fun parseDurationDays(source: JsonObject): Int {
        source["durationDays"]?.jsonPrimitive?.intOrNull?.let { return it }

        val iso = source["duration"]?.jsonPrimitive?.contentOrNull ?: return 0
        if (iso.startsWith("P") && iso.endsWith("D")) {
            return iso.removePrefix("P").removeSuffix("D").toIntOrNull() ?: 0
        }
        if (iso.startsWith("PT") && iso.endsWith("H")) {
            val hours = iso.removePrefix("PT").removeSuffix("H").toIntOrNull() ?: return 0
            return hours / 24
        }
        return 0
    }

    private fun parseQuestCollection(body: JsonObject): List<QuestData> {
        val directArray = body["quests"] as? JsonArray
        if (directArray != null) {
            return parseQuestArray(directArray)
        }

        val embedded = body["_embedded"]?.jsonObject
        val embeddedList = embedded
            ?.values
            ?.flatMap { parseQuestElements(it) }
            .orEmpty()
        if (embeddedList.isNotEmpty()) {
            return embeddedList
        }

        val contentArray = body["content"] as? JsonArray
        if (contentArray != null) {
            return parseQuestArray(contentArray)
        }

        // Fallback for non-HAL responses that return a single quest object.
        return parseQuest(body)?.let(::listOf).orEmpty()
    }

    private fun parseQuestElements(element: JsonElement): List<QuestData> {
        val objectValue = element as? JsonObject
        if (objectValue != null) {
            val maybeContentArray = objectValue["content"] as? JsonArray
            if (maybeContentArray != null) {
                return parseQuestArray(maybeContentArray)
            }
        }

        val arrayValue = element as? JsonArray
        return if (arrayValue != null) parseQuestArray(arrayValue) else emptyList()
    }

    private fun parseQuestArray(array: JsonArray): List<QuestData> {
        return array.mapNotNull { element ->
            val item = element as? JsonObject ?: return@mapNotNull null
            val source = item["content"]?.jsonObject ?: item
            parseQuest(source)
        }
    }

    private fun parseProgress(source: JsonObject?): QuestProgressData? {
        if (source == null) return null
        val questId = source["questId"]?.jsonPrimitive?.contentOrNull ?: return null
        val questName = source["questName"]?.jsonPrimitive?.contentOrNull ?: questId
        val status = source["status"]?.jsonPrimitive?.contentOrNull ?: "IN_PROGRESS"
        val completion = source["completionPercentage"]?.jsonPrimitive?.intOrNull ?: 0
        val habits = source["habits"]?.jsonArray?.mapNotNull { habitElement ->
            val habit = habitElement as? JsonObject ?: return@mapNotNull null
            val habitId = habit["habitId"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
            QuestHabitProgressData(
                habitId = habitId,
                title = habit["title"]?.jsonPrimitive?.contentOrNull ?: habitId,
                requiredOccurrences = habit["requiredOccurrences"]?.jsonPrimitive?.intOrNull ?: 0,
                attendedOccurrences = habit["attendedOccurrences"]?.jsonPrimitive?.intOrNull ?: 0,
                remainingOccurrences = habit["remainingOccurrences"]?.jsonPrimitive?.intOrNull ?: 0,
            )
        }.orEmpty()

        return QuestProgressData(
            questId = questId,
            questName = questName,
            status = status,
            completionPercentage = completion,
            habits = habits,
        )
    }

    suspend fun createQuestWithDetails(
        token: String,
        name: String,
        durationDays: Int,
        habits: List<HabitListItem>,
    ): CreateQuestResult {
        val created = createQuest(token, name, durationDays)
        if (created is CreateQuestResult.Error) {
            return created
        }

        val questId = (created as CreateQuestResult.Success).questId

        for (habit in habits) {
            val habitAdded = addHabitToQuest(token, questId, habit)
            if (!habitAdded) {
                return CreateQuestResult.Error("Quest created but not all habits were added")
            }
        }

        return CreateQuestResult.Success(questId)
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
                        put(
                            "recurrence",
                            buildJsonObject {
                                put("type", JsonPrimitive(habit.recurrenceType.uppercase()))
                                put(
                                    "dayOfMonth",
                                    habit.recurrenceDayOfMonth?.let { JsonPrimitive(it) } ?: JsonNull
                                )
                                put(
                                    "dayOfWeek",
                                    habit.recurrenceDayOfWeek?.let { JsonPrimitive(it) } ?: JsonNull
                                )
                            }
                        )
                    }
                )
            }
        }.getOrNull() ?: return false

        return response.status == HttpStatusCode.NoContent || response.status == HttpStatusCode.OK
    }
}
