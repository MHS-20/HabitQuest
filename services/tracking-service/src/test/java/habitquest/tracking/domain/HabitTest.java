package habitquest.tracking.domain;

import static habitquest.tracking.HabitFixtures.*; // Import statico delle fixture
import static org.assertj.core.api.Assertions.assertThat;

import habitquest.tracking.domain.reminder.DailyRecurrence;
import habitquest.tracking.domain.reminder.MonthlyRecurrence;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Habit domain")
class HabitTest {

  @Test
  @DisplayName("attendHabit stores last attended date")
  void attendHabitStoresDate() {
    Habit habit = neverAttendedHabit();
    LocalDateTime attendedAt = LocalDateTime.of(2026, 3, 19, 9, 0);
    habit.attendHabit(attendedAt);
    assertThat(habit.getLastAttendedDate()).isEqualTo(attendedAt);
  }

  @Test
  @DisplayName("nextRecurrence(date) uses configured recurrence strategy")
  void nextRecurrenceFromProvidedDateUsesRecurrence() {
    Habit habit = habitWithRecurrence(new DailyRecurrence());
    habit.setRecurrence(new MonthlyRecurrence(31));
    LocalDateTime next = habit.nextRecurrence(LocalDateTime.of(2026, 4, 10, 8, 30));
    assertThat(next).isEqualTo(LocalDateTime.of(2026, 5, 31, 8, 30));
  }

  @Test
  @DisplayName("setters update mutable state")
  void settersUpdateState() {
    Habit habit = neverAttendedHabit();

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
    Habit habit = hydrateHabitWithQuest();
    assertThat(habit.getId()).isEqualTo(HABIT_ID);
    assertThat(habit.getAvatarId()).isEqualTo(AVATAR_ID);
    assertThat(habit.getAssociatedQuestId()).contains(QUEST_1);
  }
}
