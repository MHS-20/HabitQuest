package habitquest.quest.infrastructure.dto;

import java.time.LocalDate;
import java.util.List;

public record HabitResponse(
    String id,
    String title,
    String description,
    List<String> tags,
    RecurrenceResponse recurrence,
    LocalDate nextRecurrenceDate,
    LocalDate lastAttendedDate) {}
