package habitquest.quest.application.port.in;

import common.ddd.Id;
import habitquest.quest.application.exceptions.QuestNotFoundException;
import habitquest.quest.domain.*;
import java.time.Duration;
import java.time.LocalDate;

public interface QuestCommandService {

  Quest createQuest(String name, Duration duration);

  Quest updateQuest(Id<Quest> id, Quest quest) throws QuestNotFoundException;

  void deleteQuest(Id<Quest> id) throws QuestNotFoundException;

  Quest updateName(Id<Quest> questId, String name) throws QuestNotFoundException;

  Quest updateDuration(Id<Quest> questId, Duration duration) throws QuestNotFoundException;

  Quest updateReward(Id<Quest> questId, MoneyReward reward) throws QuestNotFoundException;

  Quest addHabit(Id<Quest> questId, Habit habit) throws QuestNotFoundException;

  Quest removeHabit(Id<Quest> questId, Id<Habit> habit) throws QuestNotFoundException;

  ActiveQuests recordHabitAttendance(
      Id<Quest> questId, Id<Avatar> avatarId, Id<Habit> habitId, LocalDate attendedOn)
      throws QuestNotFoundException;

  ActiveQuests joinQuest(Id<Quest> questId, Id<Avatar> avatarId, LocalDate joinedOn)
      throws QuestNotFoundException;
}
