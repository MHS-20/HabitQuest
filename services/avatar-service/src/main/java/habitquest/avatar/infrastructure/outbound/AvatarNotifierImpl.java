package habitquest.avatar.infrastructure.outbound;

import common.hexagonal.Adapter;
import habitquest.avatar.application.port.out.AvatarLogger;
import habitquest.avatar.application.port.out.AvatarNotifier;
import habitquest.avatar.domain.events.Dead;
import habitquest.avatar.domain.events.LevelUpped;
import habitquest.avatar.domain.events.NewSpellLearned;
import habitquest.avatar.domain.events.SkillPointAssigned;
import java.time.Instant;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

@Adapter
@Component
public class AvatarNotifierImpl implements AvatarNotifier {

  static final String LEVEL_UPPED_BINDING = "avatar.level-upped";
  static final String DEAD_BINDING = "avatar.dead";
  static final String SKILL_POINT_BINDING = "avatar.skill-point-assigned";
  static final String NEW_SPELL_BINDING = "avatar.new-spell-learned";

  private final StreamBridge streamBridge;
  private final AvatarLogger log;

  public AvatarNotifierImpl(StreamBridge streamBridge, AvatarLogger log) {
    this.streamBridge = streamBridge;
    this.log = log;
  }

  @Override
  public void notifyLevelUpped(LevelUpped event) {
    LevelUppedMessage message =
        new LevelUppedMessage(
            event.avatarId().value(), event.newLevel().levelNumber(), Instant.now());

    log.info(message, "Publishing LevelUpped event");
    boolean sent = streamBridge.send(LEVEL_UPPED_BINDING, message);
    if (!sent) {
      log.error(message, "Failed to publish LevelUpped event", null);
    }
  }

  @Override
  public void notifyDead(Dead event) {
    DeadMessage message = new DeadMessage(event.avatarId().value(), Instant.now());

    log.info(message, "Publishing Dead event");
    boolean sent = streamBridge.send(DEAD_BINDING, message);
    if (!sent) {
      log.error(message, "Failed to publish Dead event", null);
    }
  }

  @Override
  public void notifySkillPointAssigned(SkillPointAssigned event) {
    SkillPointAssignedMessage message =
        new SkillPointAssignedMessage(
            event.avatarId().value(),
            event.stat().getClass().getSimpleName(),
            event.stat().value(),
            Instant.now());
    log.info(message, "Publishing SkillPointAssigned event");
    boolean sent = streamBridge.send(SKILL_POINT_BINDING, message);
    if (!sent) {
      log.error(message, "Failed to publish SkillPointAssigned event", null);
    }
  }

  @Override
  public void notifyNewSpellLearned(NewSpellLearned event) {
    NewSpellLearnedMessage message =
        new NewSpellLearnedMessage(
            event.avatarId().value(),
            event.spell().name(),
            event.spell().getDescription(),
            Instant.now());
    log.info(message, "Publishing NewSpellLearned event");
    boolean sent = streamBridge.send(NEW_SPELL_BINDING, message);
    if (!sent) {
      log.error(message, "Failed to publish NewSpellLearned event", null);
    }
  }

  public record LevelUppedMessage(String avatarId, Integer newLevel, Instant occurredOn) {}

  public record DeadMessage(String avatarId, Instant occurredOn) {}

  public record SkillPointAssignedMessage(
      String avatarId, String statType, Integer newValue, Instant occurredOn) {}

  public record NewSpellLearnedMessage(
      String avatarId, String spellName, String description, Instant occurredOn) {}
}
