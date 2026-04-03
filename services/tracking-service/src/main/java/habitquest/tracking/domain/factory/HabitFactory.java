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

  public Habit createDailyHabit(Id<Avatar> avatarId, String title, String description) {
    return createDailyHabit(avatarId, title, description, Optional.empty());
  }

  public Habit createDailyHabit(
      Id<Avatar> avatarId, String title, String description, Optional<String> associatedQuestId) {
    return new Habit(
        new Id<>(this.idGenerator.nextId()),
        avatarId,
        title,
        description,
        new DailyRecurrence(),
        associatedQuestId);
  }

  public Habit createWeeklyHabit(
      Id<Avatar> avatarId, String title, String description, DayOfWeek dayOfWeek) {
    return createWeeklyHabit(avatarId, title, description, dayOfWeek, Optional.empty());
  }

  public Habit createWeeklyHabit(
      Id<Avatar> avatarId,
      String title,
      String description,
      DayOfWeek dayOfWeek,
      Optional<String> associatedQuestId) {
    return new Habit(
        new Id<>(this.idGenerator.nextId()),
        avatarId,
        title,
        description,
        new WeeklyRecurrence(dayOfWeek),
        associatedQuestId);
  }

  public Habit createMonthlyHabit(
      Id<Avatar> avatarId, String title, String description, Integer dayOfMonth) {
    return createMonthlyHabit(avatarId, title, description, dayOfMonth, Optional.empty());
  }

  public Habit createMonthlyHabit(
      Id<Avatar> avatarId,
      String title,
      String description,
      Integer dayOfMonth,
      Optional<String> associatedQuestId) {
    return new Habit(
        new Id<>(this.idGenerator.nextId()),
        avatarId,
        title,
        description,
        new MonthlyRecurrence(dayOfMonth),
        associatedQuestId);
  }
}
