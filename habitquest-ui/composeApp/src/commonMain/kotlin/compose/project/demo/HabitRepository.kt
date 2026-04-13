package compose.project.demo

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
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
  val recurrenceDayOfWeek: String? = null,
  val recurrenceDayOfMonth: Int? = null,
  val tags: List<String> = emptyList(),
  val lastAttendedDate: String? = null,
  val nextRecurrenceDate: String? = null,
  val associatedQuestId: String? = null,
)

data class HabitHistoryItem(
  val eventType: String,
  val habitId: String,
  val avatarId: String,
  val occurredAt: String,
  val details: String,
)

sealed interface HabitListResult {
  data class Success(val habits: List<HabitListItem>) : HabitListResult

  data class Error(val message: String) : HabitListResult
}

sealed interface AttendHabitResult {
  data object Success : AttendHabitResult

  data class Error(val message: String) : AttendHabitResult
}

sealed interface DeleteHabitResult {
  data object Success : DeleteHabitResult

  data class Error(val message: String) : DeleteHabitResult
}

sealed interface UpdateHabitResult {
  data object Success : UpdateHabitResult

  data class Error(val message: String) : UpdateHabitResult
}

sealed interface HabitHistoryResult {
  data class Success(val items: List<HabitHistoryItem>) : HabitHistoryResult

  data class Error(val message: String) : HabitHistoryResult
}

class HabitsApiRepository {
  private val client =
    HttpClient(createHttpEngine()) {
      install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    }

  suspend fun createHabit(
    token: String,
    avatarId: String,
    title: String,
    description: String,
    recurrenceType: CreateHabitRecurrenceType,
    dayOfWeek: String?,
    dayOfMonth: Int?,
    tags: List<String>,
  ): CreateHabitResult {
    val response =
      runCatching {
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
                put("tags", JsonArray(tags.map(::JsonPrimitive)))
              }
            )
          }
        }
        .getOrElse {
          return CreateHabitResult.Error("Unable to contact habit-service")
        }

    return when (response.status) {
      HttpStatusCode.Created -> {
        val body = response.body<JsonObject>()
        val source = body["content"]?.jsonObject ?: body
        val id =
          source["id"]?.jsonPrimitive?.contentOrNull
            ?: return CreateHabitResult.Error("Create habit response missing id")
        CreateHabitResult.Success(id)
      }

      HttpStatusCode.BadRequest -> {
        val body = response.body<JsonObject>()
        val message = body["message"]?.jsonPrimitive?.contentOrNull ?: "Invalid habit data"
        CreateHabitResult.Error(message)
      }

      HttpStatusCode.Unauthorized -> CreateHabitResult.Error("Session expired, please log in again")
      else -> CreateHabitResult.Error("Create habit error (${response.status.value})")
    }
  }

  suspend fun fetchHabitsByAvatar(token: String, avatarId: String): HabitListResult {
    val response =
      runCatching {
          client.get("${edgeServiceBaseUrl()}/api/v1/habits/avatar/$avatarId") {
            header(HttpHeaders.Authorization, "Bearer $token")
            header(HttpHeaders.Accept, ContentType.Application.Json)
          }
        }
        .getOrElse {
          return HabitListResult.Error("Unable to contact habit-service")
        }

    return when (response.status) {
      HttpStatusCode.OK -> {
        val items =
          response.body<JsonArray>().mapNotNull { element ->
            val obj = element as? JsonObject ?: return@mapNotNull null
            val recurrence = obj["recurrence"]?.jsonObject
            HabitListItem(
              id = obj["id"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null,
              title = obj["title"]?.jsonPrimitive?.contentOrNull.orEmpty(),
              description = obj["description"]?.jsonPrimitive?.contentOrNull.orEmpty(),
              tags =
                obj["tags"]?.jsonArray?.mapNotNull { it.jsonPrimitive.contentOrNull }
                  ?: emptyList(),
              recurrenceType = recurrence?.get("type")?.jsonPrimitive?.contentOrNull ?: "UNKNOWN",
              recurrenceDayOfWeek = recurrence?.get("dayOfWeek")?.jsonPrimitive?.contentOrNull,
              recurrenceDayOfMonth = recurrence?.get("dayOfMonth")?.jsonPrimitive?.intOrNull,
              lastAttendedDate = obj["lastAttendedDate"]?.jsonPrimitive?.contentOrNull,
              nextRecurrenceDate = obj["nextRecurrenceDate"]?.jsonPrimitive?.contentOrNull,
              associatedQuestId = obj["associatedQuestId"]?.jsonPrimitive?.contentOrNull,
            )
          }
        HabitListResult.Success(items)
      }

      HttpStatusCode.Unauthorized -> HabitListResult.Error("Session expired, please log in again")
      else -> HabitListResult.Error("Habit read error (${response.status.value})")
    }
  }

  suspend fun attendHabit(token: String, habitId: String): AttendHabitResult {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).toString()
    val response =
      runCatching {
          client.post("${edgeServiceBaseUrl()}/api/v1/habits/$habitId/attend") {
            header(HttpHeaders.Authorization, "Bearer $token")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(buildJsonObject { put("date", JsonPrimitive(now)) })
          }
        }
        .getOrElse {
          return AttendHabitResult.Error("Unable to contact habit-service")
        }

    return when (response.status) {
      HttpStatusCode.NoContent,
      HttpStatusCode.OK -> AttendHabitResult.Success
      HttpStatusCode.Unauthorized -> AttendHabitResult.Error("Session expired, please log in again")
      HttpStatusCode.NotFound -> AttendHabitResult.Error("Habit not found")
      else -> AttendHabitResult.Error("Attend habit error (${response.status.value})")
    }
  }

  suspend fun deleteHabit(token: String, habitId: String): DeleteHabitResult {
    val response =
      runCatching {
          client.delete("${edgeServiceBaseUrl()}/api/v1/habits/$habitId") {
            header(HttpHeaders.Authorization, "Bearer $token")
          }
        }
        .getOrElse {
          return DeleteHabitResult.Error("Unable to contact habit-service")
        }

    return when (response.status) {
      HttpStatusCode.NoContent,
      HttpStatusCode.OK -> DeleteHabitResult.Success
      HttpStatusCode.Unauthorized -> DeleteHabitResult.Error("Session expired, please log in again")
      HttpStatusCode.NotFound -> DeleteHabitResult.Error("Habit not found")
      else -> DeleteHabitResult.Error("Delete habit error (${response.status.value})")
    }
  }

  suspend fun updateHabit(
    token: String,
    habitId: String,
    title: String,
    description: String,
    recurrenceType: CreateHabitRecurrenceType,
    dayOfWeek: String?,
    dayOfMonth: Int?,
  ): UpdateHabitResult {
    if (habitId.isBlank()) {
      return UpdateHabitResult.Error("Habit not found")
    }

    val titleResult =
      patchHabitField(
        token = token,
        habitId = habitId,
        endpoint = "title",
        payloadKey = "title",
        payloadValue = title,
        errorPrefix = "Update title",
      )
    if (titleResult != null) {
      return UpdateHabitResult.Error(titleResult)
    }

    val descriptionResult =
      patchHabitField(
        token = token,
        habitId = habitId,
        endpoint = "description",
        payloadKey = "description",
        payloadValue = description,
        errorPrefix = "Update description",
      )
    if (descriptionResult != null) {
      return UpdateHabitResult.Error(descriptionResult)
    }

    val recurrenceResult =
      patchHabitRecurrence(
        token = token,
        habitId = habitId,
        recurrenceType = recurrenceType,
        dayOfWeek = dayOfWeek,
        dayOfMonth = dayOfMonth,
      )
    if (recurrenceResult != null) {
      return UpdateHabitResult.Error(recurrenceResult)
    }

    return UpdateHabitResult.Success
  }

  suspend fun fetchHistoryByAvatar(token: String, avatarId: String): HabitHistoryResult {
    val response =
      runCatching {
          client.get("${edgeServiceBaseUrl()}/api/v1/habits/avatar/$avatarId/history") {
            header(HttpHeaders.Authorization, "Bearer $token")
            header(HttpHeaders.Accept, ContentType.Application.Json)
          }
        }
        .getOrElse {
          return HabitHistoryResult.Error("Unable to contact habit-service")
        }

    return when (response.status) {
      HttpStatusCode.OK -> {
        val items =
          response.body<JsonArray>().mapNotNull { element ->
            val obj = element as? JsonObject ?: return@mapNotNull null
            HabitHistoryItem(
              eventType = obj["eventType"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null,
              habitId = obj["habitId"]?.jsonPrimitive?.contentOrNull ?: "",
              avatarId = obj["avatarId"]?.jsonPrimitive?.contentOrNull ?: "",
              occurredAt = obj["occurredAt"]?.jsonPrimitive?.contentOrNull ?: "",
              details = obj["details"]?.jsonPrimitive?.contentOrNull ?: "",
            )
          }
        HabitHistoryResult.Success(items)
      }

      HttpStatusCode.Unauthorized ->
        HabitHistoryResult.Error("Session expired, please log in again")
      else -> HabitHistoryResult.Error("Habit history read error (${response.status.value})")
    }
  }

  private suspend fun patchHabitField(
    token: String,
    habitId: String,
    endpoint: String,
    payloadKey: String,
    payloadValue: String,
    errorPrefix: String,
  ): String? {
    val response =
      runCatching {
          client.patch("${edgeServiceBaseUrl()}/api/v1/habits/$habitId/$endpoint") {
            header(HttpHeaders.Authorization, "Bearer $token")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(buildJsonObject { put(payloadKey, JsonPrimitive(payloadValue)) })
          }
        }
        .getOrElse {
          return "Unable to contact habit-service"
        }

    return when (response.status) {
      HttpStatusCode.NoContent,
      HttpStatusCode.OK -> null
      HttpStatusCode.Unauthorized -> "Session expired, please log in again"
      HttpStatusCode.NotFound -> "Habit not found"
      HttpStatusCode.BadRequest -> {
        val body = runCatching { response.body<JsonObject>() }.getOrNull()
        body?.get("message")?.jsonPrimitive?.contentOrNull ?: "$errorPrefix failed"
      }

      else -> "$errorPrefix failed (${response.status.value})"
    }
  }

  private suspend fun patchHabitRecurrence(
    token: String,
    habitId: String,
    recurrenceType: CreateHabitRecurrenceType,
    dayOfWeek: String?,
    dayOfMonth: Int?,
  ): String? {
    val response =
      runCatching {
          client.patch("${edgeServiceBaseUrl()}/api/v1/habits/$habitId/recurrence") {
            header(HttpHeaders.Authorization, "Bearer $token")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(
              buildJsonObject {
                put("type", JsonPrimitive(recurrenceType.name))
                if (recurrenceType == CreateHabitRecurrenceType.WEEKLY && dayOfWeek != null) {
                  put("dayOfWeek", JsonPrimitive(dayOfWeek))
                }
                if (recurrenceType == CreateHabitRecurrenceType.MONTHLY && dayOfMonth != null) {
                  put("dayOfMonth", JsonPrimitive(dayOfMonth))
                }
              }
            )
          }
        }
        .getOrElse {
          return "Unable to contact habit-service"
        }

    return when (response.status) {
      HttpStatusCode.NoContent,
      HttpStatusCode.OK -> null
      HttpStatusCode.Unauthorized -> "Session expired, please log in again"
      HttpStatusCode.NotFound -> "Habit not found"
      HttpStatusCode.BadRequest -> {
        val body = runCatching { response.body<JsonObject>() }.getOrNull()
        body?.get("message")?.jsonPrimitive?.contentOrNull ?: "Update recurrence failed"
      }

      else -> "Update recurrence failed (${response.status.value})"
    }
  }
}
