package habitquest.tracking.domain.factory;

import common.ddd.Factory;
import habitquest.tracking.domain.Habit;
import habitquest.tracking.domain.reminder.DailyRecurrence;
import habitquest.tracking.domain.reminder.MonthlyRecurrence;
import habitquest.tracking.domain.reminder.WeeklyRecurrence;
import java.time.DayOfWeek;

public class HabitFactory implements Factory {
  public static Habit createDailyHabit() {
    return new Habit(
        new UUIDGenerator().nextId(),
        "Daily Habit",
        "This is a daily habit",
        "DAILY",
        new DailyRecurrence());
  }

  public static Habit createWeeklyHabit(DayOfWeek dayOfWeek) {
    return new Habit(
        new UUIDGenerator().nextId(),
        "Weekly Habit",
        "This is a weekly habit",
        "WEEKLY",
        new WeeklyRecurrence(dayOfWeek));
  }

  public static Habit createMonthlyHabit(Integer dayOfMonth) {
    return new Habit(
        new UUIDGenerator().nextId(),
        "Daily Habit",
        "This is a daily habit",
        "DAILY",
        new MonthlyRecurrence(dayOfMonth));
  }
}
