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
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

enum class HabitRecurrenceType {
    DAILY,
    WEEKLY,
    MONTHLY,
}

sealed interface HabitCreateResult {
    data class Success(val habitId: String) : HabitCreateResult
    data class Error(val message: String) : HabitCreateResult
}

data class HabitListItem(
    val id: String,
    val title: String,
    val description: String,
    val recurrenceType: String,
)

sealed interface HabitListResult {
    data class Success(val habits: List<HabitListItem>) : HabitListResult
    data class Error(val message: String) : HabitListResult
}

class HabitRepository {
    private val client = HttpClient(createHttpEngine()) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    suspend fun createHabit(
        token: String,
        avatarId: String,
        title: String,
        description: String,
        recurrenceType: HabitRecurrenceType,
        dayOfWeek: String?,
        dayOfMonth: Int?,
    ): HabitCreateResult {
        val response = runCatching {
            client.post("${edgeServiceBaseUrl()}/api/v1/habits") {
                header(HttpHeaders.Authorization, "Bearer $token")
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                setBody(
                    buildJsonObject {
                        put("avatarId", JsonPrimitive(avatarId))
                        put("title", JsonPrimitive(title))
                        put("description", JsonPrimitive(description))
                        put("recurrenceType", JsonPrimitive(recurrenceType.name))
                        if (dayOfWeek != null) {
                            put("dayOfWeek", JsonPrimitive(dayOfWeek))
                        }
                        if (dayOfMonth != null) {
                            put("dayOfMonth", JsonPrimitive(dayOfMonth))
                        }
                    }
                )
            }
        }.getOrElse {
            return HabitCreateResult.Error("Impossibile contattare habit-service")
        }

        return when (response.status) {
            HttpStatusCode.Created -> {
                val body = response.body<JsonObject>()
                val source = body["content"]?.jsonObject ?: body
                val id = source["id"]?.jsonPrimitive?.contentOrNull
                    ?: return HabitCreateResult.Error("Risposta create habit senza id")
                HabitCreateResult.Success(id)
            }

            HttpStatusCode.BadRequest -> {
                val body = response.body<JsonObject>()
                val message = body["message"]?.jsonPrimitive?.contentOrNull
                    ?: "Dati habit non validi"
                HabitCreateResult.Error(message)
            }

            HttpStatusCode.Unauthorized -> HabitCreateResult.Error("Sessione scaduta, rifai il login")
            else -> HabitCreateResult.Error("Errore create habit (${response.status.value})")
        }
    }

    suspend fun fetchHabitsByAvatar(token: String, avatarId: String): HabitListResult {
        val response = runCatching {
            client.get("${edgeServiceBaseUrl()}/api/v1/habits/avatar/$avatarId") {
                header(HttpHeaders.Authorization, "Bearer $token")
                header(HttpHeaders.Accept, ContentType.Application.Json)
            }
        }.getOrElse {
            return HabitListResult.Error("Impossibile contattare habit-service")
        }

        return when (response.status) {
            HttpStatusCode.OK -> {
                val items = response.body<JsonArray>().mapNotNull { element ->
                    val obj = element as? JsonObject ?: return@mapNotNull null
                    val recurrence = obj["recurrence"]?.jsonObject
                    HabitListItem(
                        id = obj["id"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null,
                        title = obj["title"]?.jsonPrimitive?.contentOrNull.orEmpty(),
                        description = obj["description"]?.jsonPrimitive?.contentOrNull.orEmpty(),
                        recurrenceType = recurrence?.get("type")?.jsonPrimitive?.contentOrNull
                            ?: "UNKNOWN"
                    )
                }
                HabitListResult.Success(items)
            }

            HttpStatusCode.Unauthorized -> HabitListResult.Error("Sessione scaduta, rifai il login")
            else -> HabitListResult.Error("Errore lettura habits (${response.status.value})")
        }
    }
}

