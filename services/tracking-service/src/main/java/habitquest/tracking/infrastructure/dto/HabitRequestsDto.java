package habitquest.tracking.infrastructure.dto;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;

public class HabitRequestsDto {
  public record CreateHabitRequest(
      String avatarId,
      String title,
      String description,
      String recurrenceType,
      DayOfWeek dayOfWeek,
      Integer dayOfMonth,
      List<String> tags,
      String associatedQuestId,
      String sourceHabitId) {}

  public record UpdateTitleRequest(String title) {}

  public record UpdateDescriptionRequest(String description) {}

  public record UpdateTagsRequest(List<String> tags) {
    public UpdateTagsRequest {
      tags = tags != null ? List.copyOf(tags) : List.of();
    }
  }

  public record UpdateRecurrenceRequest(String type, DayOfWeek dayOfWeek, Integer dayOfMonth) {}

  public record AttendRequest(LocalDateTime date) {}
}
