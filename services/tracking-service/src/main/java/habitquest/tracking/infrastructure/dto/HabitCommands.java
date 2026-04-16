package habitquest.tracking.infrastructure.dto;

import common.cqrs.Command;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;

public class HabitCommands {
  public record CreateHabitCommand(
      String avatarId,
      String title,
      String description,
      String recurrenceType,
      DayOfWeek dayOfWeek,
      Integer dayOfMonth,
      List<String> tags,
      String associatedQuestId,
      String sourceHabitId)
      implements Command {}

  public record UpdateTitleCommand(String title) implements Command {}

  public record UpdateDescriptionCommand(String description) implements Command {}

  public record UpdateTagsCommand(List<String> tags) implements Command {
    public UpdateTagsCommand {
      tags = tags != null ? List.copyOf(tags) : List.of();
    }
  }

  public record UpdateRecurrenceCommand(String type, DayOfWeek dayOfWeek, Integer dayOfMonth)
      implements Command {}

  public record AttendCommand(LocalDateTime date) implements Command {}
}
