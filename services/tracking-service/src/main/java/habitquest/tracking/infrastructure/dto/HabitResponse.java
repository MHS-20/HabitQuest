package habitquest.tracking.infrastructure.dto;

import habitquest.tracking.infrastructure.HabitController;
import java.time.LocalDateTime;
import java.util.List;

public record HabitResponse(
    String id,
    String avatarId,
    String title,
    String description,
    List<String> tags,
    HabitController.RecurrenceResponse recurrence,
    LocalDateTime lastAttendedDate,
    LocalDateTime nextRecurrenceDate,
    String associatedQuestId,
    String sourceHabitId) {

  public HabitResponse {
    tags = List.copyOf(tags);
  }
}
