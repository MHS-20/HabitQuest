package compose.project.demo

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
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
}

