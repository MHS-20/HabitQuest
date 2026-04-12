package habitquest.tracking.domain.factory;

import common.ddd.Factory;
import common.ddd.Id;
import habitquest.tracking.domain.Avatar;
import habitquest.tracking.domain.Habit;
import habitquest.tracking.domain.reminder.DailyRecurrence;
import habitquest.tracking.domain.reminder.MonthlyRecurrence;
import habitquest.tracking.domain.reminder.WeeklyRecurrence;
import java.time.DayOfWeek;
import java.util.Optional;

public class HabitFactory implements Factory {

  private final IdGenerator idGenerator;

  public HabitFactory(IdGenerator idGenerator) {
    this.idGenerator = idGenerator;
  }

  public Habit createDailyHabit(
      Id<Avatar> avatarId,
      String title,
      String description,
      String associatedQuestId,
      String sourceHabitId) {
    return new Habit(
        new Id<>(this.idGenerator.nextId()),
        avatarId,
        title,
        description,
        new DailyRecurrence(),
        Optional.ofNullable(associatedQuestId),
        Optional.ofNullable(sourceHabitId));
  }

  public Habit createWeeklyHabit(
      Id<Avatar> avatarId,
      String title,
      String description,
      DayOfWeek dayOfWeek,
      String associatedQuestId,
      String sourceHabitId) {
    return new Habit(
        new Id<>(this.idGenerator.nextId()),
        avatarId,
        title,
        description,
        new WeeklyRecurrence(dayOfWeek),
        Optional.ofNullable(associatedQuestId),
        Optional.ofNullable(sourceHabitId));
  }

  public Habit createMonthlyHabit(
      Id<Avatar> avatarId,
      String title,
      String description,
      Integer dayOfMonth,
      String associatedQuestId,
      String sourceHabitId) {
    return new Habit(
        new Id<>(this.idGenerator.nextId()),
        avatarId,
        title,
        description,
        new MonthlyRecurrence(dayOfMonth),
        Optional.ofNullable(associatedQuestId),
        Optional.ofNullable(sourceHabitId));
  }
}
