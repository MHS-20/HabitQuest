package habitquest.quest.application;

import common.hexagonal.OutBoundPort;
import habitquest.quest.domain.events.QuestCompleted;
import habitquest.quest.domain.events.QuestCreated;
import habitquest.quest.domain.events.QuestJoined;
import habitquest.quest.domain.events.QuestLeft;
import habitquest.quest.domain.events.QuestNotCompleted;

@OutBoundPort
public interface QuestNotifier {
  void notifyQuestCreated(QuestCreated event);

  void notifyQuestCompleted(QuestCompleted event);

  void notifyQuestNotCompleted(QuestNotCompleted event);

  void notifyQuestJoined(QuestJoined event);

  void notifyQuestLeft(QuestLeft event);
}
