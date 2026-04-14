package habitquest.avatar.application.service;

import habitquest.avatar.application.port.out.AvatarLogger;
import habitquest.avatar.application.port.out.AvatarNotifier;
import habitquest.avatar.domain.events.*;
import org.springframework.stereotype.Component;

@Component
public class AvatarObserverImpl implements AvatarObserver {

  private final AvatarNotifier avatarNotifier;
  private final AvatarLogger log;

  public AvatarObserverImpl(AvatarNotifier avatarNotifier, AvatarLogger log) {
    this.avatarNotifier = avatarNotifier;
    this.log = log;
  }

  @Override
  public void notifyAvatarEvent(AvatarEvent event) {
    switch (event) {
      case LevelUpped e -> handleLevelUpped(e);
      case Dead e -> handleDead(e);
      case SkillPointAssigned e -> handleSkillPointAssigned(e);
      case NewSpellLearned e -> handleNewSpellLearned(e);
      default -> throw new IllegalArgumentException("Unknown event type: " + event.getClass());
    }
  }

  public void handleLevelUpped(LevelUpped e) {
    log.info(e, "Handling LevelUpped event");
    avatarNotifier.notifyLevelUpped(e);
  }

  public void handleDead(Dead e) {
    log.info(e, "Handling Dead event");
    avatarNotifier.notifyDead(e);
  }

  public void handleSkillPointAssigned(SkillPointAssigned e) {
    log.info(e, "Handling SkillPointAssigned event");
    avatarNotifier.notifySkillPointAssigned(e);
  }

  public void handleNewSpellLearned(NewSpellLearned e) {
    log.info(e, "Handling NewSpellLearned event");
    avatarNotifier.notifyNewSpellLearned(e);
  }
}
