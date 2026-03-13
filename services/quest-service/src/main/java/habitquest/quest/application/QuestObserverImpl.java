package habitquest.quest.application;

import common.hexagonal.Adapter;
import habitquest.quest.domain.events.QuestCompleted;
import habitquest.quest.domain.events.QuestCreated;
import habitquest.quest.domain.events.QuestEvent;
import habitquest.quest.domain.events.QuestJoined;
import habitquest.quest.domain.events.QuestLeft;
import habitquest.quest.domain.events.QuestNotCompleted;
import habitquest.quest.domain.events.QuestObserver;
import org.springframework.stereotype.Component;

@Adapter
@Component
public class QuestObserverImpl implements QuestObserver {

  private final QuestNotifier questNotifier;

  public QuestObserverImpl(QuestNotifier questNotifier) {
    this.questNotifier = questNotifier;
  }

  @Override
  public void notifyQuestEvent(QuestEvent event) {
    switch (event) {
      case QuestCreated e -> questNotifier.notifyQuestCreated(e);
      case QuestCompleted e -> questNotifier.notifyQuestCompleted(e);
      case QuestNotCompleted e -> questNotifier.notifyQuestNotCompleted(e);
      case QuestJoined e -> questNotifier.notifyQuestJoined(e);
      case QuestLeft e -> questNotifier.notifyQuestLeft(e);
      default -> throw new IllegalArgumentException("Unknown event type: " + event.getClass());
    }
  }
}
