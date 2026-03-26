package habitquest.tracking.application;

import common.ddd.Id;
import common.hexagonal.InBoundPort;
import habitquest.tracking.domain.Avatar;
import habitquest.tracking.domain.Habit;
import habitquest.tracking.domain.Tag;
import habitquest.tracking.domain.events.HabitHistoryEvent;
import habitquest.tracking.domain.reminder.Recurrence;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;

@InBoundPort
public interface HabitService {
  Habit createDailyHabit(Id<Avatar> avatarId, String title, String description);

  Habit createWeeklyHabit(
      Id<Avatar> avatarId, String title, String description, DayOfWeek dayOfWeek);

  Habit createMonthlyHabit(
      Id<Avatar> avatarId, String title, String description, Integer dayOfMonth);

  Habit getHabitById(Id<Habit> habitId) throws HabitNotFoundException;

  List<Habit> getHabitsByAvatarId(Id<Avatar> avatarId);

  void deleteHabitById(Id<Habit> habitId) throws HabitNotFoundException;

  // region getters
  String getTitle(Id<Habit> habitId) throws HabitNotFoundException;

  String getDescription(Id<Habit> habitId) throws HabitNotFoundException;

  List<Tag> getTags(Id<Habit> habitId) throws HabitNotFoundException;

  Recurrence getRecurrence(Id<Habit> habitId) throws HabitNotFoundException;

  LocalDateTime getLastAttendedDate(Id<Habit> habitId) throws HabitNotFoundException;

  List<HabitHistoryEvent> getHistory(Id<Habit> habitId);

  // endregion

  // region updaters
  Habit updateTitle(Id<Habit> habitId, String title) throws HabitNotFoundException;

  Habit updateDescription(Id<Habit> habitId, String description) throws HabitNotFoundException;

  Habit updateTags(Id<Habit> habitId, List<Tag> tags) throws HabitNotFoundException;

  Habit updateRecurrence(Id<Habit> habitId, Recurrence recurrence) throws HabitNotFoundException;

  Habit attendHabit(Id<Habit> habitId, LocalDateTime date) throws HabitNotFoundException;

  void detectOverdueHabits();
  // endregion

}
