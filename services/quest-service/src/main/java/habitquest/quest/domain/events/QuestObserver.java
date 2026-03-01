package habitquest.quest.domain.events;

public interface QuestObserver {
  void notifyQuestEvent(QuestEvent event);
}
