package habitquest.quest.infrastructure.dto;

import java.time.LocalDate;
import java.util.List;

public class QuestResponsesDto {

  public record RecurrenceResponse(String type, Integer dayOfMonth, String dayOfWeek) {}

  public record QuestResponse(
      String id, String name, Integer durationDays, Integer reward, List<String> habitIds) {
    public QuestResponse {
      habitIds = List.copyOf(habitIds);
    }
  }

  public record QuestCreatedResponse(String id) {}

  public record NameResponse(String name) {}

  public record DurationResponse(Long durationDays) {}

  public record HabitsResponse(List<HabitResponse> habits) {
    public HabitsResponse {
      habits = habits != null ? List.copyOf(habits) : List.of();
    }
  }

  public record AvatarQuestProgressResponse(String avatarId, List<QuestProgressResponse> quests) {
    public AvatarQuestProgressResponse {
      quests = quests != null ? List.copyOf(quests) : List.of();
    }
  }

  public record QuestProgressResponse(
      String questId,
      String questName,
      String status,
      Integer completionPercentage,
      List<HabitProgressResponse> habits) {
    public QuestProgressResponse {
      habits = habits != null ? List.copyOf(habits) : List.of();
    }
  }

  public record HabitProgressResponse(
      String habitId,
      String title,
      Integer requiredOccurrences,
      Integer attendedOccurrences,
      Integer remainingOccurrences) {}

  public record ErrorResponse(String message) {}

  public record HabitResponse(
      String id,
      String title,
      String description,
      List<String> tags,
      RecurrenceResponse recurrence,
      LocalDate nextRecurrenceDate,
      LocalDate lastAttendedDate) {

    public HabitResponse {
      tags = List.copyOf(tags);
    }
  }
}
