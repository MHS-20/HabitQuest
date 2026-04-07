package habitquest.quest.domain;

import static habitquest.quest.QuestFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;

import common.ddd.Id;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ActiveQuests domain")
class ActiveQuestsTest {

  private static final LocalDate START_DATE = LocalDate.of(2026, 4, 1);

  @Test
  @DisplayName("fromQuest computes required occurrences by recurrence in the active window")
  void fromQuestComputesRequiredOccurrences() {
    Quest quest = complexQuest();

    ActiveQuests active =
        ActiveQuests.fromQuest(new Id<>("active-1"), AVATAR_ID_1, START_DATE, quest);

    assertThat(active.getRequiredOccurrences().get(HABIT_ID_1)).isEqualTo(14);
    assertThat(active.getRequiredOccurrences().get(weeklyHabit().getId())).isEqualTo(2);
    assertThat(active.getStatus()).isEqualTo(ActiveQuests.Status.IN_PROGRESS);
  }

  @Test
  @DisplayName("quest is completed only after every habit is attended at least once")
  void completesOnlyWhenEveryHabitIsAttended() {
    Quest quest = questWithDuration("quest-2", 2);
    Habit h1 = morningRunHabit();
    Habit h2 = meditationHabit();
    quest.addHabit(h1);
    quest.addHabit(h2);

    ActiveQuests active =
        ActiveQuests.fromQuest(new Id<>("active-2"), AVATAR_ID_1, START_DATE, quest);

    active.recordAttendance(HABIT_ID_1, START_DATE);
    assertThat(active.getStatus()).isEqualTo(ActiveQuests.Status.IN_PROGRESS);

    active.recordAttendance(HABIT_ID_2, START_DATE.plusDays(1));
    assertThat(active.getStatus()).isEqualTo(ActiveQuests.Status.COMPLETED);
  }

  @Test
  @DisplayName("in-progress quest expires after the active window")
  void expiresAfterActiveWindow() {
    Quest quest = questWithDuration("quest-3", 2);
    quest.addHabit(morningRunHabit());

    ActiveQuests active =
        ActiveQuests.fromQuest(new Id<>("active-3"), AVATAR_ID_1, START_DATE, quest);

    active.refreshStatus(START_DATE.plusDays(4));
    assertThat(active.getStatus()).isEqualTo(ActiveQuests.Status.EXPIRED);
  }
}
