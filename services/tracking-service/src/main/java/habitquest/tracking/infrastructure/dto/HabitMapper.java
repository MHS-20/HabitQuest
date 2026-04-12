package habitquest.tracking.infrastructure.dto;

import habitquest.tracking.domain.Habit;
import habitquest.tracking.domain.Tag;
import habitquest.tracking.domain.events.HabitHistoryEvent;
import habitquest.tracking.domain.reminder.DailyRecurrence;
import habitquest.tracking.domain.reminder.MonthlyRecurrence;
import habitquest.tracking.domain.reminder.Recurrence;
import habitquest.tracking.domain.reminder.WeeklyRecurrence;
import habitquest.tracking.infrastructure.HabitController.RecurrenceResponse;

public class HabitMapper {

  private HabitMapper() {}

  public static HabitResponse toResponse(Habit habit) {
    return new HabitResponse(
        habit.getId().value(),
        habit.getAvatarId().value(),
        habit.getTitle(),
        habit.getDescription(),
        habit.getTags().stream().map(Tag::name).toList(),
        toRecurrenceResponse(habit.getRecurrence()),
        habit.getLastAttendedDate(),
        habit.nextRecurrence(),
        habit.getAssociatedQuestId().orElse(null),
        habit.getSourceHabitId().orElse(null));
  }

  public static HabitHistoryEventResponse toResponse(HabitHistoryEvent event) {
    return new HabitHistoryEventResponse(
        event.event().getClass().getSimpleName(),
        event.habitId().value(),
        event.avatarId().value(),
        event.occurredAt(),
        event.details());
  }

  public static RecurrenceResponse toRecurrenceResponse(Recurrence recurrence) {
    if (recurrence == null) {
      return null;
    }
    return switch (recurrence) {
      case DailyRecurrence r -> new RecurrenceResponse("DAILY", null, null);
      case WeeklyRecurrence r -> new RecurrenceResponse("WEEKLY", null, r.dayOfWeek().name());
      case MonthlyRecurrence r -> new RecurrenceResponse("MONTHLY", r.dayOfMonth(), null);
      default ->
          throw new IllegalArgumentException("Unknown recurrence type: " + recurrence.getClass());
    };
  }
}
