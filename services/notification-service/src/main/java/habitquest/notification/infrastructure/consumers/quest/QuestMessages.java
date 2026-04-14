package habitquest.notification.infrastructure.consumers.quest;

import java.time.Instant;

public class QuestMessages {
  public record QuestCreatedMessage(
      String questId, String avatarId, String name, Instant occurredOn) {}

  public record QuestCompletedMessage(String questId, String avatarId, Instant occurredOn) {}

  public record QuestNotCompletedMessage(String avatarId, Instant occurredOn) {}

  public record QuestJoinedMessage(String questId, String avatarId, Instant occurredOn) {}

  public record QuestLeftMessage(String questId, String avatarId, Instant occurredOn) {}
}
