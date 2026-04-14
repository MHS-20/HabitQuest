package habitquest.notification.infrastructure.consumers.avatar;

import java.time.Instant;

public class AvatarMessages {

  public record LevelUppedMessage(String avatarId, Integer newLevel, Instant occurredOn) {}

  public record DeadMessage(String avatarId, Instant occurredOn) {}

  public record SkillPointAssignedMessage(
      String avatarId, String statType, Integer newValue, Instant occurredOn) {}

  public record NewSpellLearnedMessage(
      String avatarId, String spellName, String description, Instant occurredOn) {}
}
