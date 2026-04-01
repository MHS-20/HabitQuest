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
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

enum class CreateHabitRecurrenceType {
    DAILY,
    WEEKLY,
    MONTHLY,
}

sealed interface CreateHabitResult {
    data class Success(val habitId: String) : CreateHabitResult
    data class Error(val message: String) : CreateHabitResult
}

data class HabitListItem(
    val id: String,
    val title: String,
    val description: String,
    val recurrenceType: String,
    val tags : List<String> = emptyList(),
    val lastAttendedDate: String? = null,
    val nextRecurrenceDate: String? = null,
)

sealed interface HabitListResult {
    data class Success(val habits: List<HabitListItem>) : HabitListResult
    data class Error(val message: String) : HabitListResult
}

sealed interface AttendHabitResult {
    data object Success : AttendHabitResult
    data class Error(val message: String) : AttendHabitResult
}

class HabitsApiRepository {
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
        recurrenceType: CreateHabitRecurrenceType,
        dayOfWeek: String?,
        dayOfMonth: Int?,
    ): CreateHabitResult {
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
            return CreateHabitResult.Error("Impossibile contattare habit-service")
        }

        return when (response.status) {
            HttpStatusCode.Created -> {
                val body = response.body<JsonObject>()
                val source = body["content"]?.jsonObject ?: body
                val id = source["id"]?.jsonPrimitive?.contentOrNull
                    ?: return CreateHabitResult.Error("Risposta create habit senza id")
                CreateHabitResult.Success(id)
            }

            HttpStatusCode.BadRequest -> {
                val body = response.body<JsonObject>()
                val message = body["message"]?.jsonPrimitive?.contentOrNull
                    ?: "Dati habit non validi"
                CreateHabitResult.Error(message)
            }

            HttpStatusCode.Unauthorized -> CreateHabitResult.Error("Sessione scaduta, rifai il login")
            else -> CreateHabitResult.Error("Errore create habit (${response.status.value})")
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
                        tags = obj["tags"]?.jsonArray?.mapNotNull { it.jsonPrimitive.contentOrNull } ?: emptyList(),
                        recurrenceType = recurrence?.get("type")?.jsonPrimitive?.contentOrNull
                            ?: "UNKNOWN",
                        lastAttendedDate = obj["lastAttendedDate"]?.jsonPrimitive?.contentOrNull,
                        nextRecurrenceDate = obj["nextRecurrenceDate"]?.jsonPrimitive?.contentOrNull,

                    )
                }
                HabitListResult.Success(items)
            }

            HttpStatusCode.Unauthorized -> HabitListResult.Error("Sessione scaduta, rifai il login")
            else -> HabitListResult.Error("Errore lettura habits (${response.status.value})")
        }
    }

    suspend fun attendHabit(token: String, habitId: String): AttendHabitResult {
        val now = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .toString()
        val response = runCatching {
            client.post("${edgeServiceBaseUrl()}/api/v1/habits/$habitId/attend") {
                header(HttpHeaders.Authorization, "Bearer $token")
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                setBody(
                    buildJsonObject {
                        put("date", JsonPrimitive(now))
                    }
                )
            }
        }.getOrElse {
            return AttendHabitResult.Error("Impossibile contattare habit-service")
        }

        return when (response.status) {
            HttpStatusCode.NoContent, HttpStatusCode.OK -> AttendHabitResult.Success
            HttpStatusCode.Unauthorized -> AttendHabitResult.Error("Sessione scaduta, rifai il login")
            HttpStatusCode.NotFound -> AttendHabitResult.Error("Habit non trovata")
            else -> AttendHabitResult.Error("Errore attend habit (${response.status.value})")
        }
    }
}
