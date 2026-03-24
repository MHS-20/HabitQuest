package habitquest.quest;

import common.ddd.Id;
import habitquest.quest.domain.Habit;
import habitquest.quest.domain.MoneyReward;
import habitquest.quest.domain.Quest;
import habitquest.quest.domain.Tag;
import habitquest.quest.domain.reminder.DailyRecurrence;
import java.time.Duration;
import java.util.List;

public final class QuestFixtures {
  // String literals
  public static final String QUEST_1 = "quest-1";
  public static final String QUEST_NAME = "Morning Routine";
  public static final String UNKNOWN_QUEST = "ghost-99";
  public static final String HABIT_1 = "habit-1";
  public static final String HABIT_2 = "habit-2";
  public static final String HABIT_TITLE = "Morning run";
  public static final String HABIT_DESC = "Run 5km every morning";
  public static final String HABIT_2_TITLE = "Meditation";
  public static final String HABIT_2_DESC = "Meditate for 10 minutes";
  public static final String TAG_HEALTH = "health";
  public static final String TAG_MINDFULNESS = "mindfulness";

  // Default values
  public static final Duration DEFAULT_DURATION = Duration.ofHours(2);
  public static final int DEFAULT_REWARD = 10;

  // Typed ids
  public static final Id<Quest> QUEST_ID = new Id<>(QUEST_1);
  public static final Id<Quest> UNKNOWN_ID = new Id<>(UNKNOWN_QUEST);
  public static final Id<Habit> HABIT_ID_1 = new Id<>(HABIT_1);
  public static final Id<Habit> HABIT_ID_2 = new Id<>(HABIT_2);

  // Reward instance
  public static final MoneyReward DEFAULT_MONEY_REWARD = new MoneyReward(DEFAULT_REWARD);

  // Habit factories
  public static Habit morningRunHabit() {
    return new Habit(
        HABIT_ID_1, HABIT_TITLE, HABIT_DESC, List.of(new Tag(TAG_HEALTH)), new DailyRecurrence());
  }

  public static Habit meditationHabit() {
    return new Habit(
        HABIT_ID_2,
        HABIT_2_TITLE,
        HABIT_2_DESC,
        List.of(new Tag(TAG_MINDFULNESS)),
        new DailyRecurrence());
  }

  // Quest factories
  public static Quest fullQuest() {
    Quest quest = new Quest(QUEST_ID, QUEST_NAME);
    quest.setDuration(DEFAULT_DURATION);
    quest.setReward(DEFAULT_MONEY_REWARD);
    quest.addHabit(morningRunHabit());
    return quest;
  }

  public static Quest emptyQuest() {
    return new Quest(QUEST_ID, QUEST_NAME);
  }

  public static Quest questWithMeditationHabit() {
    Quest quest = new Quest(QUEST_ID, QUEST_NAME);
    quest.addHabit(meditationHabit());
    return quest;
  }

  // Prevent instantiation
  private QuestFixtures() {
    throw new UnsupportedOperationException("utility class");
  }
}
