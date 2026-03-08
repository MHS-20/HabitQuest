package habitquest.avatar.infrastructure;

import common.hexagonal.Adapter;
import habitquest.avatar.application.AvatarNotifier;
import habitquest.avatar.domain.events.Dead;
import habitquest.avatar.domain.events.LevelUpped;
import habitquest.avatar.domain.events.SkillPointAssigned;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

@Adapter
@Component
public class AvatarNotifierImpl implements AvatarNotifier {

  private static final Logger LOG = LoggerFactory.getLogger(AvatarNotifierImpl.class);

  static final String LEVEL_UPPED_BINDING = "avatar-level-upped-out-0";
  static final String DEAD_BINDING = "avatar-dead-out-0";
  static final String SKILL_POINT_BINDING = "avatar-skill-point-out-0";

  private final StreamBridge streamBridge;

  public AvatarNotifierImpl(StreamBridge streamBridge) {
    this.streamBridge = streamBridge;
  }

  @Override
  public void notifyLevelUpped(LevelUpped event) {
    LevelUppedMessage message =
        new LevelUppedMessage(event.newLevel().levelNumber(), Instant.now());

    LOG.info("Publishing LevelUpped event: newLevel={}", message.newLevel());
    boolean sent = streamBridge.send(LEVEL_UPPED_BINDING, message);
    if (!sent) {
      LOG.error("Failed to publish LevelUpped event for level {}", message.newLevel());
    }
  }

  @Override
  public void notifyDead(Dead event) {
    DeadMessage message = new DeadMessage(event.avatarId(), Instant.now());

    LOG.info("Publishing Dead event: avatarId={}", message.avatarId());
    boolean sent = streamBridge.send(DEAD_BINDING, message);
    if (!sent) {
      LOG.error("Failed to publish Dead event for avatarId {}", message.avatarId());
    }
  }

  @Override
  public void notifySkillPointAssigned(SkillPointAssigned event) {
    SkillPointAssignedMessage message =
        new SkillPointAssignedMessage(
            event.stat().getClass().getSimpleName(), event.stat().value(), Instant.now());

    LOG.info(
        "Publishing SkillPointAssigned event: stat={}, newValue={}",
        message.statType(),
        message.newValue());
    boolean sent = streamBridge.send(SKILL_POINT_BINDING, message);
    if (!sent) {
      LOG.error("Failed to publish SkillPointAssigned event for stat {}", message.statType());
    }
  }

  public record LevelUppedMessage(Integer newLevel, Instant occurredOn) {}

  public record DeadMessage(String avatarId, Instant occurredOn) {}

  public record SkillPointAssignedMessage(String statType, Integer newValue, Instant occurredOn) {}
}
