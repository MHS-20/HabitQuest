package habitquest.tracking.domain.factory;

import common.ddd.Factory;
import habitquest.tracking.domain.Habit;
import habitquest.tracking.domain.reminder.DailyRecurrence;
import habitquest.tracking.domain.reminder.MonthlyRecurrence;
import habitquest.tracking.domain.reminder.WeeklyRecurrence;
import java.time.DayOfWeek;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class HabitFactory implements Factory {
  public Habit createDailyHabit(String avatarId, String title, String description) {
    return new Habit(
        new UUIDGenerator().nextId(),
        avatarId,
        title,
        description,
        new DailyRecurrence(),
        Optional.empty());
  }

  public Habit createWeeklyHabit(
      String avatarId, String title, String description, DayOfWeek dayOfWeek) {
    return new Habit(
        new UUIDGenerator().nextId(),
        avatarId,
        title,
        description,
        new WeeklyRecurrence(dayOfWeek),
        Optional.empty());
  }

  public Habit createMonthlyHabit(
      String avatarId, String title, String description, Integer dayOfMonth) {
    return new Habit(
        new UUIDGenerator().nextId(),
        avatarId,
        title,
        description,
        new MonthlyRecurrence(dayOfMonth),
        Optional.empty());
  }
}
