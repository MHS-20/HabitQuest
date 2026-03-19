package habitquest.quest.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Quest domain")
class QuestTest {

  private static final String QUEST_ID = "quest-1";
  private static final String QUEST_NAME = "Morning Routine";
  private static final String UPDATED_NAME = "Evening Routine";
  private static final String HABIT_ID_1 = "habit-1";
  private static final String HABIT_ID_2 = "habit-2";

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
    MoneyReward reward = new MoneyReward();

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
    Habit firstHabit = new Habit(HABIT_ID_1);
    Habit secondHabit = new Habit(HABIT_ID_2);

    quest.addHabit(firstHabit);
    quest.addHabit(secondHabit);

    assertThat(quest.getHabits()).extracting(Habit::getId).containsExactly(HABIT_ID_1, HABIT_ID_2);

    quest.removeHabit(firstHabit);

    assertThat(quest.getHabits()).extracting(Habit::getId).containsExactly(HABIT_ID_2);
  }
}
