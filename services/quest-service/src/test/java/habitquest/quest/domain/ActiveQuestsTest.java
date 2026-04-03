package habitquest.quest.domain;

import static org.assertj.core.api.Assertions.assertThat;

import common.ddd.Id;
import habitquest.quest.domain.reminder.DailyRecurrence;
import habitquest.quest.domain.reminder.WeeklyRecurrence;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ActiveQuests domain")
class ActiveQuestsTest {

  @Test
  @DisplayName("fromQuest computes required occurrences by recurrence in the active window")
  void fromQuestComputesRequiredOccurrences() {
    Quest quest = new Quest(new Id<>("quest-1"), "Routine");
    quest.setDuration(Duration.ofDays(14));

    Habit daily =
        new Habit(
            new Id<>("habit-daily"),
            "Drink water",
            "8 glasses",
            List.of(new Tag("health")),
            new DailyRecurrence());
    Habit weekly =
        new Habit(
            new Id<>("habit-weekly"),
            "Clean desk",
            "Weekly cleanup",
            List.of(new Tag("home")),
            new WeeklyRecurrence(DayOfWeek.MONDAY));

    quest.addHabit(daily);
    quest.addHabit(weekly);

    ActiveQuests active =
        ActiveQuests.fromQuest(
            new Id<>("active-1"), new Id<>("avatar-1"), LocalDate.of(2026, 4, 1), quest);

    assertThat(active.getRequiredOccurrences().get(daily.getId())).isEqualTo(14);
    assertThat(active.getRequiredOccurrences().get(weekly.getId())).isEqualTo(2);
    assertThat(active.getStatus()).isEqualTo(ActiveQuests.Status.IN_PROGRESS);
  }

  @Test
  @DisplayName("quest is completed only after every habit is attended at least once")
  void completesOnlyWhenEveryHabitIsAttended() {
    Quest quest = new Quest(new Id<>("quest-2"), "Two day quest");
    quest.setDuration(Duration.ofDays(2));

    Habit firstHabit =
        new Habit(
            new Id<>("habit-1"),
            "Stretch",
            "Morning stretch",
            List.of(new Tag("fitness")),
            new DailyRecurrence());
    Habit secondHabit =
        new Habit(
            new Id<>("habit-2"),
            "Hydrate",
            "Drink water",
            List.of(new Tag("health")),
            new DailyRecurrence());
    quest.addHabit(firstHabit);
    quest.addHabit(secondHabit);

    ActiveQuests active =
        ActiveQuests.fromQuest(
            new Id<>("active-2"), new Id<>("avatar-2"), LocalDate.of(2026, 4, 1), quest);

    assertThat(active.recordAttendance(firstHabit.getId(), LocalDate.of(2026, 4, 1))).isTrue();
    assertThat(active.getStatus()).isEqualTo(ActiveQuests.Status.IN_PROGRESS);

    assertThat(active.recordAttendance(secondHabit.getId(), LocalDate.of(2026, 4, 2))).isTrue();
    assertThat(active.getStatus()).isEqualTo(ActiveQuests.Status.COMPLETED);
    assertThat(active.remainingOccurrences(firstHabit.getId())).isEqualTo(1);
    assertThat(active.remainingOccurrences(secondHabit.getId())).isEqualTo(1);
  }

  @Test
  @DisplayName("in-progress quest expires after the active window")
  void expiresAfterActiveWindow() {
    Quest quest = new Quest(new Id<>("quest-3"), "Daily");
    quest.setDuration(Duration.ofDays(2));

    Habit firstHabit =
        new Habit(
            new Id<>("habit-1"),
            "Walk",
            "Walk 30 minutes",
            List.of(new Tag("health")),
            new DailyRecurrence());
    Habit secondHabit =
        new Habit(
            new Id<>("habit-2"),
            "Stretch",
            "Morning stretch",
            List.of(new Tag("fitness")),
            new DailyRecurrence());
    quest.addHabit(firstHabit);
    quest.addHabit(secondHabit);

    ActiveQuests active =
        ActiveQuests.fromQuest(
            new Id<>("active-3"), new Id<>("avatar-3"), LocalDate.of(2026, 4, 1), quest);

    active.recordAttendance(firstHabit.getId(), LocalDate.of(2026, 4, 1));
    active.refreshStatus(LocalDate.of(2026, 4, 4));

    assertThat(active.getStatus()).isEqualTo(ActiveQuests.Status.EXPIRED);
  }
}
