package habitquest.tracking;

import common.ddd.Id;
import habitquest.tracking.domain.Avatar;
import habitquest.tracking.domain.Habit;
import habitquest.tracking.domain.Tag;
import habitquest.tracking.domain.reminder.DailyRecurrence;
import habitquest.tracking.domain.reminder.MonthlyRecurrence;
import habitquest.tracking.domain.reminder.WeeklyRecurrence;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public final class HabitFixtures {
  public static final String HABIT_1 = "habit-1";
  public static final String HABIT_OVERDUE = "habit-overdue";
  public static final String HABIT_NULL = "habit-null";
  public static final String HABIT_OK = "habit-ok";
  public static final String UNKNOWN_HABIT = "missing";
  public static final String GHOST_HABIT = "ghost-99";
  public static final String AVATAR_1 = "avatar-1";
  public static final String QUEST_1 = "quest-1";

  public static final String TITLE = "Hydrate";
  public static final String DESCRIPTION = "Drink 2L of water";

  public static final String TAG_HEALTH = "health";
  public static final String TAG_WELLNESS = "wellness";
  public static final String TAG_MINDSET = "mindset";

  // Default values
  public static final int DEFAULT_DAY_OF_MONTH = 15;
  public static final DayOfWeek DEFAULT_DAY_OF_WEEK = DayOfWeek.MONDAY;

  // Typed IDs
  public static final Id<Habit> HABIT_ID = new Id<>(HABIT_1);
  public static final Id<Habit> OVERDUE_HABIT_ID = new Id<>(HABIT_OVERDUE);
  public static final Id<Habit> UNKNOWN_ID = new Id<>(UNKNOWN_HABIT);
  public static final Id<Habit> GHOST_ID = new Id<>(GHOST_HABIT);
  public static final Id<Avatar> AVATAR_ID = new Id<>(AVATAR_1);

  // Notifier
  public static final String FIELD_HABIT_ID = "habitId";
  public static final String FIELD_AVATAR_ID = "avatarId";
  public static final String FIELD_OCCURRED_ON = "occurredOn";
  public static final String SPECIAL_HABIT_ID = "special-habit-99";
  public static final String SPECIAL_AVATAR_ID = "special-avatar-123";

  // Recurrence instances
  public static final DailyRecurrence DAILY_RECURRENCE = new DailyRecurrence();
  public static final WeeklyRecurrence WEEKLY_RECURRENCE =
      new WeeklyRecurrence(DEFAULT_DAY_OF_WEEK);
  public static final MonthlyRecurrence MONTHLY_RECURRENCE =
      new MonthlyRecurrence(DEFAULT_DAY_OF_MONTH);

  // Timestamps
  public static final LocalDateTime ATTENDED_AT = LocalDateTime.of(2026, 3, 16, 10, 0);
  public static final LocalDateTime NEXT_ATTENDED_AT = LocalDateTime.of(2026, 3, 17, 9, 30);
  public static final LocalDateTime CONTROLLER_ATTENDED_AT = LocalDateTime.of(2026, 3, 17, 8, 0);

  // Habit factories
  public static Habit hydrateHabit() {
    Habit habit =
        new Habit(HABIT_ID, AVATAR_ID, TITLE, DESCRIPTION, DAILY_RECURRENCE, Optional.empty());
    //    habit.setTitle(TITLE);
    //    habit.setDescription(DESCRIPTION);
    habit.setTags(List.of(new Tag(TAG_HEALTH), new Tag(TAG_WELLNESS)));
    //    habit.setRecurrence(DAILY_RECURRENCE);
    habit.attendHabit(ATTENDED_AT);
    return habit;
  }

  public static Habit hydrateHabitWithQuest() {
    Habit habit =
        new Habit(HABIT_ID, AVATAR_ID, TITLE, DESCRIPTION, DAILY_RECURRENCE, Optional.of(QUEST_1));
    //    habit.setTitle(TITLE);
    //    habit.setDescription(DESCRIPTION);
    //    habit.setRecurrence(DAILY_RECURRENCE);
    habit.setTags(List.of(new Tag(TAG_HEALTH)));
    habit.attendHabit(CONTROLLER_ATTENDED_AT);
    return habit;
  }

  public static Habit habitWithRecurrence(
      habitquest.tracking.domain.reminder.Recurrence recurrence) {
    return new Habit(HABIT_ID, AVATAR_ID, TITLE, DESCRIPTION, recurrence, Optional.empty());
  }

  public static Habit neverAttendedHabit() {
    Habit habit =
        new Habit(
            new Id<>(HABIT_NULL),
            AVATAR_ID,
            TITLE,
            DESCRIPTION,
            DAILY_RECURRENCE,
            Optional.empty());
    //    habit.setTitle(TITLE);
    //    habit.setDescription(DESCRIPTION);
    //    habit.setRecurrence(DAILY_RECURRENCE);
    return habit;
  }

  public static Habit overdueHabit() {
    Habit habit =
        new Habit(
            OVERDUE_HABIT_ID, AVATAR_ID, TITLE, DESCRIPTION, DAILY_RECURRENCE, Optional.empty());
    //    habit.setTitle(TITLE);
    //    habit.setDescription(DESCRIPTION);
    //    habit.setRecurrence(DAILY_RECURRENCE);
    habit.attendHabit(LocalDateTime.now().minusDays(2));
    return habit;
  }

  public static Habit upToDateMonthlyHabit() {
    Habit habit =
        new Habit(
            new Id<>(HABIT_OK),
            AVATAR_ID,
            TITLE,
            DESCRIPTION,
            new MonthlyRecurrence(20),
            Optional.empty());
    //    habit.setTitle(TITLE);
    //    habit.setDescription(DESCRIPTION);
    //    habit.setRecurrence(new MonthlyRecurrence(20));
    habit.attendHabit(LocalDateTime.now());
    return habit;
  }

  // Prevent instantiation
  private HabitFixtures() {
    throw new UnsupportedOperationException("utility class");
  }
}
