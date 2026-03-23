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
      case QuestCreated e -> handleQuestCreated(e);
      case QuestCompleted e -> handleQuestCompleted(e);
      case QuestNotCompleted e -> handleQuestNotCompleted(e);
      case QuestJoined e -> handleQuestJoined(e);
      case QuestLeft e -> handleQuestLeft(e);
      default -> throw new IllegalArgumentException("Unknown event type: " + event.getClass());
    }
  }

  public void handleQuestCreated(QuestCreated e) {
    questNotifier.notifyQuestCreated(e);
  }

  public void handleQuestCompleted(QuestCompleted e) {
    questNotifier.notifyQuestCompleted(e);
  }

  public void handleQuestNotCompleted(QuestNotCompleted e) {
    questNotifier.notifyQuestNotCompleted(e);
  }

  public void handleQuestJoined(QuestJoined e) {
    questNotifier.notifyQuestJoined(e);
  }

  public void handleQuestLeft(QuestLeft e) {
    questNotifier.notifyQuestLeft(e);
  }
}
