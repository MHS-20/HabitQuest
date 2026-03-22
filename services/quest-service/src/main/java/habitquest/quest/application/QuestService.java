package habitquest.quest.application;

import common.ddd.Id;
import common.hexagonal.InBoundPort;
import habitquest.quest.domain.Habit;
import habitquest.quest.domain.Quest;
import habitquest.quest.domain.Reward;
import java.time.Duration;
import java.util.List;

@InBoundPort
public interface QuestService {
  Quest createQuest(String name);

  Quest getQuest(Id<Quest> id) throws QuestNotFoundException;

  Quest updateQuest(Id<Quest> id, Quest quest) throws QuestNotFoundException;

  void deleteQuest(Id<Quest> id) throws QuestNotFoundException;

  // region getters
  String getName(Id<Quest> questId) throws QuestNotFoundException;

  Duration getDuration(Id<Quest> questId) throws QuestNotFoundException;

  Reward getReward(Id<Quest> questId) throws QuestNotFoundException;

  List<Habit> getHabits(Id<Quest> questId) throws QuestNotFoundException;

  // endregion

  // region updaters
  Quest updateName(Id<Quest> questId, String name) throws QuestNotFoundException;

  Quest updateDuration(Id<Quest> questId, Duration duration) throws QuestNotFoundException;

  Quest updateReward(Id<Quest> questId, Reward reward) throws QuestNotFoundException;

  Quest addHabit(Id<Quest> questId, Habit habit) throws QuestNotFoundException;

  Quest removeHabit(Id<Quest> questId, Habit habit) throws QuestNotFoundException;
  // endregion
}
