package habitquest.tracking.application;

import common.hexagonal.InBoundPort;
import habitquest.tracking.domain.Habit;
import habitquest.tracking.domain.Tag;
import habitquest.tracking.domain.events.HabitHistoryEvent;
import habitquest.tracking.domain.reminder.Recurrence;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;

@InBoundPort
public interface HabitService {
  Habit createDailyHabit(String avatarId, String title, String description);

  Habit createWeeklyHabit(String avatarId, String title, String description, DayOfWeek dayOfWeek);

  Habit createMonthlyHabit(String avatarId, String title, String description, Integer dayOfMonth);

  Habit getHabitById(String habitId) throws HabitNotFoundException;

  void deleteHabitById(String habitId) throws HabitNotFoundException;

  // region getters
  String getTitle(String habitId) throws HabitNotFoundException;

  String getDescription(String habitId) throws HabitNotFoundException;

  List<Tag> getTags(String habitId) throws HabitNotFoundException;

  Recurrence getRecurrence(String habitId) throws HabitNotFoundException;

  LocalDateTime getLastAttendedDate(String habitId) throws HabitNotFoundException;

  List<HabitHistoryEvent> getHistory(String habitId);

  // endregion

  // region updaters
  Habit updateTitle(String habitId, String title) throws HabitNotFoundException;

  Habit updateDescription(String habitId, String description) throws HabitNotFoundException;

  Habit updateTags(String habitId, List<Tag> tags) throws HabitNotFoundException;

  Habit updateRecurrence(String habitId, Recurrence recurrence) throws HabitNotFoundException;

  Habit attendHabit(String habitId, LocalDateTime date) throws HabitNotFoundException;

  void detectOverdueHabits();
  // endregion

}
