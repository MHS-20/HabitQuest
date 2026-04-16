package habitquest.quest.infrastructure.dto;

import common.cqrs.QueryResponse;
import java.time.LocalDate;
import java.util.List;

public class QuestQueries {

  public record RecurrenceResponse(String type, Integer dayOfMonth, String dayOfWeek)
      implements QueryResponse {}

  public record QuestResponse(
      String id, String name, Integer durationDays, Integer reward, List<String> habitIds)
      implements QueryResponse {
    public QuestResponse {
      habitIds = List.copyOf(habitIds);
    }
  }

  public record QuestCreatedResponse(String id) implements QueryResponse {}

  public record NameResponse(String name) implements QueryResponse {}

  public record DurationResponse(Long durationDays) implements QueryResponse {}

  public record ErrorResponse(String message) implements QueryResponse {}

  public record HabitsResponse(List<HabitResponse> habits) implements QueryResponse {
    public HabitsResponse {
      habits = habits != null ? List.copyOf(habits) : List.of();
    }
  }

  public record AvatarQuestProgressResponse(String avatarId, List<QuestProgressResponse> quests)
      implements QueryResponse {
    public AvatarQuestProgressResponse {
      quests = quests != null ? List.copyOf(quests) : List.of();
    }
  }

  public record QuestProgressResponse(
      String questId,
      String questName,
      String status,
      Integer completionPercentage,
      List<HabitProgressResponse> habits)
      implements QueryResponse {
    public QuestProgressResponse {
      habits = habits != null ? List.copyOf(habits) : List.of();
    }
  }

  public record HabitProgressResponse(
      String habitId,
      String title,
      Integer requiredOccurrences,
      Integer attendedOccurrences,
      Integer remainingOccurrences)
      implements QueryResponse {}

  public record HabitResponse(
      String id,
      String title,
      String description,
      List<String> tags,
      RecurrenceResponse recurrence,
      LocalDate nextRecurrenceDate,
      LocalDate lastAttendedDate)
      implements QueryResponse {

    public HabitResponse {
      tags = List.copyOf(tags);
    }
  }
}
