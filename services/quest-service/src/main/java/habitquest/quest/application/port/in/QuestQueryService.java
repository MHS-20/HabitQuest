package habitquest.quest.application.port.in;

import common.ddd.Id;
import habitquest.quest.application.exceptions.QuestNotFoundException;
import habitquest.quest.application.service.QuestProgressView;
import habitquest.quest.domain.*;
import java.time.Duration;
import java.util.List;

public interface QuestQueryService {

  List<Quest> getAllQuests();

  Quest getQuest(Id<Quest> id) throws QuestNotFoundException;

  String getName(Id<Quest> questId) throws QuestNotFoundException;

  Duration getDuration(Id<Quest> questId) throws QuestNotFoundException;

  Reward getReward(Id<Quest> questId) throws QuestNotFoundException;

  List<Habit> getHabits(Id<Quest> questId) throws QuestNotFoundException;

  List<QuestProgressView> getActiveQuestProgressByAvatar(Id<Avatar> avatarId);
}
