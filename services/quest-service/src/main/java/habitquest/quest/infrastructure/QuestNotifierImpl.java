package habitquest.quest.infrastructure;

import common.hexagonal.Adapter;
import habitquest.quest.application.QuestLogger;
import habitquest.quest.application.QuestNotifier;
import habitquest.quest.domain.events.QuestCompleted;
import habitquest.quest.domain.events.QuestCreated;
import habitquest.quest.domain.events.QuestJoined;
import habitquest.quest.domain.events.QuestLeft;
import habitquest.quest.domain.events.QuestNotCompleted;
import java.time.Instant;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

@Adapter
@Component
public class QuestNotifierImpl implements QuestNotifier {

  static final String QUEST_CREATED_BINDING = "quest.created";
  static final String QUEST_COMPLETED_BINDING = "quest.completed";
  static final String QUEST_NOT_COMPLETED_BINDING = "quest.not-completed";
  static final String QUEST_JOINED_BINDING = "quest.joined";
  static final String QUEST_LEFT_BINDING = "quest.left";

  private final StreamBridge streamBridge;
  private final QuestLogger log;

  public QuestNotifierImpl(StreamBridge streamBridge, QuestLogger log) {
    this.streamBridge = streamBridge;
    this.log = log;
  }

  @Override
  public void notifyQuestCreated(QuestCreated event) {
    QuestCreatedMessage message =
        new QuestCreatedMessage(
            event.quest().getId().value(), event.quest().getName(), Instant.now());

    log.info(message, "Publishing QuestCreated event");
    boolean sent = streamBridge.send(QUEST_CREATED_BINDING, message);
    if (!sent) {
      log.error(message, "Failed to publish QuestCreated event", null);
    }
  }

  @Override
  public void notifyQuestCompleted(QuestCompleted event) {
    QuestCompletedMessage message =
        new QuestCompletedMessage(
            event.quest().getId().value(), event.avatarId().value(), Instant.now());

    log.info(message, "Publishing QuestCompleted event");
    boolean sent = streamBridge.send(QUEST_COMPLETED_BINDING, message);
    if (!sent) {
      log.error(message, "Failed to publish QuestCompleted event", null);
    }
  }

  @Override
  public void notifyQuestNotCompleted(QuestNotCompleted event) {
    QuestNotCompletedMessage message = new QuestNotCompletedMessage(Instant.now());

    log.info(message, "Publishing QuestNotCompleted event");
    boolean sent = streamBridge.send(QUEST_NOT_COMPLETED_BINDING, message);
    if (!sent) {
      log.error(message, "Failed to publish QuestNotCompleted event", null);
    }
  }

  @Override
  public void notifyQuestJoined(QuestJoined event) {
    QuestJoinedMessage message =
        new QuestJoinedMessage(
            event.quest().getId().value(), event.avatarId().value(), Instant.now());

    log.info(message, "Publishing QuestJoined event");
    boolean sent = streamBridge.send(QUEST_JOINED_BINDING, message);
    if (!sent) {
      log.error(message, "Failed to publish QuestJoined event", null);
    }
  }

  @Override
  public void notifyQuestLeft(QuestLeft event) {
    QuestLeftMessage message =
        new QuestLeftMessage(
            event.quest().getId().value(), event.avatarId().value(), Instant.now());

    log.info(message, "Publishing QuestLeft event");
    boolean sent = streamBridge.send(QUEST_LEFT_BINDING, message);
    if (!sent) {
      log.error(message, "Failed to publish QuestLeft event", null);
    }
  }

  public record QuestCreatedMessage(String questId, String name, Instant occurredOn) {}

  public record QuestCompletedMessage(String questId, String avatarId, Instant occurredOn) {}

  public record QuestNotCompletedMessage(Instant occurredOn) {}

  public record QuestJoinedMessage(String questId, String avatarId, Instant occurredOn) {}

  public record QuestLeftMessage(String questId, String avatarId, Instant occurredOn) {}
}
