package habitquest.tracking.application.port.in;

import common.ddd.Id;
import habitquest.tracking.application.exceptions.HabitNotFoundException;
import habitquest.tracking.domain.Avatar;
import habitquest.tracking.domain.Habit;
import habitquest.tracking.domain.Tag;
import habitquest.tracking.domain.reminder.Recurrence;
import java.time.LocalDateTime;
import java.util.List;

public interface HabitCommandService {

  Habit createHabit(
      Id<Avatar> avatarId,
      String title,
      String description,
      Recurrence recurrence,
      String associatedQuestId,
      String sourceHabitId);

  Habit updateTitle(Id<Habit> habitId, String title) throws HabitNotFoundException;

  Habit updateDescription(Id<Habit> habitId, String description) throws HabitNotFoundException;

  Habit updateTags(Id<Habit> habitId, List<Tag> tags) throws HabitNotFoundException;

  Habit updateRecurrence(Id<Habit> habitId, Recurrence recurrence) throws HabitNotFoundException;

  Habit attendHabit(Id<Habit> habitId, LocalDateTime date) throws HabitNotFoundException;

  void deleteHabitById(Id<Habit> habitId) throws HabitNotFoundException;

  void detectOverdueHabits();
}
