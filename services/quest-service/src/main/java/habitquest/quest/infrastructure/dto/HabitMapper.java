package habitquest.quest.infrastructure.dto;

import common.ddd.Id;
import habitquest.quest.domain.Habit;
import habitquest.quest.domain.Tag;
import habitquest.quest.domain.reminder.DailyRecurrence;
import habitquest.quest.domain.reminder.MonthlyRecurrence;
import habitquest.quest.domain.reminder.Recurrence;
import habitquest.quest.domain.reminder.WeeklyRecurrence;
import habitquest.quest.infrastructure.dto.QuestRequestsDto.*;
import habitquest.quest.infrastructure.dto.QuestResponsesDto.*;
import java.time.DayOfWeek;
import java.util.List;

public class HabitMapper {

  private HabitMapper() {}

  public static HabitResponse toResponse(Habit habit) {
    return new HabitResponse(
        habit.getId().value(),
        habit.getTitle(),
        habit.getDescription(),
        habit.getTags() != null ? habit.getTags().stream().map(Tag::name).toList() : List.of(),
        toRecurrenceResponse(habit.getRecurrence()),
        habit.nextRecurrence(),
        habit.getLastAttendedDate());
  }

  private static RecurrenceResponse toRecurrenceResponse(Recurrence recurrence) {
    if (recurrence == null) {
      return null;
    }

    return switch (recurrence) {
      case DailyRecurrence r -> new RecurrenceResponse("DAILY", null, null);
      case WeeklyRecurrence r -> new RecurrenceResponse("WEEKLY", null, r.dayOfWeek().name());
      case MonthlyRecurrence r -> new RecurrenceResponse("MONTHLY", r.dayOfMonth(), null);
    };
  }

  public static Habit toDomain(String habitId, AddHabitRequest request) {
    return new Habit(
        new Id<>(habitId),
        request.title(),
        request.description(),
        toTags(request.tags()),
        toRecurrence(request.recurrence()));
  }

  private static List<Tag> toTags(List<String> tags) {
    if (tags == null) {
      return List.of();
    }
    return tags.stream().map(Tag::new).toList();
  }

  private static Recurrence toRecurrence(RecurrenceRequest request) {
    if (request == null) {
      return null;
    }
    return switch (request.type()) {
      case "DAILY" -> new DailyRecurrence();
      case "WEEKLY" -> new WeeklyRecurrence(DayOfWeek.valueOf(request.dayOfWeek()));
      case "MONTHLY" -> new MonthlyRecurrence(request.dayOfMonth());
      default -> throw new IllegalArgumentException("Unknown recurrence type: " + request.type());
    };
  }
}
