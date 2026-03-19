package habitquest.tracking.domain;

import static org.assertj.core.api.Assertions.assertThat;

import habitquest.tracking.domain.reminder.DailyRecurrence;
import habitquest.tracking.domain.reminder.MonthlyRecurrence;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Habit domain")
class HabitTest {
  private static final String HABIT_ID = "habit-1";
  private static final String AVATAR_ID = "avatar-1";
  private static final String TITLE = "Hydrate";
  private static final String DESCRIPTION = "Drink water";

  @Test
  @DisplayName("attendHabit stores last attended date")
  void attendHabitStoresDate() {
    Habit habit =
        new Habit(
            HABIT_ID, AVATAR_ID, TITLE, DESCRIPTION, new DailyRecurrence(), Optional.of("quest-1"));
    LocalDateTime attendedAt = LocalDateTime.of(2026, 3, 19, 9, 0);

    habit.attendHabit(attendedAt);

    assertThat(habit.getLastAttendedDate()).isEqualTo(attendedAt);
  }

  @Test
  @DisplayName("nextRecurrence(date) uses configured recurrence strategy")
  void nextRecurrenceFromProvidedDateUsesRecurrence() {
    Habit habit =
        new Habit(HABIT_ID, AVATAR_ID, TITLE, DESCRIPTION, new DailyRecurrence(), Optional.empty());
    habit.setRecurrence(new MonthlyRecurrence(31));

    LocalDateTime next = habit.nextRecurrence(LocalDateTime.of(2026, 4, 10, 8, 30));

    assertThat(next).isEqualTo(LocalDateTime.of(2026, 5, 31, 8, 30));
  }

  @Test
  @DisplayName("setters update mutable state")
  void settersUpdateState() {
    Habit habit =
        new Habit(HABIT_ID, AVATAR_ID, TITLE, DESCRIPTION, new DailyRecurrence(), Optional.empty());

    habit.setTitle("Read");
    habit.setDescription("Read 20 pages");
    habit.setTags(List.of(new Tag("focus"), new Tag("mindset")));

    assertThat(habit.getTitle()).isEqualTo("Read");
    assertThat(habit.getDescription()).isEqualTo("Read 20 pages");
    assertThat(habit.getTags()).containsExactly(new Tag("focus"), new Tag("mindset"));
  }

  @Test
  @DisplayName("constructor keeps immutable identifiers")
  void constructorKeepsIds() {
    Habit habit =
        new Habit(
            HABIT_ID, AVATAR_ID, TITLE, DESCRIPTION, new DailyRecurrence(), Optional.of("quest-9"));

    assertThat(habit.getId()).isEqualTo("habit-1");
    assertThat(habit.getAvatarId()).isEqualTo("avatar-1");
    assertThat(habit.getAssociatedQuestId()).contains("quest-9");
  }
}
