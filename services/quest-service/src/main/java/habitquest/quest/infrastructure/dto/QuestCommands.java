package habitquest.quest.infrastructure.dto;

import common.cqrs.Command;
import java.util.List;

public class QuestCommands {
  public record CreateQuestCommand(String name, Integer durationDays) implements Command {}

  public record UpdateNameCommand(String name) implements Command {}

  public record UpdateDurationCommand(Integer durationDays) implements Command {}

  public record UpdateRewardCommand(Integer experience, Integer money) implements Command {}

  public record RemoveHabitCommand(String habitId, String title) implements Command {}

  public record RecurrenceCommand(String type, Integer dayOfMonth, String dayOfWeek)
      implements Command {}

  public record RecordAttendanceCommand(String avatarId, String habitId, String attendedOn)
      implements Command {}

  public record JoinQuestCommand(String avatarId, String joinedOn) implements Command {}

  public record AddHabitCommand(
      String habitId,
      String title,
      String description,
      List<String> tags,
      RecurrenceCommand recurrence)
      implements Command {
    public AddHabitCommand {
      tags = tags != null ? List.copyOf(tags) : List.of();
    }
  }
}
