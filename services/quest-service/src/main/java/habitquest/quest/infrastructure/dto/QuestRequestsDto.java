package habitquest.quest.infrastructure.dto;

import java.util.List;

public class QuestRequestsDto {
  public record CreateQuestRequest(String name, Integer durationDays) {}

  public record UpdateNameRequest(String name) {}

  public record UpdateDurationRequest(Integer durationDays) {}

  public record UpdateRewardRequest(Integer experience, Integer money) {}

  public record RemoveHabitRequest(String habitId, String title) {}

  public record AddHabitRequest(
      String habitId,
      String title,
      String description,
      List<String> tags,
      RecurrenceRequest recurrence) {
    public AddHabitRequest {
      tags = tags != null ? List.copyOf(tags) : List.of();
    }
  }

  public record RecurrenceRequest(String type, Integer dayOfMonth, String dayOfWeek) {}

  public record RecordAttendanceRequest(String avatarId, String habitId, String attendedOn) {}

  public record JoinQuestRequest(String avatarId, String joinedOn) {}
}
