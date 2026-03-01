package habitquest.quest.application;

import common.hexagonal.InBoundPort;
import habitquest.quest.domain.Habit;
import habitquest.quest.domain.Quest;
import habitquest.quest.domain.Reward;
import java.time.Duration;
import java.util.List;

@InBoundPort
public interface QuestService {
  Quest createQuest(String name);

  Quest getQuest(String id) throws QuestNotFoundException;

  Quest updateQuest(String id, Quest quest) throws QuestNotFoundException;

  void deleteQuest(String id) throws QuestNotFoundException;

  // region getters
  String getName(String questId) throws QuestNotFoundException;

  Duration getDuration(String questId) throws QuestNotFoundException;

  Reward getReward(String questId) throws QuestNotFoundException;

  List<Habit> getHabits(String questId) throws QuestNotFoundException;

  // endregion

  // region updaters
  Quest updateName(String questId, String name) throws QuestNotFoundException;

  Quest updateDuration(String questId, Duration duration) throws QuestNotFoundException;

  Quest updateReward(String questId, Reward reward) throws QuestNotFoundException;

  Quest addHabit(String questId, Habit habit) throws QuestNotFoundException;

  Quest removeHabit(String questId, Habit habit) throws QuestNotFoundException;
  // endregion
}
