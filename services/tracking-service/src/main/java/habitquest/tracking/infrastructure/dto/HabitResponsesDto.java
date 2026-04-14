package habitquest.tracking.infrastructure.dto;

import java.time.LocalDateTime;
import java.util.List;

public class HabitResponsesDto {

  public record HabitCreatedResponse(String id) {}

  public record TitleResponse(String title) {}

  public record DescriptionResponse(String description) {}

  public record TagsResponse(List<String> tags) {}

  public record HistoryResponse(List<HabitHistoryEventResponse> history) {}

  public record LastAttendedDateResponse(LocalDateTime date) {}

  public record ErrorResponse(String message) {}

  public record RecurrenceResponse(String type, Integer dayOfMonth, String dayOfWeek) {}

  public record HabitHistoryEventResponse(
      String eventType,
      String habitId,
      String avatarId,
      LocalDateTime occurredAt,
      String details) {}

  public record HabitResponse(
      String id,
      String avatarId,
      String title,
      String description,
      List<String> tags,
      RecurrenceResponse recurrence,
      LocalDateTime lastAttendedDate,
      LocalDateTime nextRecurrenceDate,
      String associatedQuestId,
      String sourceHabitId) {

    public HabitResponse {
      tags = List.copyOf(tags);
    }
  }
}
