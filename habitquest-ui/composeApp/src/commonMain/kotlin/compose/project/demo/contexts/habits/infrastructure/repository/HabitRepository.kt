package compose.project.demo.contexts.habits.infrastructure.repository

import compose.project.demo.contexts.habits.domain.contract.HabitsGateway
import compose.project.demo.contexts.habits.domain.model.AttendHabitResult
import compose.project.demo.contexts.habits.domain.model.CreateHabitRecurrenceType
import compose.project.demo.contexts.habits.domain.model.CreateHabitResult
import compose.project.demo.contexts.habits.domain.model.DeleteHabitResult
import compose.project.demo.contexts.habits.domain.model.HabitHistoryResult
import compose.project.demo.contexts.habits.domain.model.HabitListResult
import compose.project.demo.contexts.habits.domain.model.UpdateHabitResult
import compose.project.demo.contexts.habits.infrastructure.mapper.buildCreateHabitPayload
import compose.project.demo.contexts.habits.infrastructure.mapper.buildRecurrencePatchPayload
import compose.project.demo.contexts.habits.infrastructure.mapper.parseHabitHistory
import compose.project.demo.contexts.habits.infrastructure.mapper.parseHabitList
import compose.project.demo.createHttpEngine
import compose.project.demo.edgeServiceBaseUrl
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
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class HabitsApiRepository : HabitsGateway {
  private val client =
    HttpClient(createHttpEngine()) {
      install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    }

  override suspend fun createHabit(
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
              buildCreateHabitPayload(
                avatarId = avatarId,
                title = title,
                description = description,
                recurrenceType = recurrenceType,
                dayOfWeek = dayOfWeek,
                dayOfMonth = dayOfMonth,
                tags = tags,
              )
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

  override suspend fun fetchHabitsByAvatar(token: String, avatarId: String): HabitListResult {
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
        val items = parseHabitList(response.body<JsonArray>())
        HabitListResult.Success(items)
      }

      HttpStatusCode.Unauthorized -> HabitListResult.Error("Session expired, please log in again")
      else -> HabitListResult.Error("Habit read error (${response.status.value})")
    }
  }

  override suspend fun attendHabit(token: String, habitId: String): AttendHabitResult {
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

  override suspend fun deleteHabit(token: String, habitId: String): DeleteHabitResult {
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

  override suspend fun updateHabit(
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

  override suspend fun fetchHistoryByAvatar(token: String, avatarId: String): HabitHistoryResult {
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
        val items = parseHabitHistory(response.body<JsonArray>())
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
            setBody(buildRecurrencePatchPayload(recurrenceType, dayOfWeek, dayOfMonth))
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
