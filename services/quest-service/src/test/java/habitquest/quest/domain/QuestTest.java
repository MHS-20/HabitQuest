package habitquest.quest.domain;

import static org.assertj.core.api.Assertions.assertThat;

import common.ddd.Id;
import habitquest.quest.domain.reminder.DailyRecurrence;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Quest domain")
class QuestTest {

  private static final Id<Quest> QUEST_ID = new Id<>("quest-1");
  private static final String QUEST_NAME = "Morning Routine";
  private static final String UPDATED_NAME = "Evening Routine";
  private static final Id<Habit> HABIT_ID_1 = new Id<>("habit-1");
  private static final Id<Habit> HABIT_ID_2 = new Id<>("habit-2");
  private static final String HABIT_TITLE = "Morning run";
  private static final String HABIT_DESCRIPTION = "Run 5km every morning";

  private Habit stubHabit() {
    return new Habit(
        HABIT_ID_1,
        HABIT_TITLE,
        HABIT_DESCRIPTION,
        List.of(new Tag("health")),
        new DailyRecurrence());
  }

  private Habit stubHabitNew() {
    return new Habit(
        HABIT_ID_2,
        "Meditation",
        "Meditate for 10 minutes",
        List.of(new Tag("mindfulness")),
        new DailyRecurrence());
  }

  @Test
  @DisplayName("constructor with name initializes id and name")
  void constructorWithNameInitializesFields() {
    Quest quest = new Quest(QUEST_ID, QUEST_NAME);

    assertThat(quest.getId()).isEqualTo(QUEST_ID);
    assertThat(quest.getName()).isEqualTo(QUEST_NAME);
    assertThat(quest.getHabits()).isEmpty();
  }

  @Test
  @DisplayName("setters update mutable fields")
  void settersUpdateFields() {
    Quest quest = new Quest(QUEST_ID, QUEST_NAME);
    Duration duration = Duration.ofMinutes(45);
    MoneyReward reward = new MoneyReward(10);
    quest.setName(UPDATED_NAME);
    quest.setDuration(duration);
    quest.setReward(reward);
    assertThat(quest.getName()).isEqualTo(UPDATED_NAME);
    assertThat(quest.getDuration()).isEqualTo(duration);
    assertThat(quest.getReward()).isEqualTo(reward);
  }

  @Test
  @DisplayName("addHabit and removeHabit update the habits list")
  void addAndRemoveHabit() {
    Quest quest = new Quest(QUEST_ID, QUEST_NAME);
    quest.addHabit(stubHabit());
    quest.addHabit(stubHabitNew());
    assertThat(quest.getHabits()).extracting(Habit::getId).containsExactly(HABIT_ID_1, HABIT_ID_2);
    quest.removeHabit(stubHabit().getId());
    assertThat(quest.getHabits()).extracting(Habit::getId).containsExactly(HABIT_ID_2);
  }
}
