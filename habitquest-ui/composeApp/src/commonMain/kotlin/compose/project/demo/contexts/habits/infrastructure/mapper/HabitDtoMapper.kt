package compose.project.demo.contexts.habits.infrastructure.mapper

import compose.project.demo.contexts.habits.domain.model.CreateHabitRecurrenceType
import compose.project.demo.contexts.habits.domain.model.HabitHistoryItem
import compose.project.demo.contexts.habits.domain.model.HabitListItem
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

internal fun buildCreateHabitPayload(
  avatarId: String,
  title: String,
  description: String,
  recurrenceType: CreateHabitRecurrenceType,
  dayOfWeek: String?,
  dayOfMonth: Int?,
  tags: List<String>,
): JsonObject {
  return buildJsonObject {
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
}

internal fun buildRecurrencePatchPayload(
  recurrenceType: CreateHabitRecurrenceType,
  dayOfWeek: String?,
  dayOfMonth: Int?,
): JsonObject {
  return buildJsonObject {
    put("type", JsonPrimitive(recurrenceType.name))
    if (recurrenceType == CreateHabitRecurrenceType.WEEKLY && dayOfWeek != null) {
      put("dayOfWeek", JsonPrimitive(dayOfWeek))
    }
    if (recurrenceType == CreateHabitRecurrenceType.MONTHLY && dayOfMonth != null) {
      put("dayOfMonth", JsonPrimitive(dayOfMonth))
    }
  }
}

internal fun parseHabitList(payload: JsonArray): List<HabitListItem> {
  return payload.mapNotNull { element ->
    val obj = element as? JsonObject ?: return@mapNotNull null
    val recurrence = obj["recurrence"]?.jsonObject
    HabitListItem(
      id = obj["id"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null,
      title = obj["title"]?.jsonPrimitive?.contentOrNull.orEmpty(),
      description = obj["description"]?.jsonPrimitive?.contentOrNull.orEmpty(),
      tags = obj["tags"]?.jsonArray?.mapNotNull { it.jsonPrimitive.contentOrNull } ?: emptyList(),
      recurrenceType = recurrence?.get("type")?.jsonPrimitive?.contentOrNull ?: "UNKNOWN",
      recurrenceDayOfWeek = recurrence?.get("dayOfWeek")?.jsonPrimitive?.contentOrNull,
      recurrenceDayOfMonth = recurrence?.get("dayOfMonth")?.jsonPrimitive?.intOrNull,
      lastAttendedDate = obj["lastAttendedDate"]?.jsonPrimitive?.contentOrNull,
      nextRecurrenceDate = obj["nextRecurrenceDate"]?.jsonPrimitive?.contentOrNull,
      associatedQuestId = obj["associatedQuestId"]?.jsonPrimitive?.contentOrNull,
    )
  }
}

internal fun parseHabitHistory(payload: JsonArray): List<HabitHistoryItem> {
  return payload.mapNotNull { element ->
    val obj = element as? JsonObject ?: return@mapNotNull null
    HabitHistoryItem(
      eventType = obj["eventType"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null,
      habitId = obj["habitId"]?.jsonPrimitive?.contentOrNull ?: "",
      avatarId = obj["avatarId"]?.jsonPrimitive?.contentOrNull ?: "",
      occurredAt = obj["occurredAt"]?.jsonPrimitive?.contentOrNull ?: "",
      details = obj["details"]?.jsonPrimitive?.contentOrNull ?: "",
    )
  }
}
