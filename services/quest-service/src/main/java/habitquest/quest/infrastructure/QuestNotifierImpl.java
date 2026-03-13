package habitquest.quest.infrastructure;

import common.hexagonal.Adapter;
import habitquest.quest.application.QuestNotifier;
import habitquest.quest.domain.events.QuestCompleted;
import habitquest.quest.domain.events.QuestCreated;
import habitquest.quest.domain.events.QuestJoined;
import habitquest.quest.domain.events.QuestLeft;
import habitquest.quest.domain.events.QuestNotCompleted;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

@Adapter
@Component
public class QuestNotifierImpl implements QuestNotifier {

  private static final Logger LOG = LoggerFactory.getLogger(QuestNotifierImpl.class);

  static final String QUEST_CREATED_BINDING = "quest-created-out-0";
  static final String QUEST_COMPLETED_BINDING = "quest-completed-out-0";
  static final String QUEST_NOT_COMPLETED_BINDING = "quest-not-completed-out-0";
  static final String QUEST_JOINED_BINDING = "quest-joined-out-0";
  static final String QUEST_LEFT_BINDING = "quest-left-out-0";

  private final StreamBridge streamBridge;

  public QuestNotifierImpl(StreamBridge streamBridge) {
    this.streamBridge = streamBridge;
  }

  @Override
  public void notifyQuestCreated(QuestCreated event) {
    QuestCreatedMessage message =
        new QuestCreatedMessage(event.quest().getId(), event.quest().getName(), Instant.now());

    LOG.info("Publishing QuestCreated event: questId={}", message.questId());
    boolean sent = streamBridge.send(QUEST_CREATED_BINDING, message);
    if (!sent) {
      LOG.error("Failed to publish QuestCreated event for questId {}", message.questId());
    }
  }

  @Override
  public void notifyQuestCompleted(QuestCompleted event) {
    QuestCompletedMessage message =
        new QuestCompletedMessage(event.quest().getId(), event.avatarId(), Instant.now());

    LOG.info(
        "Publishing QuestCompleted event: questId={}, avatarId={}",
        message.questId(),
        message.avatarId());
    boolean sent = streamBridge.send(QUEST_COMPLETED_BINDING, message);
    if (!sent) {
      LOG.error("Failed to publish QuestCompleted event for questId {}", message.questId());
    }
  }

  @Override
  public void notifyQuestNotCompleted(QuestNotCompleted event) {
    QuestNotCompletedMessage message = new QuestNotCompletedMessage(Instant.now());

    LOG.info("Publishing QuestNotCompleted event");
    boolean sent = streamBridge.send(QUEST_NOT_COMPLETED_BINDING, message);
    if (!sent) {
      LOG.error("Failed to publish QuestNotCompleted event");
    }
  }

  @Override
  public void notifyQuestJoined(QuestJoined event) {
    QuestJoinedMessage message =
        new QuestJoinedMessage(event.quest().getId(), event.avatarId(), Instant.now());

    LOG.info(
        "Publishing QuestJoined event: questId={}, avatarId={}",
        message.questId(),
        message.avatarId());
    boolean sent = streamBridge.send(QUEST_JOINED_BINDING, message);
    if (!sent) {
      LOG.error("Failed to publish QuestJoined event for questId {}", message.questId());
    }
  }

  @Override
  public void notifyQuestLeft(QuestLeft event) {
    QuestLeftMessage message =
        new QuestLeftMessage(event.quest().getId(), event.avatarId(), Instant.now());

    LOG.info(
        "Publishing QuestLeft event: questId={}, avatarId={}",
        message.questId(),
        message.avatarId());
    boolean sent = streamBridge.send(QUEST_LEFT_BINDING, message);
    if (!sent) {
      LOG.error("Failed to publish QuestLeft event for questId {}", message.questId());
    }
  }

  public record QuestCreatedMessage(String questId, String name, Instant occurredOn) {}

  public record QuestCompletedMessage(String questId, String avatarId, Instant occurredOn) {}

  public record QuestNotCompletedMessage(Instant occurredOn) {}

  public record QuestJoinedMessage(String questId, String avatarId, Instant occurredOn) {}

  public record QuestLeftMessage(String questId, String avatarId, Instant occurredOn) {}
}
