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
  private final QuestLogger log;

  public QuestObserverImpl(QuestNotifier questNotifier, QuestLogger log) {
    this.questNotifier = questNotifier;
    this.log = log;
  }

  @Override
  public void notifyQuestEvent(QuestEvent event) {
    log.info(event, "Received quest event");
    switch (event) {
      case QuestCreated e -> handleQuestCreated(e);
      case QuestCompleted e -> handleQuestCompleted(e);
      case QuestNotCompleted e -> handleQuestNotCompleted(e);
      case QuestJoined e -> handleQuestJoined(e);
      case QuestLeft e -> handleQuestLeft(e);
      default -> {
        log.warn(event, "Unknown event type received");
        throw new IllegalArgumentException("Unknown event type: " + event.getClass());
      }
    }
  }

  public void handleQuestCreated(QuestCreated e) {
    log.info(e, "Handling QuestCreated event");
    questNotifier.notifyQuestCreated(e);
  }

  public void handleQuestCompleted(QuestCompleted e) {
    log.info(e, "Handling QuestCompleted event");
    questNotifier.notifyQuestCompleted(e);
  }

  public void handleQuestNotCompleted(QuestNotCompleted e) {
    log.info(e, "Handling QuestNotCompleted event");
    questNotifier.notifyQuestNotCompleted(e);
  }

  public void handleQuestJoined(QuestJoined e) {
    log.info(e, "Handling QuestJoined event");
    questNotifier.notifyQuestJoined(e);
  }

  public void handleQuestLeft(QuestLeft e) {
    log.info(e, "Handling QuestLeft event");
    questNotifier.notifyQuestLeft(e);
  }
}