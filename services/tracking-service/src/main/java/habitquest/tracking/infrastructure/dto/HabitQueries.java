package habitquest.tracking.infrastructure.dto;

import common.cqrs.QueryResponse;
import java.time.LocalDateTime;
import java.util.List;

public class HabitQueries {

  public record HabitCreatedResponse(String id) implements QueryResponse {}

  public record TitleResponse(String title) implements QueryResponse {}

  public record DescriptionResponse(String description) implements QueryResponse {}

  public record TagsResponse(List<String> tags) implements QueryResponse {}

  public record HistoryResponse(List<HabitHistoryEventResponse> history) implements QueryResponse {}

  public record LastAttendedDateResponse(LocalDateTime date) implements QueryResponse {}

  public record ErrorResponse(String message) implements QueryResponse {}

  public record RecurrenceResponse(String type, Integer dayOfMonth, String dayOfWeek)
      implements QueryResponse {}

  public record HabitHistoryEventResponse(
      String eventType, String habitId, String avatarId, LocalDateTime occurredAt, String details)
      implements QueryResponse {}

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
      String sourceHabitId)
      implements QueryResponse {

    public HabitResponse {
      tags = List.copyOf(tags);
    }
  }
}
