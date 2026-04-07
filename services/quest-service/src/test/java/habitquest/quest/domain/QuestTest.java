package habitquest.quest.domain;

import static habitquest.quest.QuestFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Quest domain")
class QuestTest {

  @Test
  @DisplayName("constructor with name initializes id and name")
  void constructorWithNameInitializesFields() {
    Quest quest = emptyQuest();
    assertThat(quest.getId()).isEqualTo(QUEST_ID);
    assertThat(quest.getName()).isEqualTo(QUEST_NAME);
    assertThat(quest.getHabits()).isEmpty();
  }

  @Test
  @DisplayName("setters update mutable fields")
  void settersUpdateFields() {
    Quest quest = emptyQuest();
    Duration duration = Duration.ofMinutes(45);

    quest.setName("New Name");
    quest.setDuration(duration);
    quest.setReward(DEFAULT_MONEY_REWARD);

    assertThat(quest.getName()).isEqualTo("New Name");
    assertThat(quest.getDuration()).isEqualTo(duration);
    assertThat(quest.getReward()).isEqualTo(DEFAULT_MONEY_REWARD);
  }

  @Test
  @DisplayName("addHabit and removeHabit update the habits list")
  void addAndRemoveHabit() {
    Quest quest = emptyQuest();
    Habit habit1 = morningRunHabit();
    Habit habit2 = meditationHabit();

    quest.addHabit(habit1);
    quest.addHabit(habit2);

    assertThat(quest.getHabits()).extracting(Habit::getId).containsExactly(HABIT_ID_1, HABIT_ID_2);

    quest.removeHabit(HABIT_ID_1);
    assertThat(quest.getHabits()).extracting(Habit::getId).containsExactly(HABIT_ID_2);
  }
}
