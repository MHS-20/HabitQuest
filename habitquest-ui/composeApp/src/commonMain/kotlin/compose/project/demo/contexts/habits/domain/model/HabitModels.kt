package compose.project.demo.contexts.habits.domain.model

enum class CreateHabitRecurrenceType {
    DAILY,
    WEEKLY,
    MONTHLY,
}

sealed interface CreateHabitResult {
    data class Success(
        val habitId: String,
    ) : CreateHabitResult

    data class Error(
        val message: String,
    ) : CreateHabitResult
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
    data class Success(
        val habits: List<HabitListItem>,
    ) : HabitListResult

    data class Error(
        val message: String,
    ) : HabitListResult
}

sealed interface AttendHabitResult {
    data object Success : AttendHabitResult

    data class Error(
        val message: String,
    ) : AttendHabitResult
}

sealed interface DeleteHabitResult {
    data object Success : DeleteHabitResult

    data class Error(
        val message: String,
    ) : DeleteHabitResult
}

sealed interface UpdateHabitResult {
    data object Success : UpdateHabitResult

    data class Error(
        val message: String,
    ) : UpdateHabitResult
}

sealed interface HabitHistoryResult {
    data class Success(
        val items: List<HabitHistoryItem>,
    ) : HabitHistoryResult

    data class Error(
        val message: String,
    ) : HabitHistoryResult
}
